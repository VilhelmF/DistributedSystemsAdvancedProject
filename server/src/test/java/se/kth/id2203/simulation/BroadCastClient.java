package se.kth.id2203.simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.broadcasting.*;
import se.kth.id2203.kvstore.OpResponse;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.kth.id2203.overlay.LookupTable;
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

    protected final Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start start) {
            LookupTable lookupTable = new LookupTable();

            trigger(new TopologyMessage(lookupTable.getPartition(self)), beb);
            trigger(new BEB_Broadcast(self, new BroadcastTest(self, 0)), beb);
        }
    };

    protected final ClassMatchedHandler<BEB_Deliver, Message> responseHandler = new ClassMatchedHandler<BEB_Deliver, Message>() {

        @Override
        public void handle(BEB_Deliver content, Message context) {
            Object responsess = res.get("broadcast", String.class);
            if (responsess == null) {
                res.put("broadcast", "1");
            } else {
                int temp = Integer.parseInt((String)responsess) + 1;
                res.put("broadcast", Integer.toString(temp));
            }
        }
    };

    {
        subscribe(startHandler, control);
        subscribe(responseHandler, net);
    }
}
