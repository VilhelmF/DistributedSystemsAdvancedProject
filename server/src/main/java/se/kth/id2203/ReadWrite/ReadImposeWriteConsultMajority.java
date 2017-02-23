package se.kth.id2203.ReadWrite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.atomicregister.*;
import se.kth.id2203.broadcasting.*;
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
    private int rid = 0;
    private HashMap<Address, ReadListValue> readlist = new HashMap<>();
    private boolean reading = false;
    private int N = config().getValue("id2203.project.replicationDegree", Integer.class);

    //******* Handlers ******
    protected final Handler<AR_Read_Request> readRequestHandler = new Handler<AR_Read_Request>() {

        @Override
        public void handle(AR_Read_Request readRequest) {
            LOG.info("Received AR_READ_REQUEST inside RIWC");
            rid++;
            acks = 0;
            readlist.clear();
            reading = true;
            trigger(new BEB_Broadcast(self, new Read(self, rid, readRequest.key, readRequest.id)), beb);
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
            LOG.info("RECEIVED READ inside RIWC");
            trigger(new Message(self, read.src, new Value(self, read.rid, timestamp, wr, read.key, keyValueStore.get(read.key), read.opId)), net);
        }
    };

    protected final ClassMatchedHandler<Write, Message> writeHandler = new ClassMatchedHandler<Write, Message>() {

        @Override
        public void handle(Write write, Message context) {

            LOG.info("RECEIVED WRITE inside RIWC");

            if (isBigger(write.wr, write.ts, wr, timestamp)) {
                timestamp= write.ts;
                wr = write.wr;
                keyValueStore.put(write.key, write.writeVal);
            }
            trigger(new Message(self, write.src, new Ack(self, write.rid, write.opId)), net);
        }
    };

    protected final ClassMatchedHandler<Value, Message> valueHandler = new ClassMatchedHandler<Value, Message>() {

        @Override
        public void handle(Value value, Message context) {

            LOG.info("RECEIVED VALUE inside RIWC");

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
                        broadcastval = writeVal;
                    }
                    trigger(new BEB_Broadcast(self, new Write(self, rid, maxtimestamp, rr, value.key, broadcastval, value.opId)), beb);
                }
            }
        }
    };

    protected final ClassMatchedHandler<Ack, Message> ackHandler = new ClassMatchedHandler<Ack, Message>() {

        @Override
        public void handle(Ack ack, Message context) {
            LOG.info("RECEIVED ACK inside RIWC");
            if (ack.rid == rid) {
                acks++;
                if (acks > N / 2) {
                    acks = 0;
                    if (reading) {
                        reading = false;
                        LOG.info("Triggering AR_READ_RESPONSE");
                        trigger(new AR_Read_Response(readVal, ack.opId), nnar);
                    } else {
                        LOG.info("Triggering AR_WRITE_RESPONSE");
                        LOG.info("ACK : " + ack.opId);
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
        subscribe(readHandler, net);
        subscribe(writeHandler, net);
        subscribe(valueHandler, net);
        subscribe(ackHandler, net);
    }


}
