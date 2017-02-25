package se.kth.id2203.broadcasting;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by sindrikaldal on 25/02/17.
 */
public class BroadcastTest implements KompicsEvent, Serializable {

    public final NetAddress src;
    public final int key;

    public BroadcastTest(NetAddress src, int key) {
        this.src = src;
        this.key = key;
    }
}
