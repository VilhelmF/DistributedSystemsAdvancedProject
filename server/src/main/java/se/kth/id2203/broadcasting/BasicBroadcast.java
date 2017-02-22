package se.kth.id2203.broadcasting;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.*;
import se.sics.kompics.network.*;

import java.util.Collection;

public class BasicBroadcast extends ComponentDefinition {


    final static Logger LOG = LoggerFactory.getLogger(BasicBroadcast.class);

        //******* Ports ******
        protected final Negative<BestEffortBroadcast> beb = provides(BestEffortBroadcast.class);
        protected final Positive<Network> net = requires(Network.class);

        //******* Fields ******
        private Collection<NetAddress> topology;

        protected final Handler<BEB_Deliver> broadcastMessageHandler = new Handler<BEB_Deliver>() {

            @Override
            public void handle(BEB_Deliver broadcastMessage) {
                /*
                for (NetAddress address : topology) {
                    trigger(new Message(broadcastMessage.src, address, broadcastMessage.payload), net);
                }
                */
            }
        };
    {
        subscribe(broadcastMessageHandler, beb);
    }

}