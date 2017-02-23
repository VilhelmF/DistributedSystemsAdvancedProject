package se.kth.id2203.broadcasting;

import se.sics.kompics.PortType;

public class CausalOrderReliableBroadcast extends PortType {
    {
        indication(CRB_Deliver.class);
        request(CRB_Broadcast.class);
    }
}
