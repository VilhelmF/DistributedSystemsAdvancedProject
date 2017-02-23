package se.kth.id2203.atomicregister;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.UUID;

public class AR_Write_Request implements KompicsEvent, Serializable {
    public final int key;
    public final Object value;
    public final UUID opId;

    public AR_Write_Request(int key, Object value, UUID opId)  {
        this.key = key;
        this.value = value;
        this.opId = opId;
    }
}
