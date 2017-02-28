package se.kth.id2203.Paxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.List;

/**
 * Created by sindrikaldal on 27/02/17.
 */
public class PrepareAck implements KompicsEvent, Serializable {

    public final int pts2;
    public final int ts;
    public final List<Object> vsuf;
    public final int l;
    public final int t2;

    public PrepareAck(int ts, int ats, List<Object> values, int al, int t) {
        this.pts2 = ts;
        this.ts = ats;
        this.vsuf = values;
        this.l = al;
        this.t2 = t;
    }
}
