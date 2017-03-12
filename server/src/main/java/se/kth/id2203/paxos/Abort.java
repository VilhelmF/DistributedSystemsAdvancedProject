package se.kth.id2203.paxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by sindrikaldal on 27/02/17.
 */
public class Abort implements KompicsEvent, Serializable {

    public final UUID id;

    public Abort(UUID id) {
        this.id = id;
    }
}
