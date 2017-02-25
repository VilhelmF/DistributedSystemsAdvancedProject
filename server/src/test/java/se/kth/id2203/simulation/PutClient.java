package se.kth.id2203.simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.kvstore.GetOperation;
import se.kth.id2203.kvstore.OpResponse;
import se.kth.id2203.kvstore.PutOperation;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.kth.id2203.overlay.RouteMsg;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;

import java.util.*;

/**
 * Created by Vilhelm on 25.2.2017.
 */
public class PutClient extends ComponentDefinition {

    final static Logger LOG = LoggerFactory.getLogger(PutClient.class);
    //******* Ports ******
    protected final Positive<Network> net = requires(Network.class);
    protected final Positive<Timer> timer = requires(Timer.class);
    //******* Fields ******
    private final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    private final NetAddress server = config().getValue("id2203.project.bootstrap-address", NetAddress.class);
    private final SimulationResultMap res = SimulationResultSingleton.getInstance();
    private final Map<UUID, String> pending = new TreeMap<>();
    private List<UUID> putID = new ArrayList<>();
    private List<UUID> getID = new ArrayList<>();
    //******* Handlers ******
    protected final Handler<Start> startHandler = new Handler<Start>() {

        @Override
        public void handle(Start event) {
            int messages = res.get("messages", Integer.class);

            for (int i = 0; i < messages; i++) {
                PutOperation op = new PutOperation("" + i, "Value: " + i);
                LOG.info("OP id: " + op.id);
                RouteMsg rm = new RouteMsg(op.key, op); // don't know which partition is responsible, so ask the bootstrap server to forward it
                trigger(new Message(self, server, rm), net);
                pending.put(op.id, op.key);
                putID.add(op.id);
                LOG.info("Sending {}", op);
            }


            /*
            for (int i = 0; i < messages; i++) {
                GetOperation op = new GetOperation("" + i);
                LOG.info("OP id: " + op.id);
                RouteMsg rm = new RouteMsg(op.key, op); // don't know which partition is responsible, so ask the bootstrap server to forward it
                trigger(new Message(self, server, rm), net);
                pending.put(op.id, op.key);
                getID.add(op.id);
                LOG.info("Sending {}", op);
            } */
        }
    };
    protected final ClassMatchedHandler<OpResponse, Message> responseHandler = new ClassMatchedHandler<OpResponse, Message>() {

        @Override
        public void handle(OpResponse content, Message context) {
            int messages = res.get("messages", Integer.class);
            LOG.debug("Got OpResponse: {}", content);
            String key = pending.remove(content.id);
            if (key != null && putID.contains(content.id)) {
                LOG.info("Putting to res: " + content.status.toString());
                res.put(key, content.status.toString());
                GetOperation op = new GetOperation(key);
                LOG.info("GetOP id: " + op.id);
                RouteMsg rm = new RouteMsg(op.key, op); // don't know which partition is responsible, so ask the bootstrap server to forward it
                trigger(new Message(self, server, rm), net);
                pending.put(op.id, op.key);
                getID.add(op.id);
                LOG.info("Sending {}", op);

            }  else if (key != null && getID.contains(content.id)) {
                LOG.info("Got GetResponse: " + content.response);
                int tempKey = Integer.parseInt(key) + messages;
                String resp = content.response;
                res.put(Integer.toString(tempKey), resp);
            } else {
                LOG.warn("ID {} was not pending! Ignoring response.", content.id);
            }
        }
    };

    {
        subscribe(startHandler, control);
        subscribe(responseHandler, net);
    }

}
