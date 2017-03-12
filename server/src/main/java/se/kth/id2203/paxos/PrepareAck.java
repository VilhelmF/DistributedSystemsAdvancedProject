package se.kth.id2203.paxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Created by sindrikaldal on 27/02/17.
 */
public class PrepareAck implements KompicsEvent, Serializable {

    public final int pts2;
    public final int ts;
    public final List<Propose> vsuf;
    public final int l;
    public final int t2;
    public final UUID id;

    public PrepareAck(int pts2, int ts, List<Propose> vsuf, int l, int t2, UUID id) {
        this.pts2 = pts2;
        this.ts = ts;
        this.vsuf = vsuf;
        this.l = l;
        this.t2 = t2;
        this.id = id;
    }


}
