package se.kth.id2203.simulation.broadcast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.broadcasting.*;
import se.kth.id2203.kvstore.OpResponse;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.kth.id2203.overlay.LookupTable;
import se.kth.id2203.simulation.PutClient;
import se.kth.id2203.simulation.SimulationResultMap;
import se.kth.id2203.simulation.SimulationResultSingleton;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;
import sun.nio.ch.Net;

import java.util.*;

/**
 * Created by sindrikaldal on 25/02/17.
 */
public class BroadCastClient extends ComponentDefinition {

    final static Logger LOG = LoggerFactory.getLogger(PutClient.class);
    //******* Ports ******
    protected final Positive<Network> net = requires(Network.class);
    protected final Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    protected final Positive<Timer> timer = requires(Timer.class);
    //******* Fields ******
    private final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    private final NetAddress server = config().getValue("id2203.project.bootstrap-address", NetAddress.class);
    private final SimulationResultMap res = SimulationResultSingleton.getInstance();
    private final Map<UUID, String> pending = new TreeMap<>();


    protected final ClassMatchedHandler<BroadcastMessage, Message> broadcastHandler = new ClassMatchedHandler<BroadcastMessage, Message>() {

        @Override
        public void handle(BroadcastMessage content, Message context) {
            LOG.info("Received BEB_Broadcast");
            trigger(new BEB_Broadcast(self, new BEB_Deliver(null)), beb);
        }
    };

    protected final ClassMatchedHandler<BEB_Deliver, Message> responseHandler = new ClassMatchedHandler<BEB_Deliver, Message>() {

        @Override
        public void handle(BEB_Deliver content, Message context) {
            LOG.info("Got BEB_DELIVER at : " + self.toString());
            String num = res.get("broadcast", String.class);
            if (num == null) {
                res.put("broadcast", "1");
            } else {
                res.put("broadcast", Integer.toString(Integer.parseInt(num) + 1));
            }
        }
    };

    {
        subscribe(broadcastHandler, net);
        subscribe(responseHandler, net);
    }
}
