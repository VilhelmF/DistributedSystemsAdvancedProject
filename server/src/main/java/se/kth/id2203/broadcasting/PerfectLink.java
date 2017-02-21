package se.kth.id2203.broadcasting;

import se.sics.kompics.PortType;

/**
 * Created by sindrikaldal on 21/02/17.
 */
public class PerfectLink extends PortType {
    {
        indication(PL_Deliver.class);
        request(PL_Send.class);
    }
}
