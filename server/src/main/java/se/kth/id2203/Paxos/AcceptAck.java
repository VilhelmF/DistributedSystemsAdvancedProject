package se.kth.id2203.Paxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.List;

/**
 * Created by sindrikaldal on 27/02/17.
 */
public class AcceptAck implements KompicsEvent, Serializable {

    public final int pts;
    public final int l;
    public final int t;

    public AcceptAck(int pts, int l, int t) {
        this.pts = pts;
        this.l = l;
        this.t = t;
    }
}
