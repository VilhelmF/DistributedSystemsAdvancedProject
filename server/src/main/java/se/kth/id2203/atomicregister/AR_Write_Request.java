package se.kth.id2203.atomicregister;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by sindrikaldal on 21/02/17.
 */
public class AR_Write_Request implements KompicsEvent, Serializable {
    public final Object value;

    public AR_Write_Request(Object value) {
        this.value = value;
    }
}