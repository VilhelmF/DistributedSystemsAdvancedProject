package se.kth.id2203.Paxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by sindrikaldal on 27/02/17.
 */
public class ASCDecide implements KompicsEvent, Serializable {
    public final Object obj;

    public ASCDecide(Object obj) {
        this.obj = obj;
    }
}
