package se.kth.id2203.Paxos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.broadcasting.BestEffortBroadcast;
import se.kth.id2203.failuredetector.StartMessage;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.*;
import se.sics.kompics.network.Address;
import se.sics.kompics.network.Network;

import java.util.*;

/**
 * Created by vilhelm on 27/02/17.
 */
@SuppressWarnings("Since15")
public class MultiPaxos2 extends ComponentDefinition {

    final static Logger LOG = LoggerFactory.getLogger(MultiPaxos2.class);

    //******* Ports ******
    Positive<Network> net = requires(Network.class);
    Negative<AbortableSequenceConsensus> asc = provides(AbortableSequenceConsensus.class);
    Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);

    //******* Fields ******
    public NavigableSet<NetAddress> topology;
    public int N;
    private int t;                                          //Logical clock.
    /*
    private int prepts;                                     //Acceptor: Prepared timestamp.
    private int ats;                                        //Acceptor: Timestamp.
    private List<Object> av;                                //Acceptor: Accepted sequence.
    private int al;                                         //Acceptor: Length of decided seq.
    private int pts;                                        //Proposer: Timestamp.
    private List<Object> pv;                                //Proposer: Proposed seq.
    private int pl;                                         //Proposer: Length of learned seq.
    private List<Object> proposedValues;                    //Proposer: Values proposed while preparing.
    private HashMap<Address, PaxosReadlistValue> readlist;
    private HashMap<Address, Integer> accepted;             //Proposer's knowledge about length of acceptor's longest seq num.
    private HashMap<Address, Integer> decided;              //Proposer's knowledge about length of acceptor's longest decided seq.
    */
    final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);


    private HashMap<Integer, PaxosEntry> paxosEntryHashMap;

    //******* Constructor ******
    public MultiPaxos2(Init init) {
        this.topology = new TreeSet<>(); // TODO assign topology
        this.N = 3; // TODO assign proper N

        this.t = init.t;
        /*
        this.prepts = init.prepts;

        this.ats = init.ats;
        this.av = init.av;
        this.al = init.al;

        this.pts = init.pts;
        this.pv = init.pv;
        this.pl = init.pl;

        this.proposedValues = init.proposedValues;
        this.readlist = init.readlist;
        this.accepted = init.accepted;
        this.decided = init.decided;
        */

        subscribe(proposeHandler, asc);
        subscribe(prepareHandler, net);
        subscribe(nackHandler, net);
        subscribe(prepareAckHandler, net);
        subscribe(acceptHandler, net);
        subscribe(acceptAckHandler, net);
        subscribe(decideHandler, net);

    }

    //******* Init ******
    public static class Init extends se.sics.kompics.Init<MultiPaxos2> {

        public List<NetAddress> topology;
        public int N;
        public int t;
        /*
        public int prepts;
        public int ats;
        public List<Object> av;
        public int al;
        public int pts;
        public List<Object> pv;
        public int pl;
        public List<Object> proposedValues;
        public HashMap<Address, PaxosReadlistValue> readlist;
        public HashMap<Address, Integer> accepted;
        public HashMap<Address, Integer> decided;
        */

        public Init() {
            this.t = 0;
            /*
            this.prepts = 0;
            this.ats = 0;
            this.av = new ArrayList<>();
            this.al = 0;
            this.pts = 0;
            this.pv = new ArrayList<>();
            this.pl = 0;
            this.proposedValues = new ArrayList<>();
            //TODO Init with topology
            this.readlist = new HashMap<>();
            this.accepted = new HashMap<>();
            this.decided = new HashMap<>();
            */

        }
    }

    //******* Handlers ******
    protected final Handler<StartMessage> startHandler = new Handler<StartMessage>() {
        @Override
        public void handle(StartMessage startMessage) {
            LOG.info("Received Start Message");

            // Not fully sure where to handle initialization.
            t = 0;
            /*
            prepts = 0;
            ats = 0;
            av = new ArrayList<>();
            al = 0;
            pts = 0;
            pv = new ArrayList<>();
            pl = 0;

            proposedValues = new ArrayList<>();
            readlist = new HashMap<>();
            accepted = new HashMap<>();
            decided = new HashMap<>();
            */

            paxosEntryHashMap = new HashMap<>();


            topology = startMessage.topology;
            N = topology.size();
            /*
            for (NetAddress na : topology) {
                accepted.put(na, 0);
                decided.put(na, 0);
            }*/
        }
    };

    protected final ClassMatchedHandler<Propose, Message> proposeHandler = new ClassMatchedHandler<Propose, Message>() {

        @Override
        public void handle(Propose propose, Message context) {
            t++;

            //TODO put key into message!
            int key = 0;

            PaxosEntry paxosEntry = paxosEntryHashMap.get(0);
            if(paxosEntry == null) paxosEntry = new PaxosEntry(topology);

            if (paxosEntry.pts == 0) {
                paxosEntry.pts = (t * paxosEntry.readlist.size()) + selfRank();
                paxosEntry.pv = getPrefix(paxosEntry.av, paxosEntry.al);
                paxosEntry.pl = 0;
                //proposedValues = propose.values;
                paxosEntry.proposedValues = new ArrayList<>();
                //TODO create value message.
                paxosEntry.proposedValues.add(value);
                //TODO Hashmaps with topology. Readlist empty, accepted decided with 0 for each node.

                // TODO Empty all out of lists? Leave 0? Leave null in readlist?
                paxosEntry.readlist.clear();
                paxosEntry.accepted.clear();
                paxosEntry.decided.clear();
                for (NetAddress address : topology) {
                    trigger(new Message(self, address, new Prepare(key, paxosEntry.pts, paxosEntry.al, t)), net);
                }
            }
            else if (paxosEntry.readlist.size() <= N / 2) {
                paxosEntry.proposedValues.add(value);
                /*for (Object obj : propose.values) {
                    proposedValues.add(obj);
                }*/
            }
            /*
            else if (!pv.equals(propose.values)) {
                for (Object obj : propose.values) {
                    pv.add(obj);
                }
                for (NetAddress address : topology) {
                    trigger(new Message(self, address, new Accept(pts, propose.values, pv.size() - 1, t)), net);
                }
            }*/
            else if (!paxosEntry.pv.contains(value)) {
                paxosEntry.pv.add(value);
                for (NetAddress address : paxosEntry.readlist.keySet()) {
                    // TODO Handle value
                    trigger(new Message(self, address, new Accept(key, paxosEntry.pts, propose.values, paxosEntry.pv.size() - 1, t)), net);
                }

            }
        }
    };

    protected final ClassMatchedHandler<Prepare, Message> prepareHandler = new ClassMatchedHandler<Prepare, Message>() {

        @Override
        public void handle(Prepare prepare, Message context) {
            t = Integer.max(t, prepare.t2) + 1;
            PaxosEntry paxosEntry = paxosEntryHashMap.get(prepare.key);
            if (prepare.ts < paxosEntry.prepts) {
                trigger(new Message(self, context.getSource(), new Nack(prepare.ts, t)), net);
            } else {
                paxosEntry.prepts = prepare.ts;
                paxosEntryHashMap.put(prepare.key, paxosEntry);
                trigger(new Message(self, context.getSource(), new PrepareAck(prepare.ts, paxosEntry.ats, getSuffix(paxosEntry.av, prepare.l), paxosEntry.al, t)), net);
            }
        }
    };

    protected final ClassMatchedHandler<Nack, Message> nackHandler = new ClassMatchedHandler<Nack, Message>() {

        @Override
        public void handle(Nack nack, Message context) {
            t = Integer.max(t, nack.t) + 1;
            if (nack.pts == pts) {
                pts = 0;
                trigger(new Abort(), asc);
            }
        }
    };

    protected final ClassMatchedHandler<PrepareAck, Message> prepareAckHandler = new ClassMatchedHandler<PrepareAck, Message>() {

        @Override
        public void handle(PrepareAck prepareAck, Message context) {
            t = Integer.max(t, prepareAck.t) + 1;
            if (prepareAck.ats == pts); {
                readlist.put(context.getSource(), new PaxosReadlistValue(prepareAck.ts, prepareAck.values));
                decided.put(context.getSource(), prepareAck.al);
                if (readlist.size() == ((N / 2) + 1)) {
                    int tsTemp = 0;
                    List<Object> vsufTemp = new ArrayList<>();
                    for (PaxosReadlistValue readListValue : readlist.values()) {
                        if (tsTemp < readListValue.getTs() || (tsTemp == readListValue.getTs() && vsufTemp.size() < readListValue.getVsuf().size())) {
                            tsTemp = readListValue.getTs();
                            vsufTemp = new ArrayList<>(readListValue.getVsuf());
                        }
                    }
                    pv.add(vsufTemp);
                    for (Value value : proposedValues) {
                        if (!pv.contains(value)) {
                            pv.add(value);
                        }
                    }
                    for (NetAddress address : topology) {
                        Integer tempL = decided.get(address);
                        List<Object> tempSuffix = getSuffix(pv, tempL);
                        trigger(new Message(self, address, new Accept(pts, tempSuffix, tempL, t)), net);
                    }
                } else if (readlist.size() > ((N/2) + 1)) {
                    trigger(new Message(self, context.getSource(), new Accept(pts, getSuffix(pv, prepareAck.al), al, t)), net);
                    if (pl != 0) {
                        trigger(new Message(self, context.getSource(), new Decide(pts, pl, t)), net);
                    }
                }
            }
        }
    };

    protected final ClassMatchedHandler<Accept, Message> acceptHandler = new ClassMatchedHandler<Accept, Message>() {

        @Override
        public void handle(Accept accept, Message context) {
            t = Integer.max(t, accept.t) + 1;
            if (accept.pts != prepts) {
                trigger(new Message(self, context.getSource(), new Nack(accept.pts, t)), net);
            }
            else {
                ats = accept.pts;
                if (accept.pvLength < av.size()) {
                    av = getPrefix(av, accept.pvLength);
                }
                av.add(accept.values);
                trigger(new Message(self, context.getSource(), new AcceptAck(accept.pts, av.size(), t)), net);
            }
        }
    };

    protected final ClassMatchedHandler<AcceptAck, Message> acceptAckHandler = new ClassMatchedHandler<AcceptAck, Message>() {

        @Override
        public void handle(AcceptAck acceptAck, Message context) {
            t = Integer.max(t, acceptAck.t) + 1;
            if (acceptAck.pts == pts) {
                accepted.put(context.getSource(), acceptAck.l);
                List<Integer> tempList = new ArrayList<>();

                for (Integer num : accepted.values()) {
                    if (num >= acceptAck.l) {
                        tempList.add(num);
                    }
                }
                if (pl < acceptAck.l || tempList.size() > N / 2) {
                    pl = acceptAck.l;
                    for (Address address : readlist.keySet()) {
                        trigger(new Message(self, (NetAddress) address, new Decide(pts, pl, t)), net);
                    }
                }

            }
        }
    };

    protected final ClassMatchedHandler<Decide, Message> decideHandler = new ClassMatchedHandler<Decide, Message>() {

        @Override
        public void handle(Decide decide, Message context) {
            t = Integer.max(t, decide.t) + 1;
            if (decide.ts == prepts) {
                while (al < decide.l) {
                    trigger(new ASCDecide(av.get(al)), asc);
                    al++;
                }
            }
        }
    };


    public List<Object> getPrefix(List<Object> list, int prefix) {
        List<Object> prefixList = new ArrayList<>();
        for (int i = 0; i < prefix; i++) {
            prefixList.add(list.get(i));
        }
        return prefixList;
    }

    public List<Object> getSuffix(List<Object> list, int suffix) {
        List<Object> suffixList = new ArrayList<>();
        for (int i = suffix; i < list.size(); i++) {
            suffixList.add(list.get(i));
        }
        return suffixList;
    }

    public int selfRank() {
        return self.getPort();
    }



}