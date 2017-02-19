package se.kth.id2203.broadcasting;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.*;
import se.sics.kompics.network.*;

public class BasicBroadcast extends ComponentDefinition {

    final static Logger LOG = LoggerFactory.getLogger(BasicBroadcast.class);

        protected final Negative<Broadcasting> broadcast = provides(Broadcasting.class);
        protected final Positive<Network> net = requires(Network.class);

        protected final Handler<BroadcastMessage> broadcastMessageHandler = new Handler<BroadcastMessage>() {

            @Override
            public void handle(BroadcastMessage broadcastMessage) {

                LOG.info("Broadcast: Received message. NIIIIIIIIIIIIIIIIIIIIIICE {}", broadcastMessage.recipients.size());

                for (NetAddress address : broadcastMessage.recipients) {
                    trigger(new Message(broadcastMessage.src, address, broadcastMessage.msg), net);
                }
            }
        };


    {
        subscribe(broadcastMessageHandler, broadcast);
    }
}