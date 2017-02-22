package se.kth.id2203.failuredetector;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by sindrikaldal on 22/02/17.
 */
public class HeartbeatReply implements KompicsEvent, Serializable {

    public final int seq;

    public HeartbeatReply(int seq) {
        this.seq = seq;
    }
}
