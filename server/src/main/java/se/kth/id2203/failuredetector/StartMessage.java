package se.kth.id2203.failuredetector;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.NavigableSet;

/**
 * Created by sindrikaldal on 23/02/17.
 */
public class StartMessage implements KompicsEvent, Serializable {

    public final NavigableSet<NetAddress> topology;

    public StartMessage(NavigableSet<NetAddress> topology) {
        this.topology = topology;
    }
}
