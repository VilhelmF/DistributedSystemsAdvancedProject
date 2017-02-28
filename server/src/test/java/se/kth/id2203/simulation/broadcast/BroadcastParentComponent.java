package se.kth.id2203.simulation.broadcast;

import se.kth.id2203.broadcasting.BasicBroadcast;
import se.kth.id2203.broadcasting.BestEffortBroadcast;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;

/**
 * Created by sindrikaldal on 26/02/17.
 */
public class BroadcastParentComponent extends ComponentDefinition {
    //******* Ports ******
    protected final Positive<Network> net = requires(Network.class);
    protected final Positive<Timer> timer = requires(Timer.class);
    protected final Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);

    //******* Children ******
    protected final Component broadcastClient = create(BroadCastClient.class, Init.NONE);
    protected final Component basicbroadcast = create(BasicBroadcast.class, Init.NONE);

    {
        connect(broadcastClient.getNegative(BestEffortBroadcast.class), basicbroadcast.getPositive(BestEffortBroadcast.class), Channel.TWO_WAY);
        connect(broadcastClient.getNegative(Network.class), net, Channel.TWO_WAY);
        connect(basicbroadcast.getNegative(Network.class), net, Channel.TWO_WAY);
    }
}
