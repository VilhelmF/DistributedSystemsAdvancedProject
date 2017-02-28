/*
 * The MIT License
 *
 * Copyright 2017 Lars Kroll <lkroll@kth.se>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package se.kth.id2203.overlay;

import com.larskroll.common.J6;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.Paxos.Paxos;
import se.kth.id2203.atomicregister.AtomicRegister;
import se.kth.id2203.bootstrapping.Booted;
import se.kth.id2203.bootstrapping.Bootstrapping;
import se.kth.id2203.bootstrapping.GetInitialAssignments;
import se.kth.id2203.bootstrapping.InitialAssignments;
import se.kth.id2203.broadcasting.BestEffortBroadcast;
import se.kth.id2203.broadcasting.CausalOrderReliableBroadcast;
import se.kth.id2203.broadcasting.TopologyMessage;
import se.kth.id2203.failuredetector.*;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;

import java.util.Collection;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * The V(ery)S(imple)OverlayManager.
 * <p>
 * Keeps all nodes in a single partition in one replication group.
 * <p>
 * Note: This implementation does not fulfill the project task. You have to
 * support multiple partitions!
 * <p>
 * @author Lars Kroll <lkroll@kth.se>
 */
public class VSOverlayManager extends ComponentDefinition {

    final static Logger LOG = LoggerFactory.getLogger(VSOverlayManager.class);
    //******* Ports ******

    protected final Negative<Routing> route = provides(Routing.class);
    protected final Positive<EventuallyPerfectFailureDetector> epfd = requires(EventuallyPerfectFailureDetector.class);
    protected final Positive<Bootstrapping> boot = requires(Bootstrapping.class);
    protected final Positive<Network> net = requires(Network.class);
    protected final Positive<Timer> timer = requires(Timer.class);
    protected final Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    protected final Positive<CausalOrderReliableBroadcast> crb = requires(CausalOrderReliableBroadcast.class);
    protected final Positive<AtomicRegister> ar = requires(AtomicRegister.class);
    protected final Positive<Paxos> asc = requires(Paxos.class);



    //******* Fields ******
    final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    final int partitions = config().getValue("id2203.project.partitionCount", Integer.class);
    final int partitionSize = config().getValue("id2203.project.replicationDegree", Integer.class);
    private LookupTable lut = null;
    private NavigableSet<NetAddress> partition = null;

    //******* Handlers ******
    protected final Handler<GetInitialAssignments> initialAssignmentHandler = new Handler<GetInitialAssignments>() {

        @Override
        public void handle(GetInitialAssignments event) {
            LOG.info("Generating LookupTable...");
            LookupTable lut = LookupTable.generatePartitionedTable(event.nodes, partitions, partitionSize);
            LOG.debug("Generated assignments:\n{}", lut);
            trigger(new InitialAssignments(lut), boot);
        }
    };
    protected final Handler<Booted> bootHandler = new Handler<Booted>() {

        @Override
        public void handle(Booted event) {
            if (event.assignment instanceof LookupTable) {
                LOG.info("Got NodeAssignment, overlay ready.");
                lut = (LookupTable) event.assignment;
                LOG.info("I am: " + self);
                LOG.info("My lookup table: ");
                LOG.info(lut.toString());
                partition = lut.getPartition(self);
                if(partition == null) {
                    try {
                        throw new Exception("Could not find self in lookup table. Initialization faulty.");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.exit(0);
                }
                NavigableSet<NetAddress> failureTopology = new TreeSet<>(partition);
                failureTopology.remove(self);

                LOG.info(self + ": The topolgy I'm sending - " + partition.toString());
                trigger(new TopologyMessage(partition), beb);
                trigger(new StartMessage(failureTopology), epfd);
                trigger(new StartMessage(partition), asc);
                LOG.info(partition.toString());
            } else {
                LOG.error("Got invalid NodeAssignment type. Expected: LookupTable; Got: {}", event.assignment.getClass());
            }
        }
    };
    protected final ClassMatchedHandler<RouteMsg, Message> routeHandler = new ClassMatchedHandler<RouteMsg, Message>() {

        @Override
        public void handle(RouteMsg content, Message context) {
            //int i_key = Integer.parseInt(content.key);
            Collection<NetAddress> partition = lut.get(content.key);
            NetAddress target = J6.randomElement(partition);
            // Hvað er að frétta með etta NavigableSet?
            for(NetAddress na : partition) {
                // Þangað til ég finn út hvernig ég tek út úr þessu fjandans setti
                target = na;
            }
            LOG.info("Broadcasting message for key {} to {}", content.key, target);
            trigger(new Message(context.getSource(), target, content.msg), net);
            //trigger(new BroadcastMessage(context.getSource(), content.msg, partition), broadcast);
        }
    };
    protected final Handler<RouteMsg> localRouteHandler = new Handler<RouteMsg>() {

        @Override
        public void handle(RouteMsg event) {
            Collection<NetAddress> partition = lut.lookup(event.key);
            NetAddress target = J6.randomElement(partition);
            LOG.info("Routing message for key {} to {}", event.key, target);
            trigger(new Message(self, target, event.msg), net);
        }
    };

    protected final ClassMatchedHandler<Connect, Message> connectHandler = new ClassMatchedHandler<Connect, Message>() {

        @Override
        public void handle(Connect content, Message context) {
            if (lut != null) {
                LOG.debug("Accepting connection request from {}", context.getSource());
                int size = lut.getNodes().size();
                trigger(new Message(self, context.getSource(), content.ack(size)), net);
            } else {
                LOG.info("Rejecting connection request from {}, as system is not ready, yet.", context.getSource());
            }
        }
    };

    protected final Handler<Suspect> suspectHandler = new Handler<Suspect>() {

        @Override
        public void handle(Suspect event) {
            trigger(event, beb);
            trigger(new Topology_Change(-1), ar);
        }
    };

    protected final Handler<Restore> restoreHandler = new Handler<Restore>() {

        @Override
        public void handle(Restore event) {
            trigger(event, beb);
            trigger(new Topology_Change(1), ar);
        }
    };

    {
        subscribe(initialAssignmentHandler, boot);
        subscribe(bootHandler, boot);
        subscribe(routeHandler, net);
        subscribe(localRouteHandler, route);
        subscribe(connectHandler, net);
        subscribe(suspectHandler, epfd);
        subscribe(restoreHandler, epfd);
    }
}
