package se.kth.id2203.broadcasting;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by sindrikaldal on 23/02/17.
 */
public class CRB_Deliver implements KompicsEvent, Serializable {

    public final NetAddress src;
    public final KompicsEvent payload;

    public CRB_Deliver(NetAddress src, KompicsEvent payload) {
        this.src = src;
        this.payload = payload;
    }
}
