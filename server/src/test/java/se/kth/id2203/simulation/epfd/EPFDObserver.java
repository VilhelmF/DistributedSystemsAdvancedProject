package se.kth.id2203.simulation.epfd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.broadcasting.BroadcastMessage;
import se.kth.id2203.failuredetector.EventuallyPerfectFailureDetector;
import se.kth.id2203.failuredetector.StartMessage;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.network.Address;
import se.sics.kompics.network.Network;
import se.sics.kompics.simulator.network.identifier.Identifier;
import se.sics.kompics.simulator.util.GlobalView;
import se.sics.kompics.timer.CancelPeriodicTimeout;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;
import sun.nio.ch.Net;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Created by sindrikaldal on 26/02/17.
 */
@SuppressWarnings("Duplicates")
public class EPFDObserver extends ComponentDefinition {

    private static final Logger LOG = LoggerFactory.getLogger(EPFDObserver.class);
    private final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);

    Positive<Timer> timer = requires(Timer.class);
    Positive<Network> net = requires(Network.class);
    Positive<EventuallyPerfectFailureDetector> epfd = requires(EventuallyPerfectFailureDetector.class);


    private final int minDeadNodes;
    private final int minAliveNodes;

    private UUID timerId;

    public EPFDObserver(Init init) {
        minDeadNodes = init.minDeadNodes;
        minAliveNodes = init.minAliveNodes;

        subscribe(handleStart, control);
        subscribe(handleCheck, timer);
    }

    Handler<Start> handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            schedulePeriodicCheck();
        }
    };


    public static class Init extends se.sics.kompics.Init<EPFDObserver> {

        public final int minAliveNodes;
        public final int minDeadNodes;

        public Init(int minAliveNodes, int minDeadNodes) {
            this.minAliveNodes = minAliveNodes;
            this.minDeadNodes = minDeadNodes;
        }
    }

    @Override
    public void tearDown() {
        trigger(new CancelPeriodicTimeout(timerId), timer);
    }

    Handler<CheckTimeout> handleCheck = new Handler<CheckTimeout>() {
        @Override
        public void handle(CheckTimeout event) {
            GlobalView gv = config().getValue("simulation.globalview", GlobalView.class);

            if(gv.getDeadNodes().size() > minDeadNodes) {
                //TODO WHAT TO DO?
            } else if (gv.getAliveNodes().size() >= minAliveNodes) {

                Map<Identifier, Address> aliveNodes = gv.getAliveNodes();
                Iterator iterator = aliveNodes.values().iterator();

                LOG.info("Enough live nodes");
                while(iterator.hasNext()) {
                    Address address = (Address) iterator.next();
                    TreeSet<NetAddress> topology = new TreeSet<>();
                    if (!self.equals(address.getIp())) {
                        for (Address add: aliveNodes.values()
                                ) {
                            topology.add(new NetAddress(add.getIp(), add.getPort()));
                        }
                        trigger(new Message(self, new NetAddress(address.getIp(), address.getPort()), new BroadcastMessage(null, null, topology)), net);
                        tearDown();
                    }
                }
            }

        }
    };

    private void schedulePeriodicCheck() {
        long period = config().getValue("epfd.simulation.checkTimeout", Long.class);
        SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(period, period);
        CheckTimeout timeout = new CheckTimeout(spt);
        spt.setTimeoutEvent(timeout);
        trigger(spt, timer);
        timerId = timeout.getTimeoutId();
    }

    public static class CheckTimeout extends Timeout {

        public CheckTimeout(SchedulePeriodicTimeout spt) {
            super(spt);
        }
    }

    {
        subscribe(handleStart, control);
        subscribe(handleCheck, timer);
    }
}
