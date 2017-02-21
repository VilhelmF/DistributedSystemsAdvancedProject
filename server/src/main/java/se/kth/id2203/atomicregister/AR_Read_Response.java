package se.kth.id2203.atomicregister;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by sindrikaldal on 21/02/17.
 */
public class AR_Read_Response implements KompicsEvent, Serializable {

    public final Object value;

    public AR_Read_Response(Object value) {
        this.value = value;
    }
}
