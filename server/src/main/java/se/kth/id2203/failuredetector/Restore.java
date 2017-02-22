package se.kth.id2203.failuredetector;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.network.Address;

import java.io.Serializable;

/**
 * Created by sindrikaldal on 22/02/17.
 */
public class Restore implements KompicsEvent, Serializable {

    public final NetAddress process;

    public Restore(NetAddress process) {
        this.process = process;
    }
}
