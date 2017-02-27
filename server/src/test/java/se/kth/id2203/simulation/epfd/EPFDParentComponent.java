package se.kth.id2203.simulation.epfd;

import se.kth.id2203.broadcasting.BasicBroadcast;
import se.kth.id2203.broadcasting.BestEffortBroadcast;
import se.kth.id2203.failuredetector.EPFD;
import se.kth.id2203.failuredetector.EventuallyPerfectFailureDetector;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;

/**
 * Created by sindrikaldal on 26/02/17.
 */
public class EPFDParentComponent extends ComponentDefinition {

    //******* Ports ******
    protected final Positive<Network> net = requires(Network.class);
    protected final Positive<Timer> timer = requires(Timer.class);
    protected final Positive<EventuallyPerfectFailureDetector> epfd = requires(EventuallyPerfectFailureDetector.class);

    //******* Children ******
    protected final Component epfdClient = create(EPFDClient.class, Init.NONE);
    protected final Component epfdServer = create(EPFD.class, Init.NONE);

    {
        connect(timer, epfdServer.getNegative(Timer.class), Channel.TWO_WAY);
        connect(net, epfdServer.getNegative(Network.class), Channel.TWO_WAY);
        connect(net, epfdClient.getNegative(Network.class), Channel.TWO_WAY);
        connect(epfdClient.getNegative(EventuallyPerfectFailureDetector.class), epfd, Channel.TWO_WAY);
        connect(epfdServer.getPositive(EventuallyPerfectFailureDetector.class), epfdClient.getNegative(EventuallyPerfectFailureDetector.class), Channel.TWO_WAY);

    }
}
