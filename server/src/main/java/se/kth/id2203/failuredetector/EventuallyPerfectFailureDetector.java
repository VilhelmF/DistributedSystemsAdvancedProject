package se.kth.id2203.failuredetector;

import se.sics.kompics.PortType;

/**
 * Created by sindrikaldal on 22/02/17.
 */
public class EventuallyPerfectFailureDetector extends PortType {

    {
        indication(Suspect.class);
        request(Restore.class);

    }
}
