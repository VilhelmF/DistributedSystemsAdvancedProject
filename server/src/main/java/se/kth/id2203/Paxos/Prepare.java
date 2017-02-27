package se.kth.id2203.Paxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by sindrikaldal on 27/02/17.
 */
public class Prepare implements KompicsEvent, Serializable {

    public final int pts;
    public final int al;
    public final int t;

    public Prepare(int pts, int al, int t) {
        this.pts = pts;
        this.al = al;
        this.t = t;
    }
}
