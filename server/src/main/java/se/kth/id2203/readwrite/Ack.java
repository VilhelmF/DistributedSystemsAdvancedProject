package se.kth.id2203.readwrite;

import se.sics.kompics.KompicsEvent;
import se.sics.kompics.network.Address;

import java.io.Serializable;
import java.util.UUID;

public class Ack implements KompicsEvent, Serializable {

    private static final long serialVersionUID = -5669973156747202337L;

    public final Address src;
    public final int rid;
    public  int key;
    public final UUID opId;

    public Ack(Address src, int rid, int key, UUID opId) {
        this.src = src;
        this.rid = rid;
        this.key = key;
        this.opId = opId;
        this.key = key;
    }
}
