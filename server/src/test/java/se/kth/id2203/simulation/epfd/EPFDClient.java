package se.kth.id2203.simulation.epfd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.broadcasting.BroadcastMessage;
import se.kth.id2203.broadcasting.TopologyMessage;
import se.kth.id2203.failuredetector.EventuallyPerfectFailureDetector;
import se.kth.id2203.failuredetector.StartMessage;
import se.kth.id2203.failuredetector.Suspect;
import se.kth.id2203.networking.Message;
import se.kth.id2203.simulation.SimulationResultMap;
import se.kth.id2203.simulation.SimulationResultSingleton;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.simulator.util.GlobalView;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;

import java.util.UUID;

/**
 * Created by sindrikaldal on 26/02/17.
 */
public class EPFDClient extends ComponentDefinition {

    private static final Logger LOG = LoggerFactory.getLogger(EPFDClient.class);
    private final SimulationResultMap res = SimulationResultSingleton.getInstance();

    Positive<Timer> timer = requires(Timer.class);
    Negative<Network> network = provides(Network.class);
    Positive<EventuallyPerfectFailureDetector> epfd = requires(EventuallyPerfectFailureDetector.class);
    Positive<Network> net = requires(Network.class);

    protected final ClassMatchedHandler<BroadcastMessage, Message> broadcastHandler = new ClassMatchedHandler<BroadcastMessage, Message>() {

        @Override
        public void handle(BroadcastMessage content, Message context) {
            LOG.info("Received topology");
            trigger(new StartMessage(content.recipients), epfd);
        }
    };

    Handler<Suspect> suspectHandler = new Handler<Suspect>() {
        @Override
        public void handle(Suspect event) {
            LOG.info("Received a suspect");
            Object suspects = res.get("epfd-suspects", String.class);
            if (suspects == null) {
                res.put("epfd-suspects", "1");
            } else {
                res.put("epfd-suspects", Integer.toString(Integer.parseInt((String) suspects) + 1));
            }
        }
    };


    {
        subscribe(suspectHandler, epfd);
        subscribe(broadcastHandler, net);
    }
}
