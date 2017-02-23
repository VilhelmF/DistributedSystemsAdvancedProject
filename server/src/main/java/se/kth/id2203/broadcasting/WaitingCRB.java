package se.kth.id2203.broadcasting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Created by sindrikaldal on 23/02/17.
 */
public class WaitingCRB extends ComponentDefinition {

    final static Logger LOG = LoggerFactory.getLogger(BasicBroadcast.class);

    //******* Ports ******
    protected final Positive<ReliableBroadcast> rb = requires(ReliableBroadcast.class);
    protected final Negative<CausalOrderReliableBroadcast> crb = provides(CausalOrderReliableBroadcast.class);

    //******* Fields ******
    private final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    private List<PendingElement> pending = new ArrayList<>();
    private HashSet<NetAddress> suspected = new HashSet<>();
    private VectorClock vec  = new VectorClock();
    private int lsn = 0;


    protected final Handler<CRB_Broadcast> crbBroadcastHandler = new Handler<CRB_Broadcast>() {

        @Override
        public void handle(CRB_Broadcast broadcastMessage) {
            if (!suspected.contains(broadcastMessage.src)) {
                VectorClock W = copyVectorClock(vec);
                W.set(self, lsn);
                lsn++;
                trigger(new RB_Broadcast(broadcastMessage.src, new DataMessage(W, broadcastMessage.payload)), rb);
            }
        }
    };

    protected final Handler<RB_Deliver> rbDeliverHandler = new Handler<RB_Deliver>() {

        @Override
        public void handle(RB_Deliver broadcastMessage) {
            DataMessage dataMessage = (DataMessage) broadcastMessage.payload;
            pending.add(new PendingElement(broadcastMessage.src, dataMessage));
            Collections.sort(pending);
            for (PendingElement element : pending) {
                if (dataMessage.vec.compareTo(vec) == 0) {
                    pending.remove(element);
                    vec.inc(element.getSrc());
                    trigger(new CRB_Deliver(element.getSrc(), element.getMsg().payload), crb);
                }
            }
        }
    };

    public VectorClock copyVectorClock(VectorClock vec) {
        VectorClock newVec = new VectorClock();
        for (VectorClockElement element : vec.vectorClock) {
            newVec.set(element.getAddress(), element.getValue());
        }

        return newVec;
    }

    {
        subscribe(crbBroadcastHandler, crb);
        subscribe(rbDeliverHandler, rb);
    }


}
