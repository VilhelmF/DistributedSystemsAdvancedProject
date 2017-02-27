package se.kth.id2203.Paxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by sindrikaldal on 27/02/17.
 */
public class Decide implements KompicsEvent, Serializable {

    public final int ts;
    public final int l;
    public final int t;

    public Decide(int ts, int l, int t) {
        this.ts = ts;
        this.l = l;
        this.t = t;
    }
}
