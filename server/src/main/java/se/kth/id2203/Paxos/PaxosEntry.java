package se.kth.id2203.Paxos;

import se.kth.id2203.networking.NetAddress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NavigableSet;

/**
 * Created by Vilhelm on 28.2.2017.
 */
public class PaxosEntry {

    public int prepts;                                     //Acceptor: Prepared timestamp.

    public int ats;                                        //Acceptor: Timestamp.
    public List<Object> av;                                //Acceptor: Accepted sequence.
    public int al;                                         //Acceptor: Length of decided seq.

    public int pts;                                        //Proposer: Timestamp.
    public List<Object> pv;                                //Proposer: Proposed seq.
    public int pl;                                         //Proposer: Length of learned seq.

    public List<Object> proposedValues;                    //Proposer: Values proposed while preparing.
    public HashMap<NetAddress, PaxosReadlistValue> readlist;
    public HashMap<NetAddress, Integer> accepted;             //Proposer's knowledge about length of acceptor's longest seq num.
    public HashMap<NetAddress, Integer> decided;              //Proposer's knowledge about length of acceptor's longest decided seq.

    public PaxosEntry(NavigableSet<NetAddress> topology) {
        this.prepts = 0;

        this.ats = 0;
        this.av = new ArrayList<>();
        this.al = 0;

        this.pts = 0;
        this.pv = new ArrayList<>();
        this.pl = 0;

        this.readlist = new HashMap<>();
        this.accepted = new HashMap<>();
        this.decided = new HashMap<>();

        for(NetAddress na : topology) {
            this.accepted.put(na, 0);
            this.decided.put(na, 0);
        }
    }

    public PaxosEntry(int prepts, int ats, List<Object> av, int al, int pts, List<Object> pv, int pl,
                      List<Object> proposedValues, HashMap<NetAddress, PaxosReadlistValue> readlist,
                      HashMap<NetAddress, Integer> accepted, HashMap<NetAddress, Integer> decided) {
        this.prepts = prepts;
        this.ats = ats;
        this.av = av;
        this.al = al;
        this.pts = pts;
        this.pv = pv;
        this.pl = pl;
        this.proposedValues = proposedValues;
        this.readlist = readlist;
        this.accepted = accepted;
        this.decided = decided;
    }

}
