package se.kth.id2203.Paxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by sindrikaldal on 27/02/17.
 */
public class Prepare implements KompicsEvent, Serializable {

    public final int ts;
    public final int l;
    public final int t2;

    public Prepare(int pts, int al, int t) {
        this.ts = pts;
        this.l = al;
        this.t2 = t;
    }
}
