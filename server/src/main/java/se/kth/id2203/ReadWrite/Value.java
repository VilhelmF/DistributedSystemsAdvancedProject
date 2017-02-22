package se.kth.id2203.ReadWrite;

import se.sics.kompics.KompicsEvent;
import se.sics.kompics.network.Address;

import java.io.Serializable;

/**
 * Created by sindrikaldal on 21/02/17.
 */
public class Value implements KompicsEvent, Serializable {

    public final Address src;
    public final int rid;
    public final int ts;
    public final int wr;
    public final int key;
    public final Object value;

    public Value(Address src, int rid, int ts, int wr, int key, Object value) {
        this.src = src;
        this.rid = rid;
        this.ts = ts;
        this.wr = wr;
        this.key = key;
        this.value = value;
    }
}
