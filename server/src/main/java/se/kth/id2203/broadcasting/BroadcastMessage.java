package se.kth.id2203.broadcasting;

import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.network.Address;

import java.util.Collection;

/**
 * Created by sindrikaldal on 18/02/17.
 */
public class BroadcastMessage implements KompicsEvent {

    public final NetAddress src;
    public final KompicsEvent msg;
    public final Collection<NetAddress> recipients;

    public BroadcastMessage(NetAddress src, KompicsEvent msg, Collection<NetAddress> recipients) {
        this.src = src;
        this.msg = msg;
        this.recipients = recipients;
    }
}
