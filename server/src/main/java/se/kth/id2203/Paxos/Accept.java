package se.kth.id2203.Paxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Created by sindrikaldal on 27/02/17.
 */
public class Accept implements KompicsEvent, Serializable {

    public final int pts;
    public final List<Propose> values;
    public final int pvLength;
    public final int t;
    public final UUID id;

    public Accept(int pts, List<Propose> values, int pvLength, int t, UUID id) {
        this.pts = pts;
        this.values = values;
        this.pvLength = pvLength;
        this.t = t;
        this.id = id;
    }
}
