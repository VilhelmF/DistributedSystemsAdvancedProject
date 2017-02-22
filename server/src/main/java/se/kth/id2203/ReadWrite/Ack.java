package se.kth.id2203.ReadWrite;

import se.sics.kompics.KompicsEvent;
import se.sics.kompics.network.Address;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by sindrikaldal on 21/02/17.
 */
public class Ack implements KompicsEvent, Serializable {

    private static final long serialVersionUID = -5669973156747202337L;

    public final Address src;
    public final int rid;
    public final UUID opId;

    public Ack(Address src, int rid, UUID opId) {
        this.src = src;
        this.rid = rid;
        this.opId = opId;
    }
}
