package se.kth.id2203.simulation.epfd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
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

    Positive<Timer> timer = requires(Timer.class);
    Positive<Network> network = requires(Network.class);

    private UUID timerId;

    Handler<Start> handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            schedulePeriodicCheck();
        }
    };

    private void schedulePeriodicCheck() {
        long period = config().getValue("epfd.simulation.checktimeout", Long.class);
        SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(period, period);
        CheckTimeout timeout = new CheckTimeout(spt);
        spt.setTimeoutEvent(timeout);
        trigger(spt, timer);
        timerId = timeout.getTimeoutId();
    }

    Handler<CheckTimeout> handleCheck = new Handler<CheckTimeout>() {
        @Override
        public void handle(CheckTimeout event) {
            GlobalView gv = config().getValue("simulation.globalview", GlobalView.class);

            if(gv.getDeadNodes().size() > 0) {
                LOG.info("Terminating simulation as the min dead nodes:{} is achieved", 1);
                gv.terminate();
            }
        }
    };

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
