package se.kth.id2203.atomicregister;

import se.kth.id2203.kvstore.OpResponse.Code;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by sindrikaldal on 23/02/17.
 */
public class AR_CAS_Response implements KompicsEvent, Serializable {

    public final UUID opId;
    public final Code opStatus;

    public AR_CAS_Response(UUID opId, Code opStatus) {
        this.opId = opId;
        this.opStatus = opStatus;
    }
}
