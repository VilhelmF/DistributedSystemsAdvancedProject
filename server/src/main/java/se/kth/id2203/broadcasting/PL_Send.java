package se.kth.id2203.broadcasting;

import se.sics.kompics.KompicsEvent;
import se.sics.kompics.network.Address;

import java.io.Serializable;

/**
 * Created by sindrikaldal on 21/02/17.
 */
public class PL_Send implements KompicsEvent, Serializable {

    public final Address src;
    public final KompicsEvent payload;

    public PL_Send(Address src, KompicsEvent payload) {
        this.src = src;
        this.payload = payload;
    }
}
