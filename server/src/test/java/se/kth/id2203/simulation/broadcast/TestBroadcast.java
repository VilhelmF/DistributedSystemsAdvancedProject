package se.kth.id2203.simulation.broadcast;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.broadcasting.BEB_Broadcast;
import se.kth.id2203.broadcasting.BestEffortBroadcast;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Address;
import se.sics.kompics.network.Network;
import se.sics.kompics.simulator.util.GlobalView;

import java.util.HashSet;
import java.util.NavigableSet;

/**
 * Created by sindrikaldal on 26/02/17.
 */
public class TestBroadcast extends ComponentDefinition {


    final static Logger LOG = LoggerFactory.getLogger(TestBroadcast.class);

    //******* Ports ******
    protected final Negative<BestEffortBroadcast> beb = provides(BestEffortBroadcast.class);
    protected final Positive<Network> net = requires(Network.class);

    protected final Handler<BEB_Broadcast> broadcastMessageHandler = new Handler<BEB_Broadcast>() {

        @Override
        public void handle(BEB_Broadcast broadcastMessage) {

            GlobalView gv = config().getValue("simulation.globalview", GlobalView.class);

            for (Address address : gv.getAliveNodes().values()) {
                LOG.info("Triggering message baby");
                trigger(new Message(broadcastMessage.src, new NetAddress(address.getIp(), address.getPort()), broadcastMessage.payload), net);
            }
        }
    };

    {
        subscribe(broadcastMessageHandler, beb);
    }
}
