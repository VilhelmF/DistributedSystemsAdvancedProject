package se.kth.id2203.ReadWrite;

import com.sun.jndi.cosnaming.IiopUrl;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.network.Address;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by sindrikaldal on 21/02/17.
 */
public class Read implements KompicsEvent, Serializable {

    private static final long serialVersionUID = -5669433156467202337L;

    public final NetAddress src;
    public final int rid;
    public final int key;
    public final UUID opId;

    public Read(NetAddress src, int rid,  int key, UUID opId) {
        this.src = src;
        this.rid = rid;
        this.key = key;
        this.opId = opId;
    }
}
