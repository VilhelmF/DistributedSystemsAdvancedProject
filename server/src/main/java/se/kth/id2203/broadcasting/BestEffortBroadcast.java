package se.kth.id2203.broadcasting;

import se.sics.kompics.PortType;
import se.sics.kompics.network.Msg;

/**
 * Created by sindrikaldal on 18/02/17.
 */
public class BestEffortBroadcast extends PortType {
    {
        indication(BEB_Deliver.class);
        request(BEB_Broadcast.class);
    }
}
