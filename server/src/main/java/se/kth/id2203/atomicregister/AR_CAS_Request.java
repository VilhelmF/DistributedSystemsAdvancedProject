package se.kth.id2203.atomicregister;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by sindrikaldal on 23/02/17.
 */
public class AR_CAS_Request implements KompicsEvent, Serializable {
    public final int key;
    public final Object referenceValue;
    public final Object newValue;
    public final UUID opId;

    public AR_CAS_Request(int key, Object referenceValue, Object newValue, UUID opId) {
        this.key = key;
        this.referenceValue = referenceValue;
        this.newValue = newValue;
        this.opId = opId;
    }
}
