package se.kth.id2203.failuredetector;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by sindrikaldal on 22/02/17.
 */
public class HeartbeatRequest implements KompicsEvent, Serializable {

    public final int seq;

    public HeartbeatRequest(int seq) {
        this.seq = seq;
    }
}
