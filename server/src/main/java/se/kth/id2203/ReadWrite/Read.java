package se.kth.id2203.ReadWrite;

import com.sun.jndi.cosnaming.IiopUrl;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.network.Address;

/**
 * Created by sindrikaldal on 21/02/17.
 */
public class Read implements KompicsEvent {

    public final Address src;
    public final int rid;

    public Read(Address src, int rid) {
        this.src = src;
        this.rid = rid;
    }
}
