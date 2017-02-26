package se.kth.id2203.failuredetector;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by vilhelm on 2017-02-26.
 */
public class Topology_Change implements KompicsEvent, Serializable {


    public final int change;

    public Topology_Change(int change) {
        this.change = change;
    }
}
