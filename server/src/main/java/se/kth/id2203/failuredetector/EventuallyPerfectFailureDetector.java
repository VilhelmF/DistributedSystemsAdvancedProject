package se.kth.id2203.failuredetector;

import se.kth.id2203.broadcasting.TopologyMessage;
import se.sics.kompics.PortType;

/**
 * Created by sindrikaldal on 22/02/17.
 */
public class EventuallyPerfectFailureDetector extends PortType {

    {
        indication(Suspect.class);
        indication(Restore.class);
        request(TopologyMessage.class);
        request(StartMessage.class);
    }
}
