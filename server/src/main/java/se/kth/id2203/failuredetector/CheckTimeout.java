package se.kth.id2203.failuredetector;

import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;

/**
 * Created by sindrikaldal on 22/02/17.
 */
public class CheckTimeout extends Timeout {
    protected CheckTimeout(ScheduleTimeout request) {
        super(request);
    }
}
