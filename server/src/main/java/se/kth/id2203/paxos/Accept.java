package se.kth.id2203.paxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Created by sindrikaldal on 27/02/17.
 */
public class Accept implements KompicsEvent, Serializable {

    public final int ts;
    public final List<Propose> vsuf;
    public final int offs;
    public final int t2;
    public final UUID id;

    public Accept(int pts, List<Propose> values, int pvLength, int t, UUID id) {
        this.ts = pts;
        this.vsuf = values;
        this.offs = pvLength;
        this.t2 = t;
        this.id = id;
    }
}
