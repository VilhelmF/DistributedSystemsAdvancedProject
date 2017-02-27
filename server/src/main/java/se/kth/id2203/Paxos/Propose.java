package se.kth.id2203.Paxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.List;

/**
 * Created by sindrikaldal on 27/02/17.
 */
public class Propose implements KompicsEvent, Serializable {

    public final List<Object> values;

    public Propose(List<Object> values) {
        this.values = values;
    }
}
