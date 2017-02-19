package se.kth.id2203.broadcasting;

import se.sics.kompics.PortType;
import se.sics.kompics.network.Msg;

/**
 * Created by sindrikaldal on 18/02/17.
 */
public class Broadcasting extends PortType {
    {
        positive(BroadcastMessage.class);
        negative(BroadcastMessage.class);
        indication(BroadcastMessage.class);
        request(BroadcastMessage.class);
    }
}
