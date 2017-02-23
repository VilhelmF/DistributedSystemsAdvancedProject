package se.kth.id2203.ReadWrite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.atomicregister.*;
import se.kth.id2203.broadcasting.*;
import se.kth.id2203.kvstore.OpResponse.Code;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.*;
import se.sics.kompics.network.Address;
import se.sics.kompics.network.Network;

import java.util.Collections;
import java.util.HashMap;

public class ReadImposeWriteConsultMajority extends ComponentDefinition {

    //******* Ports ******
    Negative<AtomicRegister> nnar = provides(AtomicRegister.class);
    Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    Positive<Network> net = requires(Network.class);

    //******* Fields ******
    final static Logger LOG = LoggerFactory.getLogger(ReadImposeWriteConsultMajority.class);
    final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    private int timestamp = 0;
    private int wr = 0;
    private final HashMap<Integer, Object> keyValueStore = new HashMap<>();
    private int acks = 0;
    private Object readVal = null;
    private Object writeVal = null;
    private Object casReferenceVal = null;
    private Object casNewVal = null;
    private Code casCode = null;
    private int rid = 0;
    private HashMap<Address, ReadListValue> readlist = new HashMap<>();
    private boolean reading = false;
    private boolean cas = false;
    private int N = config().getValue("id2203.project.replicationDegree", Integer.class);

    //******* Handlers ******
    protected final Handler<AR_Read_Request> readRequestHandler = new Handler<AR_Read_Request>() {

        @Override
        public void handle(AR_Read_Request readRequest) {
            rid++;
            acks = 0;
            readlist.clear();
            reading = true;
            trigger(new BEB_Broadcast(self, new Read(self, rid, readRequest.key, readRequest.id)), beb);
        }
    };

    protected final Handler<AR_CAS_Request> casRequestHandler = new Handler<AR_CAS_Request>() {

        @Override
        public void handle(AR_CAS_Request casRequest) {
            rid++;
            casReferenceVal = casRequest.referenceValue;
            casNewVal = casRequest.newValue;
            acks = 0;
            readlist.clear();
            cas = true;
            trigger(new BEB_Broadcast(self, new Read(self, rid, casRequest.key, casRequest.opId)), beb);
        }
    };

    protected final Handler<AR_Write_Request> writeRequestHandler = new Handler<AR_Write_Request>() {

        @Override
        public void handle(AR_Write_Request writeRequest) {
            rid++;
            writeVal = writeRequest.value;
            acks = 0;
            readlist.clear();
            trigger(new BEB_Broadcast(self, new Read(self, rid, writeRequest.key, writeRequest.opId)), beb);
        }
    };

    protected final ClassMatchedHandler<Read, Message> readHandler = new ClassMatchedHandler<Read, Message>() {

        @Override
        public void handle(Read read, Message context) {
            trigger(new Message(self, read.src, new Value(self, read.rid, timestamp, wr, read.key, keyValueStore.get(read.key), read.opId)), net);
        }
    };

    protected final ClassMatchedHandler<Write, Message> writeHandler = new ClassMatchedHandler<Write, Message>() {

        @Override
        public void handle(Write write, Message context) {
            if (isBigger(write.wr, write.ts, wr, timestamp)) {
                timestamp= write.ts;
                wr = write.wr;
                keyValueStore.put(write.key, write.writeVal);
            }
            trigger(new Message(self, write.src, new Ack(self, write.rid, write.opId)), net);
        }
    };

    protected final ClassMatchedHandler<CAS, Message> casHandler = new ClassMatchedHandler<CAS, Message>() {

        @Override
        public void handle(CAS cas, Message context) {
            /*
            if (isBigger(cas.wr, write.ts, wr, timestamp)) {
                timestamp= write.ts;
                wr = write.wr;
                keyValueStore.put(write.key, write.writeVal);
            }
            trigger(new Message(self, write.src, new Ack(self, write.rid, write.opId)), net);
            */
        }
    };

    protected final ClassMatchedHandler<Value, Message> valueHandler = new ClassMatchedHandler<Value, Message>() {

        @Override
        public void handle(Value value, Message context) {
            if (value.rid == rid) {
                readlist.put(value.src, new ReadListValue(value.ts, value.wr, value.value));
                if (readlist.size() > N / 2) {
                    ReadListValue readListValue = Collections.max(readlist.values());
                    readlist.clear();
                    readVal = readListValue.getValue();
                    Object broadcastval;
                    int maxtimestamp= readListValue.getTs();
                    int rr = readListValue.getWr();
                    if (reading) {
                        broadcastval = readVal;
                    } else {
                        maxtimestamp++;
                        rr = getRank(self);
                        if (cas) {
                            if (keyValueStore.get(value.key).equals(casReferenceVal)) {
                                casCode = Code.OK;
                                broadcastval = casNewVal;
                            } else {
                                casCode = Code.KEY_MISMATCH;
                                broadcastval = writeVal;
                            }

                        } else {
                            broadcastval = writeVal;
                        }
                    }
                    trigger(new BEB_Broadcast(self, new Write(self, rid, maxtimestamp, rr, value.key, broadcastval, value.opId)), beb);
                }
            }
        }
    };

    protected final ClassMatchedHandler<Ack, Message> ackHandler = new ClassMatchedHandler<Ack, Message>() {

        @Override
        public void handle(Ack ack, Message context) {
            if (ack.rid == rid) {
                acks++;
                if (acks > N / 2) {
                    acks = 0;
                    if (reading) {
                        reading = false;
                        trigger(new AR_Read_Response(readVal, ack.opId), nnar);
                    } else if (cas) {
                        cas = false;
                        trigger(new AR_CAS_Response(ack.opId, casCode), nnar);
                    } else {
                        trigger(new AR_Write_Response(ack.opId), nnar);
                    }
                }
            }
        }
    };

    public boolean isBigger(int writeWR, int writeTS, int wr, int ts) {
        if (writeWR == wr) {
            return writeTS > ts;
        } else {
            return writeWR > wr;
        }
    }

    public int getRank(Address address) {
        return address.getIp().getAddress().length;
    }

    {
        subscribe(readRequestHandler, nnar);
        subscribe(writeRequestHandler, nnar);
        subscribe(casRequestHandler, nnar);
        subscribe(readHandler, net);
        subscribe(writeHandler, net);
        subscribe(valueHandler, net);
        subscribe(ackHandler, net);
    }


}
