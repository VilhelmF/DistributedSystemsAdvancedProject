package se.kth.id2203.simulation.broadcast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.broadcasting.BroadcastMessage;
import se.kth.id2203.broadcasting.TopologyMessage;
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

import java.util.*;

/**
 * Created by sindrikaldal on 26/02/17.
 */
public class BroadcastObserver extends ComponentDefinition {

    private static final Logger LOG = LoggerFactory.getLogger(BroadcastObserver.class);
    private final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);

    Positive<Timer> timer = requires(Timer.class);
    Positive<Network> net = requires(Network.class);


    private final int minAliveNodes;

    private UUID timerId;

    public BroadcastObserver(Init init) {
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


    public static class Init extends se.sics.kompics.Init<BroadcastObserver> {

        public final int minAliveNodes;

        public Init(int minAliveNodes) {
            this.minAliveNodes = minAliveNodes;
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

            if(gv.getAliveNodes().size() > minAliveNodes) {

                Map<Identifier, Address> aliveNodes = gv.getAliveNodes();
                Iterator iterator = aliveNodes.values().iterator();

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
                        break;
                    }
                }

            }

        }
    };

    private void schedulePeriodicCheck() {
        long period = config().getValue("broadcast.simulation.checkTimeout", Long.class);
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
}
