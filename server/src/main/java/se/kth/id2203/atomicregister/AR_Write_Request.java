package se.kth.id2203.atomicregister;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by sindrikaldal on 21/02/17.
 */
public class AR_Write_Request implements KompicsEvent, Serializable {
    public final int key;
    public final Object value;

    public AR_Write_Request(int key, Object value)  {
        this.key = key;
        this.value = value;
    }
}
