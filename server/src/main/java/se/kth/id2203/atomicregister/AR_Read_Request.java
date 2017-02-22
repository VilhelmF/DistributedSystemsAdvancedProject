package se.kth.id2203.atomicregister;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.UUID;

public class AR_Read_Request implements KompicsEvent, Serializable {


    public final int key;
    public final UUID id;

    public AR_Read_Request(int key, UUID id) {
        this.key = key;
        this.id = id;
    }
}
