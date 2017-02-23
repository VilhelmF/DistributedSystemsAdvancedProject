package se.kth.id2203.broadcasting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;

import java.util.HashSet;

/**
 * Created by sindrikaldal on 23/02/17.
 */
public class EagerReliableBroadcast extends ComponentDefinition {

    final static Logger LOG = LoggerFactory.getLogger(BasicBroadcast.class);

    //******* Ports ******
    protected final Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    protected final Negative<ReliableBroadcast> rb = provides(ReliableBroadcast.class);

    //******* Fields ******
    private HashSet<KompicsEvent> delivered = new HashSet<>();

    protected final Handler<RB_Broadcast> rbBroadcastHandler = new Handler<RB_Broadcast>() {

        @Override
        public void handle(RB_Broadcast broadcastMessage) {
            trigger(new BEB_Broadcast(broadcastMessage.src, new OriginatedData(broadcastMessage.src, broadcastMessage.payload)), beb);
        }
    };

    protected final ClassMatchedHandler<OriginatedData, Message> bebDeliverHandler = new ClassMatchedHandler<OriginatedData, Message>() {

        @Override
        public void handle(OriginatedData data, Message context) {
            if (!delivered.contains(data.payload)) {
                delivered.add(data.payload);
                trigger(new RB_Deliver(data.src, data.payload), rb);
                trigger(new BEB_Broadcast(context.getSource(), data), beb);
            }
        }
    };

    {
        subscribe(rbBroadcastHandler, rb);
        subscribe(bebDeliverHandler, beb);
    }



}
