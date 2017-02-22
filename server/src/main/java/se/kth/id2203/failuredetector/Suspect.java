package se.kth.id2203.failuredetector;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by sindrikaldal on 22/02/17.
 */
public class Suspect implements KompicsEvent, Serializable {

    public final NetAddress process;

    public Suspect(NetAddress process) {
        this.process = process;
    }
}
