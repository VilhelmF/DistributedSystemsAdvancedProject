package se.kth.id2203.simulation.atomic_register;

import se.kth.id2203.ReadWrite.ReadImposeWriteConsultMajority;
import se.kth.id2203.atomicregister.AtomicRegister;
import se.kth.id2203.broadcasting.BasicBroadcast;
import se.kth.id2203.broadcasting.BestEffortBroadcast;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;

/**
 * Created by vilhelm on 2017-02-27.
 */
public class RegisterParentComponent extends ComponentDefinition {

        //******* Ports ******
        protected final Positive<Network> net = requires(Network.class);
        protected final Positive<Timer> timer = requires(Timer.class);
        protected final Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
        protected final Positive<AtomicRegister> atomicRegisterPos = requires(AtomicRegister.class);

        //******* Children ******
        protected final Component basicbroadcast = create(BasicBroadcast.class, Init.NONE);
        protected final Component riwc = create(ReadImposeWriteConsultMajority.class, Init.NONE);
        protected  final Component registerclient = create(RegisterClient.class, Init.NONE);


        {
            connect(basicbroadcast.getPositive(BestEffortBroadcast.class), riwc.getNegative(BestEffortBroadcast.class), Channel.TWO_WAY);
            connect(net, basicbroadcast.getNegative(Network.class), Channel.TWO_WAY);

            //RIWC
            connect(riwc.getPositive(AtomicRegister.class), registerclient.getNegative(AtomicRegister.class), Channel.TWO_WAY);
            connect(net, riwc.getNegative(Network.class), Channel.TWO_WAY);

            connect(net, registerclient.getNegative(Network.class), Channel.TWO_WAY);
            connect(atomicRegisterPos, registerclient.getNegative(AtomicRegister.class),  Channel.TWO_WAY);
        }
}
