package se.kth.id2203.broadcasting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.failuredetector.Restore;
import se.kth.id2203.failuredetector.Suspect;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;

import java.util.HashSet;
import java.util.NavigableSet;

/**
 * Created by sindrikaldal on 25/02/17.
 */
public class TestBroadcast extends ComponentDefinition{

    final static Logger LOG = LoggerFactory.getLogger(BasicBroadcast.class);
    final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);

    //******* Ports ******
    protected final Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    protected final Positive<Network> net = requires(Network.class);

    protected final Handler<BroadcastTest> testHandler = new Handler<BroadcastTest>() {
        @Override
        public void handle(BroadcastTest testMessage) {
            LOG.info("Received BroadcastTest");
            trigger(new Message(self, testMessage.src, new BEB_Deliver(testMessage)), net);
        }
    };

    {
        subscribe(testHandler, beb);
    }
}
