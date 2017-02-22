package se.kth.id2203.ReadWrite;

import se.sics.kompics.KompicsEvent;
import se.sics.kompics.network.Address;

/**
 * Created by sindrikaldal on 21/02/17.
 */
public class Write implements KompicsEvent {

    public Address src;
    public final int rid;
    public final int ts;
    public final int wr;
    public final int key;
    public final Object writeVal;

    public Write(Address src, int rid, int ts, int wr, int key, Object writeVal) {
        this.src = src;
        this.rid = rid;
        this.ts = ts;
        this.wr = wr;
        this.key = key;
        this.writeVal = writeVal;
    }
}
