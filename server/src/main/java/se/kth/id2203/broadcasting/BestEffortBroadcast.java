package se.kth.id2203.broadcasting;

import se.kth.id2203.failuredetector.Restore;
import se.kth.id2203.failuredetector.Suspect;
import se.sics.kompics.PortType;

/**
 * Created by sindrikaldal on 18/02/17.
 */
public class BestEffortBroadcast extends PortType {
    {
        indication(BEB_Deliver.class);
        request(TopologyMessage.class);
        request(BEB_Broadcast.class);
        request(Suspect.class);
        request(Restore.class);
    }
}
