package se.kth.id2203.simulation.epfd;

import se.kth.id2203.broadcasting.BasicBroadcast;
import se.kth.id2203.broadcasting.BestEffortBroadcast;
import se.kth.id2203.failuredetector.EPFD;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Init;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;

/**
 * Created by sindrikaldal on 26/02/17.
 */
public class EPFDParentComponent extends ComponentDefinition {

    //******* Ports ******
    protected final Positive<Network> net = requires(Network.class);
    protected final Positive<Timer> timer = requires(Timer.class);
    protected final Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);

    //******* Children ******
    protected final Component epfd = create(EPFD.class, Init.NONE);
    protected final Component epfdClient = create(EPFDClient.class, Init.NONE);
    protected final Component basicbroadcast = create(BasicBroadcast.class, Init.NONE);



}
