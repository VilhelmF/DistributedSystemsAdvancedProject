package se.kth.id2203.broadcasting;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.Collection;

/**
 * Created by sindrikaldal on 22/02/17.
 */
public class TopologyMessage implements KompicsEvent, Serializable{

    public final Collection<NetAddress> topology;

    public TopologyMessage(Collection<NetAddress> topology) {
        this.topology = topology;
    }
}
