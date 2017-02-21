package se.kth.id2203.ReadWrite;

import se.sics.kompics.KompicsEvent;
import se.sics.kompics.network.Address;

import java.io.Serializable;

/**
 * Created by sindrikaldal on 21/02/17.
 */
public class Ack implements KompicsEvent, Serializable {

    public final Address src;
    public final int rid;

    public Ack(Address src, int rid) {
        this.src = src;
        this.rid = rid;
    }
}
