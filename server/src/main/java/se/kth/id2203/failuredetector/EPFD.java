package se.kth.id2203.failuredetector;

import com.google.common.collect.Sets;
import se.kth.id2203.broadcasting.TopologyMessage;
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
import java.util.NavigableSet;

import static se.sics.kompics.network.netty.serialization.Serializers.LOG;

/**
 * Created by sindrikaldal on 22/02/17.
 */
public class EPFD extends ComponentDefinition {

    //******* Ports ******
    protected final Positive<Timer> timer = requires(Timer.class);
    protected final Positive<EventuallyPerfectFailureDetector> epfd = requires(EventuallyPerfectFailureDetector.class);
    protected final Positive<Network> net = requires(Network.class);

    //******* Fields ******
    final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    final long delta = 500; //cfg.getValue[Long]("epfd.simulation.delay");
    private NavigableSet<NetAddress> topology;
    private HashSet<NetAddress> suspcected = new HashSet<>();
    private int seqnum = 0;
    private List<Address> alive = new ArrayList<>(); // TODO initialize with proper values
    private int period = 1500; //TODO find proper period

    //******* Handlers ******
    /*protected final Handler<Start> startHandler = new Handler<Start>() {

        @Override
        public void handle(Start e) {
            startTimer(period);
        }
    };*/

    protected final Handler<TopologyMessage> topologyHandler = new Handler<TopologyMessage>() {
        @Override
        public void handle(TopologyMessage topologyMessage) {

            LOG.info("Received new topology and starting failure detection");
            topology = topologyMessage.topology;
            startTimer(period);
        }
    };

    protected final Handler<CheckTimeout> timeoutHandler = new Handler<CheckTimeout>() {
        @Override
        public void handle(CheckTimeout timeout) {
            LOG.info("Performing check");
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
            LOG.info(self + " sent a heartbeatrequest to " + context.getSource());
            trigger(new Message(self, context.getSource(), new HeartbeatReply(heartbeatRequest.seq)), net);
        }
    };

    protected final ClassMatchedHandler<HeartbeatReply, Message> heartbeatReplyHandler = new ClassMatchedHandler<HeartbeatReply, Message>() {

        @Override
        public void handle(HeartbeatReply heartbeatReply, Message context) {
            LOG.info(self + " received a heartbeatreply from " + context.getSource());
            alive.add(context.getSource());
        }
    };


    public void startTimer(int period) {
        ScheduleTimeout scheduledTimeout = new ScheduleTimeout(period);
        scheduledTimeout.setTimeoutEvent(new CheckTimeout(scheduledTimeout));
        trigger(scheduledTimeout, timer);
    }

    {
        //subscribe(startHandler, control);
        subscribe(timeoutHandler, timer);
        subscribe(heartbeatRequestHandler, net);
        subscribe(heartbeatReplyHandler, net);
        subscribe(topologyHandler, epfd);
    }


}
