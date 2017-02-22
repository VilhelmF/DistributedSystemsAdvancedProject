package se.kth.id2203.broadcasting;


import javafx.geometry.Pos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;

import java.util.Collection;
import java.util.NavigableSet;


public class BasicBroadcast extends ComponentDefinition {


    final static Logger LOG = LoggerFactory.getLogger(BasicBroadcast.class);

        //******* Ports ******
        protected final Negative<BestEffortBroadcast> beb = provides(BestEffortBroadcast.class);
        protected final Positive<PerfectLink> pLink = requires(PerfectLink.class);
        protected final Positive<Network> net = requires(Network.class);

        //******* Fields ******
        private NavigableSet<NetAddress> topology;

        protected final Handler<BEB_Broadcast> broadcastMessageHandler = new Handler<BEB_Broadcast>() {

            @Override
            public void handle(BEB_Broadcast broadcastMessage) {

                LOG.info("Broadcasting message from BasicBroadcast component");
                for (NetAddress address : topology) {
                    LOG.info(address.toString());
                    trigger(new Message(broadcastMessage.src, address, broadcastMessage.payload), net);
                }
            }
        };

        protected final Handler<TopologyMessage> topologyHandler = new Handler<TopologyMessage>() {
            @Override
            public void handle(TopologyMessage topologyMessage) {

                LOG.info("Received new topology");
                topology = topologyMessage.topology;

            }
        };
    {
        subscribe(broadcastMessageHandler, beb);
        subscribe(topologyHandler, beb);
    }

}