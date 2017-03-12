package se.kth.id2203.readwrite;

import se.sics.kompics.KompicsEvent;
import se.sics.kompics.network.Address;

import java.io.Serializable;
import java.util.UUID;

public class Value implements KompicsEvent, Serializable {


    private static final long serialVersionUID = -5639973156467202337L;

    public final Address src;
    public final int rid;
    public final int ts;
    public final int wr;
    public final int key;
    public final Object value;
    public final UUID opId;

    public Value(Address src, int rid, int ts, int wr, int key, Object value, UUID opId) {
        this.src = src;
        this.rid = rid;
        this.ts = ts;
        this.wr = wr;
        this.key = key;
        this.value = value;
        this.opId = opId;
    }
}
