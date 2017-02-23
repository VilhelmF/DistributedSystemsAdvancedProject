package se.kth.id2203.atomicregister;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.UUID;

public class AR_Write_Response implements KompicsEvent, Serializable {

    public final UUID opId;

    public AR_Write_Response(UUID opId) {
        this.opId = opId;
    }
}
