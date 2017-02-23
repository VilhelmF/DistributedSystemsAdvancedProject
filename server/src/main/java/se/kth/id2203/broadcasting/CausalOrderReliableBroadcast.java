package se.kth.id2203.broadcasting;

import se.sics.kompics.PortType;

/**
 * Created by sindrikaldal on 23/02/17.
 */
public class CausalOrderReliableBroadcast extends PortType {
    {
        indication(CRB_Deliver.class);
        request(CRB_Broadcast.class);
    }
}
