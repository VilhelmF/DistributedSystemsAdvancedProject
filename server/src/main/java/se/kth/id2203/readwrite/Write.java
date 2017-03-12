package se.kth.id2203.readwrite;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.UUID;

public class Write implements KompicsEvent, Serializable {

    private static final long serialVersionUID = -5669973156467232337L;

    public NetAddress src;
    public final int rid;
    public final int ts;
    public final int wr;
    public final int key;
    public final Object writeVal;
    public final UUID opId;

    public Write(NetAddress src, int rid, int ts, int wr, int key, Object writeVal, UUID opId) {
        this.src = src;
        this.rid = rid;
        this.ts = ts;
        this.wr = wr;
        this.key = key;
        this.writeVal = writeVal;
        this.opId = opId;
    }
}
