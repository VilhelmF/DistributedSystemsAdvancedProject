package se.kth.id2203.broadcasting;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.Collection;
import java.util.NavigableSet;

/**
 * Created by sindrikaldal on 22/02/17.
 */
public class TopologyMessage implements KompicsEvent, Serializable{

    public final NavigableSet<NetAddress> topology;

    public TopologyMessage(NavigableSet<NetAddress> topology) {
        this.topology = topology;
    }
}
