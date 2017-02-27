package se.kth.id2203.Paxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by sindrikaldal on 27/02/17.
 */
public class Nack implements KompicsEvent, Serializable {

    public final int pts;
    public final int t;

    public Nack(int pts, int t) {
        this.pts = pts;
        this.t = t;
    }
}
