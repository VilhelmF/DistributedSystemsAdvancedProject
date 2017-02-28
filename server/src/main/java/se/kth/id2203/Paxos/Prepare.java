package se.kth.id2203.Paxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by sindrikaldal on 27/02/17.
 */
public class Prepare implements KompicsEvent, Serializable {

    public final int ts;
    public final int l;
    public final int t2;
    public final UUID proposeUUID;

    public Prepare(int pts, int al, int t, UUID id) {
        this.ts = pts;
        this.l = al;
        this.t2 = t;
        this.proposeUUID = id;
    }
}
