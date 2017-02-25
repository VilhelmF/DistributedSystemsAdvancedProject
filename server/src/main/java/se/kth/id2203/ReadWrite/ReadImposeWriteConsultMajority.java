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
    //private int acks = 0;
    //private Object readVal = null;
    //private Object writeVal = null;
    private Object casReferenceVal = null;
    private Object casNewVal = null;
    private Code casCode = null;
    private HashMap<Integer, AtomicRequest> requests = new HashMap<>();
    private int rid = 0;
    //private HashMap<Address, ReadListValue> readlist = new HashMap<>();
    //private boolean reading = false;
    private HashMap<Integer, Integer> rids = new HashMap<>();
    private HashMap<Integer, Integer> acks = new HashMap<>();
    private HashMap<Integer, Boolean> reading = new HashMap<>();
    private HashMap<Integer, Object> writeVals = new HashMap<>();
    private HashMap<Integer, Object> readVals = new HashMap<>();
    private HashMap<Integer, Integer> TSs = new HashMap<>();
    private HashMap<Integer, Integer> WRs = new HashMap<>();
    private HashMap<Integer, HashMap<Address, ReadListValue>> readlists = new HashMap<>();

    private boolean cas = false;
    private int N = config().getValue("id2203.project.replicationDegree", Integer.class);

    //******* Handlers ******
    protected final Handler<AR_Read_Request> readRequestHandler = new Handler<AR_Read_Request>() {

        @Override
        public void handle(AR_Read_Request readRequest) {
            LOG.info("Starting new read req.");
            rid++;
            Object readVal = null;
            Object writeVal = null;
            AtomicRequest ar = new AtomicRequest(readRequest.key, rid, 0, new HashMap<Address, ReadListValue>(), true, readVal, writeVal, 0, 0);
            requests.put(ar.key, ar);

            trigger(new BEB_Broadcast(self, new Read(self, ar.rid, readRequest.key, readRequest.id)), beb);
        }
    };

    /*
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
    */

    protected final Handler<AR_Write_Request> writeRequestHandler = new Handler<AR_Write_Request>() {

        @Override
        public void handle(AR_Write_Request writeRequest) {
            // Hvað ef tvö request koma inn og rid hækkar um 2 áður en AtomicRequest er búið til?
            rid++;
            increaseRid(writeRequest.key);
            AtomicRequest ar = new AtomicRequest(writeRequest.key, rid, 0, new HashMap<Address, ReadListValue>(), false, null, writeRequest.value, 0, 0);
            requests.put(ar.key, ar);
            trigger(new BEB_Broadcast(self, new Read(self, ar.rid, writeRequest.key, writeRequest.opId)), beb);
        }
    };

    protected final ClassMatchedHandler<Read, Message> readHandler = new ClassMatchedHandler<Read, Message>() {

        @Override
        public void handle(Read read, Message context) {
            AtomicRequest ar = requests.get(read.key);
            int ts = 0;
            int wr = 0;
            if (ar != null) {
                ts = ar.timestamp;
                wr = ar.wr;
            }
            trigger(new Message(self, read.src, new Value(self, read.rid, ts, wr, read.key, keyValueStore.get(read.key), read.opId)), net);
        }
    };

    protected final ClassMatchedHandler<Write, Message> writeHandler = new ClassMatchedHandler<Write, Message>() {

        @Override
        public void handle(Write write, Message context) {
            AtomicRequest ar = requests.get(write.key);
            int ts = 0;
            int wr = 0;
            if (ar != null) {
                ts = ar.timestamp;
                wr = ar.wr;
            } else {
                ar = new AtomicRequest(write.key, rid, 0, new HashMap<Address, ReadListValue>(), false, null, null, ts, wr);
            }
            if (isBigger(write.wr, write.ts, wr, ts)) {
                ar.timestamp = write.ts;
                ar.wr = write.wr;
                requests.put(ar.key, ar);
                keyValueStore.put(write.key, write.writeVal);
            }
            trigger(new Message(self, write.src, new Ack(self, write.rid, write.key, write.opId)), net);
        }
    };

    protected final ClassMatchedHandler<Value, Message> valueHandler = new ClassMatchedHandler<Value, Message>() {

        @Override
        public void handle(Value value, Message context) {
            AtomicRequest ar = requests.get(value.key);
            if(value.rid == ar.rid) {
                ar.readlist.put(value.src, new ReadListValue(value.ts, value.wr, value.value));
                if (ar.readlist.size() > N / 2) {
                    ReadListValue readListValue = Collections.max(ar.readlist.values());
                    ar.readlist.clear();
                    ar.readVal = readListValue.getValue();
                    Object broadcastval;
                    int maxtimestamp= readListValue.getTs();
                    int rr = readListValue.getWr();
                    if (ar.reading) {
                        broadcastval = ar.readVal;
                    } else {
                        maxtimestamp++;
                        rr = getRank(self);
                        if (cas) {
                            if (keyValueStore.get(value.key).equals(casReferenceVal)) {
                                casCode = Code.OK;
                                broadcastval = casNewVal;
                            } else {
                                casCode = Code.KEY_MISMATCH;
                                broadcastval = writeVals.get(value.key);
                            }

                        } else {
                            broadcastval = ar.writeVal;
                        }
                    }
                    requests.put(ar.key, ar);
                    trigger(new BEB_Broadcast(self, new Write(self, ar.rid, maxtimestamp, rr, value.key, broadcastval, value.opId)), beb);
                }
            }
        }
    };

    protected final ClassMatchedHandler<Ack, Message> ackHandler = new ClassMatchedHandler<Ack, Message>() {

        @Override
        public void handle(Ack ack, Message context) {
            AtomicRequest ar = requests.get(ack.key);
            if(ack.rid == ar.rid) {
                ar.acks = ar.acks + 1;
                if (ar.acks > N / 2) {
                    acks.put(ack.key, 0);
                    ar.acks = 0;
                    if (ar.reading) {
                        ar.reading = false;
                        //LOG.info("Finsihed reading and returning response for RID: " + rids.get(ack.key));
                        trigger(new AR_Read_Response(ar.readVal, ack.opId), nnar);
                        //requests.remove(request.rid);
                        requests.put(ar.key, ar);
                    } else if (cas) {
                        cas = false;
                        trigger(new AR_CAS_Response(ack.opId, casCode), nnar);
                    } else {
                        LOG.info("Apparently I'm writing...");
                        trigger(new AR_Write_Response(ack.opId), nnar);
                        requests.put(ar.key, ar);
                    }
                }
            }
        }
    };

    public void increaseRid(int key) {
        Object rid = rids.get(key);

        if (rid == null) {
            rids.put(key, 1);
        } else {
            rids.put(key, ((Integer) rid) + 1);
        }
    }

    public void increaseAck(int key) {
        Object ack = acks.get(key);

        if (ack == null) {
            acks.put(key, 1);
        } else {
            acks.put(key, ((Integer) ack) + 1);
        }
    }

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
        //subscribe(casRequestHandler, nnar);
        subscribe(readHandler, net);
        subscribe(writeHandler, net);
        subscribe(valueHandler, net);
        subscribe(ackHandler, net);
    }


}