package se.kth.id2203.atomicregister;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.UUID;

public class AR_Read_Response implements KompicsEvent, Serializable {

    public final Object value;
    public final UUID id;

    public AR_Read_Response(Object value, UUID id) {
        this.value = value;
        this.id = id;
    }
}
