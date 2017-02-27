package se.kth.id2203.Paxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.List;

/**
 * Created by sindrikaldal on 27/02/17.
 */
public class PrepareAck implements KompicsEvent, Serializable {

    public final int ts;
    public final int ats;
    public final List<Object> values;
    public final int al;
    public final int t;

    public PrepareAck(int ts, int ats, List<Object> values, int al, int t) {
        this.ts = ts;
        this.ats = ats;
        this.values = values;
        this.al = al;
        this.t = t;
    }
}
