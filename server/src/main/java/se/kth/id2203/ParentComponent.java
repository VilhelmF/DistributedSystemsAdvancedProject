package se.kth.id2203;

import com.google.common.base.Optional;
import se.kth.id2203.ReadWrite.ReadImposeWriteConsultMajority;
import se.kth.id2203.atomicregister.AtomicRegister;
import se.kth.id2203.bootstrapping.BootstrapClient;
import se.kth.id2203.bootstrapping.BootstrapServer;
import se.kth.id2203.bootstrapping.Bootstrapping;
import se.kth.id2203.broadcasting.*;
import se.kth.id2203.failuredetector.EPFD;
import se.kth.id2203.failuredetector.EventuallyPerfectFailureDetector;
import se.kth.id2203.kvstore.KVService;
import se.kth.id2203.networking.NetAddress;
import se.kth.id2203.overlay.Routing;
import se.kth.id2203.overlay.VSOverlayManager;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;

public class ParentComponent
        extends ComponentDefinition {

    //******* Ports ******
    protected final Positive<Network> net = requires(Network.class);
    protected final Positive<Timer> timer = requires(Timer.class);
    protected final Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    protected final Positive<ReliableBroadcast> rb = requires(ReliableBroadcast.class);
    protected final Positive<CausalOrderReliableBroadcast> crb = requires(CausalOrderReliableBroadcast.class);
    protected final Positive<AtomicRegister> atomicRegisterPos = requires(AtomicRegister.class);
    //******* Children ******
    protected final Component overlay = create(VSOverlayManager.class, Init.NONE);
    protected final Component riwc = create(ReadImposeWriteConsultMajority.class, Init.NONE);
    protected final Component kv = create(KVService.class, Init.NONE);
    protected final Component basicbroadcast = create(BasicBroadcast.class, Init.NONE);
    protected final Component reliableBroadcast = create(EagerReliableBroadcast.class, Init.NONE);
    protected final Component causalReliableBroadcast = create(WaitingCRB.class, Init.NONE);
    protected final Component epfd = create(EPFD.class, Init.NONE);
    protected final Component boot;


    {

        Optional<NetAddress> serverO = config().readValue("id2203.project.bootstrap-address", NetAddress.class);
        if (serverO.isPresent()) { // start in client mode
            boot = create(BootstrapClient.class, Init.NONE);
        } else { // start in server mode
            boot = create(BootstrapServer.class, Init.NONE);
        }
        connect(timer, boot.getNegative(Timer.class), Channel.TWO_WAY);
        connect(net, boot.getNegative(Network.class), Channel.TWO_WAY);
        // Overlay
        connect(boot.getPositive(Bootstrapping.class), overlay.getNegative(Bootstrapping.class), Channel.TWO_WAY);
        connect(net, overlay.getNegative(Network.class), Channel.TWO_WAY);
        connect(beb, overlay.getNegative(BestEffortBroadcast.class), Channel.TWO_WAY);
        // KV
        connect(overlay.getPositive(Routing.class), kv.getNegative(Routing.class), Channel.TWO_WAY);
        connect(net, kv.getNegative(Network.class), Channel.TWO_WAY);
        connect(atomicRegisterPos, kv.getNegative(AtomicRegister.class),  Channel.TWO_WAY);
        //BB
        connect(basicbroadcast.getPositive(BestEffortBroadcast.class), overlay.getNegative(BestEffortBroadcast.class), Channel.TWO_WAY);
        connect(basicbroadcast.getPositive(BestEffortBroadcast.class), reliableBroadcast.getNegative(BestEffortBroadcast.class), Channel.TWO_WAY);
        connect(net, basicbroadcast.getNegative(Network.class), Channel.TWO_WAY);
        //RB
        connect(net, reliableBroadcast.getNegative(Network.class), Channel.TWO_WAY);
        connect(reliableBroadcast.getPositive(ReliableBroadcast.class), causalReliableBroadcast.getNegative(ReliableBroadcast.class), Channel.TWO_WAY);
        //CRB
        connect(causalReliableBroadcast.getPositive(CausalOrderReliableBroadcast.class), riwc.getNegative(CausalOrderReliableBroadcast.class), Channel.TWO_WAY);
        //RIWC
        connect(riwc.getPositive(AtomicRegister.class), kv.getNegative(AtomicRegister.class), Channel.TWO_WAY);
        //connect(net, riwc.getNegative(Network.class), Channel.TWO_WAY);
        //EPFD
        connect(epfd.getPositive(EventuallyPerfectFailureDetector.class), overlay.getNegative(EventuallyPerfectFailureDetector.class), Channel.TWO_WAY);
        connect(timer, epfd.getNegative(Timer.class), Channel.TWO_WAY);
        connect(net, epfd.getNegative(Network.class), Channel.TWO_WAY);
    }
}
