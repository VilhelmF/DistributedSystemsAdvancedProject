package se.kth.id2203.ReadWrite;

import se.sics.kompics.network.Address;

import java.util.HashMap;

/**
 * Created by Vilhelm on 24.2.2017.
 */
public class AtomicRequest {

    public int rid;
    public int wr;
    public int ts;
    public int acks;
    public HashMap<Address, ReadListValue> readlist;
    public boolean reading;
    public Object readVal;
    public Object writeVal;


    public AtomicRequest(int rid, int wr, int ts, int acks, HashMap<Address, ReadListValue> readlist, boolean reading, Object readVal, Object writeVal) {
        this.rid = rid;
        this.wr = wr;
        this.ts = ts;
        this.acks = acks;
        this.readlist = readlist;
        this.reading = reading;
        this.readVal = readVal;
        this.writeVal = writeVal;
    }
}

