package se.kth.id2203.broadcasting;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.network.Address;

import java.io.Serializable;

/**
 * Created by sindrikaldal on 21/02/17.
 */
public class BEB_Deliver implements KompicsEvent, Serializable {


    public final KompicsEvent payload;

    public BEB_Deliver(KompicsEvent payload) {
        this.payload = payload;
    }
}
