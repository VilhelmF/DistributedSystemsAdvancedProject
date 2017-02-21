package se.kth.id2203.ReadWrite;

import se.kth.id2203.atomicregister.AR_Read_Request;
import se.kth.id2203.atomicregister.AR_Write_Request;
import se.kth.id2203.atomicregister.AtomicRegister;
import se.kth.id2203.broadcasting.BEB_Broadcast;
import se.kth.id2203.broadcasting.BestEffortBroadcast;
import se.kth.id2203.broadcasting.PL_Send;
import se.kth.id2203.broadcasting.PerfectLink;
import se.kth.id2203.core.Ports;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Address;

import java.util.HashMap;

/**
 * Created by sindrikaldal on 21/02/17.
 */
public class ReadImposeWriteConsultMajority extends ComponentDefinition {

    //******* Ports ******
    Negative<AtomicRegister> nnar = provides(AtomicRegister.class);
    Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    Positive<PerfectLink> pLink = requires(PerfectLink.class);

    //******* Fields ******
    final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    private int ts = 0;
    private int wr = 0;
    private Object value = null;
    private int acks = 0;
    private Object readVal = null;
    private Object writeVal = null;
    private int rid = 0;
    private HashMap<Address, ReadListValue> readlist = new HashMap<>();
    private boolean reading = false;
    private int N = 10; //TODO. Hvernig finnum við N

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
            trigger(new PL_Send(read.src, new Value(read.src, read.rid, ts, wr, value)), pLink);
        }
    };

    protected final Handler<Write> bebDeliverWriteHandler = new Handler<Write>() {

        @Override
        public void handle(Write write) {
            if (isBigger(write.wr, write.ts, wr, ts)) {
                ts = write.ts;
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
                    readlist.clear();
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

    {
        subscribe(readRequestHandler, nnar);
        subscribe(writeRequestHandler, nnar);
        subscribe(bebDeliverReadHandler, beb);
        subscribe(bebDeliverWriteHandler, beb);
        subscribe(valueHandler, pLink);
    }


}
