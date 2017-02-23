package se.kth.id2203.ReadWrite;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by sindrikaldal on 23/02/17.
 */
public class CAS implements KompicsEvent, Serializable {

    private static final long serialVersionUID = -5669433156467202337L;

    public final NetAddress src;
    public final int rid;
    public final int ts;
    public final int wr;
    public final int key;
    public final Object referenceValue;
    public final Object newValue;
    public final UUID opId;

    public CAS(NetAddress src, int rid, int ts, int wr, int key, Object referenceValue, Object newValue, UUID opId) {
        this.src = src;
        this.rid = rid;
        this.ts = ts;
        this.wr = wr;
        this.key = key;
        this.referenceValue = referenceValue;
        this.newValue = newValue;
        this.opId = opId;
    }
}
