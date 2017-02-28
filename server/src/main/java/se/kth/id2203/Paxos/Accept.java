package se.kth.id2203.Paxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.List;

/**
 * Created by sindrikaldal on 27/02/17.
 */
public class Accept implements KompicsEvent, Serializable {

    public final int pts;
    public final List<Object> values;
    public final int pvLength;
    public final int t;

    public Accept(int pts, List<Object> values, int pvLength, int t) {
        this.pts = pts;
        this.values = values;
        this.pvLength = pvLength;
        this.t = t;
    }
}
