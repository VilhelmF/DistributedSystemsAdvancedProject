package se.kth.id2203.Paxos;

import se.kth.id2203.failuredetector.StartMessage;
import se.sics.kompics.PortType;

/**
 * Created by vilhelm on 2017-02-28.
 */
public class Paxos extends PortType {
    {
        request(Propose.class);
        request(StartMessage.class);
        indication(Abort.class);
        indication(ASCDecide.class);
    }
}
