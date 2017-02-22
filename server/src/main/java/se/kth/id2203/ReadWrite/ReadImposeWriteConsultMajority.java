package se.kth.id2203.ReadWrite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.atomicregister.*;
import se.kth.id2203.broadcasting.*;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Address;
import java.util.Collections;
import java.util.HashMap;

public class ReadImposeWriteConsultMajority extends ComponentDefinition {

    //******* Ports ******
    Negative<AtomicRegister> nnar = provides(AtomicRegister.class);
    Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    Positive<PerfectLink> pLink = requires(PerfectLink.class);

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
            trigger(new BEB_Broadcast(self, new Read(self, rid, writeRequest.key, null)), beb);
        }
    };

    /*
    protected final Handler<Read> bebDeliverReadHandler = new Handler<Read>() {

        @Override
        public void handle(Read read) {
            trigger(new PL_Send(read.src, new Value(read.src, read.rid, timestamp, wr, read.key, keyValueStore.get(read.key), read.opId)), pLink);
        }
    };
    */

    protected final Handler<BEB_Deliver> bebDeliverHandler = new Handler<BEB_Deliver>() {

        @Override
        public void handle(BEB_Deliver beb_deliver) {

            if (beb_deliver.payload instanceof Read) {
                Read read = (Read) beb_deliver.payload;
                trigger(new PL_Send(read.src, new Value(read.src, read.rid, timestamp, wr, read.key, keyValueStore.get(read.key), read.opId)), pLink);
            } else if (beb_deliver.payload instanceof Write){

                Write write = (Write) beb_deliver.payload;

                if (isBigger(write.wr, write.ts, wr, timestamp)) {
                    timestamp= write.ts;
                    wr = write.wr;
                    keyValueStore.put(write.key, write.writeVal);
                }
                trigger(new PL_Send(write.src, new Ack(write.src, write.rid, write.opId)), pLink);
            }


        }
    };

    protected final Handler<PL_Deliver> plDeliverHandler = new Handler<PL_Deliver>() {

        @Override
        public void handle(PL_Deliver pl_deliver) {
            if (pl_deliver.payload instanceof Value) {
                Value value = (Value) pl_deliver.payload;
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
                        trigger(new BEB_Broadcast(self, new Write(self, rid, maxtimestamp, rr, value.key, broadcastval, value.opId)), pLink);
                    }
                }
            }
            else if (pl_deliver.payload instanceof Ack) {
                Ack ack = (Ack) pl_deliver.payload;
                if (ack.rid == rid) {
                    acks++;
                    if (acks > N / 2) {
                        acks = 0;
                        if (reading) {
                            reading = false;
                            trigger(new AR_Read_Response(readVal, ack.opId), nnar);
                        } else {
                            trigger(new AR_Write_Response(ack.opId), nnar);
                        }
                    }
                }
            }

        }
    };

    /*
    protected final Handler<Ack> ackHandler = new Handler<Ack>() {

        @Override
        public void handle(Ack ack) {
            if (ack.rid == rid) {
                acks++;
                if (acks > N / 2) {
                    acks = 0;
                    if (reading) {
                        reading = false;
                        trigger(new AR_Read_Response(readVal, ack.opId), nnar);
                    } else {
                        trigger(new AR_Write_Response(ack.opId), nnar);
                    }
                }
            }
        }
    };
    */

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
        subscribe(bebDeliverHandler, beb);
        //subscribe(bebDeliverWriteHandler, beb);
        subscribe(plDeliverHandler, pLink);
        //subscribe(ackHandler, pLink);
    }


}
