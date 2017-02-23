package se.kth.id2203.broadcasting;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by sindrikaldal on 23/02/17.
 */
public class PendingElement implements Comparable {

    private NetAddress src;
    private DataMessage msg;

    public PendingElement(NetAddress src, DataMessage msg) {
        this.src = src;
        this.msg = msg;
    }

    public NetAddress getSrc() {
        return src;
    }

    public void setSrc(NetAddress src) {
        this.src = src;
    }

    public DataMessage getMsg() {
        return msg;
    }

    public void setMsg(DataMessage msg) {
        this.msg = msg;
    }

    @Override
    public int compareTo(Object o) {
        PendingElement other = (PendingElement) o;
        return this.msg.vec.compareTo(other.getMsg().vec);
    }
}
