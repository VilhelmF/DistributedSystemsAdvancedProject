package se.kth.id2203.broadcasting;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.failuredetector.Restore;
import se.kth.id2203.failuredetector.Suspect;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;

import java.util.HashSet;
import java.util.NavigableSet;


public class BasicBroadcast extends ComponentDefinition {


    final static Logger LOG = LoggerFactory.getLogger(BasicBroadcast.class);

        //******* Ports ******
        protected final Negative<BestEffortBroadcast> beb = provides(BestEffortBroadcast.class);
        protected final Positive<Network> net = requires(Network.class);

        //******* Fields ******
        private NavigableSet<NetAddress> topology;
        private HashSet<NetAddress> suspcected = new HashSet<>();

        protected final Handler<BEB_Broadcast> broadcastMessageHandler = new Handler<BEB_Broadcast>() {

            @Override
            public void handle(BEB_Broadcast broadcastMessage) {

                LOG.info("BB: Broadcasting message from BasicBroadcast component");
                for (NetAddress address : topology) {
                    LOG.info("BB: Broadcasting to : " + address.toString());
                    if (!suspcected.contains(address)) {
                        trigger(new Message(broadcastMessage.src, address, broadcastMessage.payload), net);
                    }
                }
            }
        };

        protected final Handler<TopologyMessage> topologyHandler = new Handler<TopologyMessage>() {
            @Override
            public void handle(TopologyMessage topologyMessage) {
                LOG.info("BB : Received new topology of size : " + topologyMessage.topology.size());
                topology = topologyMessage.topology;

            }
        };

        protected final Handler<Suspect> suspectHandler = new Handler<Suspect>() {

            @Override
            public void handle(Suspect event) {
                LOG.info("BB: Suspecting " + event.process.toString());
                suspcected.add(event.process);
            }
        };

        protected final Handler<Restore> restoreHandler = new Handler<Restore>() {

            @Override
            public void handle(Restore event) {
                LOG.info("BB: Restoring " + event.process.toString() + " as a suspect");
                suspcected.remove(event.process);
            }
        };
    {
        subscribe(broadcastMessageHandler, beb);
        subscribe(topologyHandler, beb);
        subscribe(suspectHandler, beb);
        subscribe(restoreHandler, beb);
    }

}