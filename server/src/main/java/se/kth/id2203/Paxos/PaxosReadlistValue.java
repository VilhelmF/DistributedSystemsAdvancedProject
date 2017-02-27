package se.kth.id2203.Paxos;

import java.util.List;

/**
 * Created by sindrikaldal on 27/02/17.
 */
public class PaxosReadlistValue {

    private int ts;
    private List<Object> vsuf;

    public PaxosReadlistValue(int ts, List<Object> vsuf) {
        this.ts = ts;
        this.vsuf = vsuf;
    }

    public int getTs() {
        return ts;
    }

    public void setTs(int ts) {
        this.ts = ts;
    }

    public List<Object> getVsuf() {
        return vsuf;
    }

    public void setVsuf(List<Object> vsuf) {
        this.vsuf = vsuf;
    }
}
