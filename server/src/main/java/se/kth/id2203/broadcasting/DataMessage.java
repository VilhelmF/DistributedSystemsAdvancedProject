package se.kth.id2203.broadcasting;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by sindrikaldal on 23/02/17.
 */
public class DataMessage implements KompicsEvent, Serializable {

    public final VectorClock vec;
    public final KompicsEvent payload;

    public DataMessage(VectorClock vec, KompicsEvent payload) {
        this.vec = vec;
        this.payload = payload;
    }
}
