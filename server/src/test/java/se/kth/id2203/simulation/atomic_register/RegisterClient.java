package se.kth.id2203.simulation.atomic_register;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.atomicregister.AtomicRegister;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;

/**
 * Created by vilhelm on 2017-02-27.
 */
public class RegisterClient extends ComponentDefinition {

    final static Logger LOG = LoggerFactory.getLogger(RegisterClient.class);
    //******* Ports ******
    protected final Positive<Network> net = requires(Network.class);
    protected final Positive<AtomicRegister> atomicRegister = requires(AtomicRegister.class);

    //******* Handlers ******

}

