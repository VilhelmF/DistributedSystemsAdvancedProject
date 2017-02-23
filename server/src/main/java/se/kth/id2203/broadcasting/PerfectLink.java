package se.kth.id2203.broadcasting;

import se.sics.kompics.PortType;

public class PerfectLink extends PortType {
    {
        indication(PL_Deliver.class);
        request(PL_Send.class);
    }
}
