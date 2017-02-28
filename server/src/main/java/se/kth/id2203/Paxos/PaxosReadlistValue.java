package se.kth.id2203.Paxos;

import java.util.List;

/**
 * Created by sindrikaldal on 27/02/17.
 */
public class PaxosReadlistValue {

    private int ts;
    private List<Propose> vsuf;

    public PaxosReadlistValue(int ts, List<Propose> vsuf) {
        this.ts = ts;
        this.vsuf = vsuf;
    }

    public int getTs() {
        return ts;
    }

    public void setTs(int ts) {
        this.ts = ts;
    }

    public List<Propose> getVsuf() {
        return vsuf;
    }

    public void setVsuf(List<Propose> vsuf) {
        this.vsuf = vsuf;
    }
}
