package se.kth.id2203.broadcasting;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

public class CRB_Broadcast implements KompicsEvent, Serializable {

    public final NetAddress src;
    public final KompicsEvent payload;

    public CRB_Broadcast(NetAddress src, KompicsEvent payload) {
        this.src = src;
        this.payload = payload;
    }
}
