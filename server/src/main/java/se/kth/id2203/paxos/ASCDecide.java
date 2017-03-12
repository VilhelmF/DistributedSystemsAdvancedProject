package se.kth.id2203.paxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by sindrikaldal on 27/02/17.
 */
public class ASCDecide implements KompicsEvent, Serializable {

    public final Propose propose;

    public ASCDecide(Propose propose) {
        this.propose = propose;
    }
}
