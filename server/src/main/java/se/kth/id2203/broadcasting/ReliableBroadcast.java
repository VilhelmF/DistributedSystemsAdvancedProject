package se.kth.id2203.broadcasting;

import se.sics.kompics.PortType;

/**
 * Created by sindrikaldal on 23/02/17.
 */
public class ReliableBroadcast extends PortType {
    {
        indication(RB_Deliver.class);
        request(RB_Broadcast.class);
    }
}
