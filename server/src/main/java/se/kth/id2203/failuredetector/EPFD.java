package se.kth.id2203.failuredetector;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * Created by sindrikaldal on 22/02/17.
 */
public class EPFD extends ComponentDefinition {

    final static Logger LOG = LoggerFactory.getLogger(EPFD.class);

    //******* Ports ******
    protected final Positive<Timer> timer = requires(Timer.class);
    protected final Negative<EventuallyPerfectFailureDetector> epfd = provides(EventuallyPerfectFailureDetector.class);
    protected final Positive<Network> net = requires(Network.class);

    //******* Fields ******
    final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    final long delta = 1000; //cfg.getValue[Long]("epfd.simulation.delay");
    private NavigableSet<NetAddress> topology;
    private HashSet<NetAddress> suspcected = new HashSet<>();
    private int seqnum = 0;
    private List<Address> alive = new ArrayList<>(); // TODO initialize with proper values
    private int period = 2000; //TODO find proper period

    //******* Handlers ******
    /*protected final Handler<Start> startHandler = new Handler<Start>() {

        @Override
        public void handle(Start e) {
            startTimer(period);
        }
    };*/

    protected final Handler<StartMessage> startHandler = new Handler<StartMessage>() {
        @Override
        public void handle(StartMessage startMessage) {
            topology = startMessage.topology;
            for (NetAddress na : topology) {
                alive.add(na);
            }
            startTimer(period);
        }
    };

    protected final Handler<TopologyMessage> topologyHandler = new Handler<TopologyMessage>() {
        @Override
        public void handle(TopologyMessage topologyMessage) {
            LOG.info("EPFD : Received new topology with size : " + topologyMessage.topology.size());
            topology = topologyMessage.topology;
        }
    };

    protected final Handler<CheckTimeout> timeoutHandler = new Handler<CheckTimeout>() {
        @Override
        public void handle(CheckTimeout timeout) {
            if (!Sets.intersection(Sets.newHashSet(alive), suspcected).isEmpty()) {
                period += delta;
            }
            LOG.info("Checking timeout");
            LOG.info("Topology size : " + topology.size());
            seqnum++;
            LOG.info("My topology: " + topology.toString());
            for (NetAddress address : topology) {
                LOG.info("Checking " + address.toString());
                if (!alive.contains(address) && !suspcected.contains(address)) {
                    LOG.info("Suspecting " + address.toString());
                    suspcected.add(address);
                    trigger(new Suspect(address), epfd);
                } else if (alive.contains(address) && suspcected.contains(address)) {
                    LOG.info("Restoring " + address.toString());
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
        subscribe(startHandler, epfd);
        subscribe(timeoutHandler, timer);
        subscribe(heartbeatRequestHandler, net);
        subscribe(heartbeatReplyHandler, net);
        subscribe(topologyHandler, epfd);
    }


}
