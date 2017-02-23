package se.kth.id2203.failuredetector;

import com.google.common.collect.Sets;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.*;
import se.sics.kompics.network.Address;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by sindrikaldal on 22/02/17.
 */
public class EPFD extends ComponentDefinition {

    //******* Ports ******
    protected final Positive<Timer> timer = requires(Timer.class);
    protected final Negative<EventuallyPerfectFailureDetector> epfd = provides(EventuallyPerfectFailureDetector.class);
    protected final Positive<Network> net = requires(Network.class);

    //******* Fields ******
    final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    final List<NetAddress> topology = new ArrayList<>(); //TODO initialize topology
    final long delta = 0; //cfg.getValue[Long]("epfd.simulation.delay"); //TODO find delta
    private HashSet<NetAddress> suspcected = new HashSet<>();
    private int seqnum = 0;
    private List<Address> alive = new ArrayList<>(); // TODO initialize with proper values
    private int period = 10; //TODO find proper period

    //******* Handlers ******
    protected final Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start e) {
            startTimer(period);
        }
    };

    protected final Handler<CheckTimeout> timeoutHandler = new Handler<CheckTimeout>() {
        @Override
        public void handle(CheckTimeout timeout) {
            if (!Sets.intersection(Sets.newHashSet(alive), suspcected).isEmpty()) {
                period += delta;
            }
            seqnum++;
            for (NetAddress address : topology) {
                if (!alive.contains(address) && !suspcected.contains(address)) {
                    suspcected.add(address);
                    trigger(new Suspect(address), epfd);
                } else if (alive.contains(address) && suspcected.contains(address)) {
                    suspcected.remove(address);
                    trigger(new Restore(address), epfd);
                }
                trigger(new Message(self, address, new HeartbeatRequest(seqnum)), net);
            }
            alive.clear();
            startTimer(period);
        }
    };

    protected final ClassMatchedHandler<HeartbeatRequest, Message> heartbeatRequestHandler = new ClassMatchedHandler<HeartbeatRequest, Message>() {

        @Override
        public void handle(HeartbeatRequest heartbeatRequest, Message context) {
            trigger(new Message(self, context.getSource(), new HeartbeatReply(heartbeatRequest.seq)), net);
        }
    };

    protected final ClassMatchedHandler<HeartbeatReply, Message> heartbeatReplyHandler = new ClassMatchedHandler<HeartbeatReply, Message>() {

        @Override
        public void handle(HeartbeatReply heartbeatReply, Message context) {
            alive.add(context.getSource());
        }
    };


    public void startTimer(int period) {
        ScheduleTimeout scheduledTimeout = new ScheduleTimeout(period);
        scheduledTimeout.setTimeoutEvent(new CheckTimeout(scheduledTimeout));
        trigger(scheduledTimeout, timer);
    }

    {
        subscribe(startHandler, control);
        subscribe(timeoutHandler, timer);
        subscribe(heartbeatRequestHandler, net);
        subscribe(heartbeatReplyHandler, net);
    }


}
