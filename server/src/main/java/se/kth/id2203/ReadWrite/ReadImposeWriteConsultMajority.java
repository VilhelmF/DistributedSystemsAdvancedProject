package se.kth.id2203.ReadWrite;

import se.kth.id2203.atomicregister.*;
import se.kth.id2203.broadcasting.BEB_Broadcast;
import se.kth.id2203.broadcasting.BestEffortBroadcast;
import se.kth.id2203.broadcasting.PL_Send;
import se.kth.id2203.broadcasting.PerfectLink;
import se.kth.id2203.core.ExercisePrimitives.AddressUtils;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Address;
import java.util.Collections;
import java.util.HashMap;

public class ReadImposeWriteConsultMajority extends ComponentDefinition {

    //******* Portimestamp******
    Negative<AtomicRegister> nnar = provides(AtomicRegister.class);
    Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    Positive<PerfectLink> pLink = requires(PerfectLink.class);

    //******* Fields ******
    final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    private int timestamp = 0;
    private int wr = 0;
    private Object value = null;
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
            rid++;
            acks = 0;
            readlist.clear();
            reading = true;
            trigger(new BEB_Broadcast(new Read(self, rid)), beb);
        }
    };

    protected final Handler<AR_Write_Request> writeRequestHandler = new Handler<AR_Write_Request>() {

        @Override
        public void handle(AR_Write_Request writeRequest) {
            rid++;
            writeVal = writeRequest.value;
            acks = 0;
            readlist.clear();
            trigger(new BEB_Broadcast(new Read(self, rid)), beb);
        }
    };

    protected final Handler<Read> bebDeliverReadHandler = new Handler<Read>() {

        @Override
        public void handle(Read read) {
            trigger(new PL_Send(read.src, new Value(read.src, read.rid, timestamp, wr, value)), pLink);
        }
    };

    protected final Handler<Write> bebDeliverWriteHandler = new Handler<Write>() {

        @Override
        public void handle(Write write) {
            if (isBigger(write.wr, write.ts, wr, timestamp)) {
                timestamp= write.ts;
                wr = write.wr;
                value = write.writeVal;
            }
            trigger(new PL_Send(write.src, new Ack(write.src, write.rid)), pLink);
        }
    };

    protected final Handler<Value> valueHandler = new Handler<Value>() {

        @Override
        public void handle(Value value) {
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
                    trigger(new BEB_Broadcast(new Write(self, rid, maxtimestamp, rr, broadcastval)), pLink);
                }
            }
        }
    };

    protected final Handler<Ack> ackHandler = new Handler<Ack>() {

        @Override
        public void handle(Ack ack) {
            if (ack.rid == rid) {
                acks++;
                if (acks > N / 2) {
                    acks = 0;
                    if (reading) {
                        reading = false;
                        trigger(new AR_Read_Response(readVal), nnar);
                    } else {
                        trigger(new AR_Write_Response(), nnar);
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
        return AddressUtils.toRank(address);
    }

    {
        subscribe(readRequestHandler, nnar);
        subscribe(writeRequestHandler, nnar);
        subscribe(bebDeliverReadHandler, beb);
        subscribe(bebDeliverWriteHandler, beb);
        subscribe(valueHandler, pLink);
        subscribe(ackHandler, pLink);
    }


}
