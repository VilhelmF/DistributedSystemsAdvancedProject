package se.kth.id2203.broadcasting;

import se.kth.id2203.failuredetector.Restore;
import se.kth.id2203.failuredetector.Suspect;
import se.sics.kompics.PortType;

public class BestEffortBroadcast extends PortType {
    {
        indication(BEB_Deliver.class);
        request(TopologyMessage.class);
        request(BEB_Broadcast.class);
        request(Suspect.class);
        request(Restore.class);
    }
}
