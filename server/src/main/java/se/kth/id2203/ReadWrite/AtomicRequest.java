package se.kth.id2203.ReadWrite;

import se.sics.kompics.network.Address;

import java.util.HashMap;

/**
 * Created by Vilhelm on 24.2.2017.
 */
public class AtomicRequest {

    public int timestamp = 0;
    public int wr = 0;
    public int rid;
    public int acks;
    public HashMap<Address, ReadListValue> readlist;
    public boolean reading;
    public Object readVal;
    public Object writeVal;
    public int key;



    public AtomicRequest(int key, int rid, int acks, HashMap<Address, ReadListValue> readlist, boolean reading, Object readVal, Object writeVal, int timestamp,
                         int wr)
    {
        this.key = key;
        this.rid = rid;
        this.acks = acks;
        this.readlist = readlist;
        this.reading = reading;
        this.readVal = readVal;
        this.writeVal = writeVal;
        this.timestamp = timestamp;
        this.wr = wr;
    }
}

