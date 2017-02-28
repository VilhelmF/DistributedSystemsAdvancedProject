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
 * Created by sindrikaldal on 27/02/17.
 */
@SuppressWarnings("Since15")
public class MultiPaxos extends ComponentDefinition {

    final static Logger LOG = LoggerFactory.getLogger(MultiPaxos.class);

    //******* Ports ******
    Positive<Network> net = requires(Network.class);
    Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    Negative<Paxos>  asc = provides(Paxos.class);

    //******* Fields ******
    public NavigableSet<NetAddress> topology;
    public int N;
    private int t;                                          //Logical clock.
    private int prepts;                                     //Acceptor: Prepared timestamp.
    private int ats;                                        //Acceptor: Timestamp.
    private List<Propose> av;                                //Acceptor: Accepted sequence.
    private int al;                                         //Acceptor: Length of decided seq.
    private int pts;                                        //Proposer: Timestamp.
    private List<Propose> pv;                                //Proposer: Proposed seq.
    private int pl;                                         //Proposer: Length of learned seq.
    private List<Propose> proposedValues;                    //Proposer: Values proposed while preparing.
    private HashMap<Address, PaxosReadlistValue> readlist;
    private HashMap<Address, Integer> accepted;             //Proposer's knowledge about length of acceptor's longest seq num.
    private HashMap<Address, Integer> decided;              //Proposer's knowledge about length of acceptor's longest decided seq.
    final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);


    /*
    //******* Constructor ******
    public MultiPaxos(Init init) {
        this.topology = new TreeSet<>(); // TODO assign topology
        this.N = topology.size(); // TODO assign proper N

        this.t = init.t;
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

        subscribe(proposeHandler, asc);
        subscribe(prepareHandler, net);
        subscribe(nackHandler, net);
        subscribe(prepareAckHandler, net);
        subscribe(acceptHandler, net);
        subscribe(acceptAckHandler, net);
        subscribe(decideHandler, net);

    }

    //******* Init ******
    public static class Init extends se.sics.kompics.Init<MultiPaxos> {

        public List<NetAddress> topology;
        public int N;
        public int t;
        public int prepts;
        public int ats;
        public List<Propose> av;
        public int al;
        public int pts;
        public List<Propose> pv;
        public int pl;
        public List<Propose> proposedValues;
        public HashMap<Address, PaxosReadlistValue> readlist;
        public HashMap<Address, Integer> accepted;
        public HashMap<Address, Integer> decided;

        public Init() {
            this.t = 0;
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

        }
    }
    */

    //******* Handlers ******
    protected final Handler<StartMessage> startHandler = new Handler<StartMessage>() {
        @Override
        public void handle(StartMessage startMessage) {
            LOG.info("Received Start Message");
            LOG.info("My Paxos topology: " + startMessage.topology.toString());

            // Not fully sure where to handle initialization.
            t = 0;
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

            topology = startMessage.topology;
            N = topology.size();
            for (NetAddress na : topology) {
               accepted.put(na, 0);
               decided.put(na, 0);
            }

            LOG.info("Paxos initialization finished");
        }
    };

    protected final Handler<Propose> proposeHandler = new Handler<Propose>() {

        @Override
        public void handle(Propose propose) {
            // TODO:  What is in the propose message?
            LOG.info(propose.toString());
            LOG.info(propose.method + " : " + propose.key);
            t++;
            if (pts == 0) {
                LOG.info("pts == 0");
                pts = (t * readlist.size()) + selfRank();
                pv = getPrefix(av, al);
                pl = 0;
                //proposedValues = propose.values;
                proposedValues = new ArrayList<>();
                //TODO create value message.
                proposedValues.add(propose);
                //TODO Hashmaps with topology. Readlist empty, accepted decided with 0 for each node.
                readlist.clear();
                accepted.clear();
                decided.clear();

                for (NetAddress address : topology) {
                    accepted.put(address, 0);
                    decided.put(address, 0);
                    LOG.info("Sending Prepare to : " + address);
                    trigger(new Message(self, address, new Prepare(pts, al, t, propose.uuid)), net);
                }
            }
            else if (readlist.size() <= N / 2) {
                proposedValues.add(propose);
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
            else if (!pv.contains(propose)) {
                pv.add(propose);
                for(NetAddress na : topology) {
                    if(readlist.containsKey(na)) {
                        List<Propose> tempProposeList = new ArrayList<>();
                        tempProposeList.add(propose);
                        trigger(new Message(self, na, new Accept(pts, tempProposeList, pv.size() - 1, t, propose.uuid)), net);
                    }
                }
            }
        }
    };

    protected final ClassMatchedHandler<Prepare, Message> prepareHandler = new ClassMatchedHandler<Prepare, Message>() {

        @Override
        public void handle(Prepare prepare, Message context) {
            LOG.info("Received Prepare from : " + context.getSource());
            LOG.info(prepare.toString());
            t = Integer.max(t, prepare.t2) + 1;
            if (prepare.ts < prepts) {
                LOG.info("Sending NACK to : " + context.getSource());
                trigger(new Message(self, context.getSource(), new Nack(prepare.ts, t, prepare.proposeUUID)), net);
            } else {
                prepts = prepare.ts;
                LOG.info("Sending PrepareACK to : " + context.getSource());
                trigger(new Message(self, context.getSource(), new PrepareAck(prepare.ts, ats, getSuffix(av, prepare.l), al, t, prepare.proposeUUID)), net);
            }
        }
    };

    protected final ClassMatchedHandler<Nack, Message> nackHandler = new ClassMatchedHandler<Nack, Message>() {

        @Override
        public void handle(Nack nack, Message context) {
            LOG.info("Received NACK from : " + context.getSource());
            t = Integer.max(t, nack.t) + 1;
            if (nack.pts == pts) {
                pts = 0;
                trigger(new Abort(nack.id), asc);
            }
        }
    };

    protected final ClassMatchedHandler<PrepareAck, Message> prepareAckHandler = new ClassMatchedHandler<PrepareAck, Message>() {

        @Override
        public void handle(PrepareAck prepareAck, Message context) {
            t = Integer.max(t, prepareAck.t2) + 1;
            LOG.info("Received PrepareACK from : " + context.getSource());
            if (prepareAck.pts2 == pts); {
                readlist.put(context.getSource(), new PaxosReadlistValue(prepareAck.ts, prepareAck.vsuf));
                decided.put(context.getSource(), prepareAck.l);
                if (readlist.size() == ((N / 2) + 1)) {
                    int tsTemp = 0;
                    List<Propose> vsufTemp = new ArrayList<>();
                    for (PaxosReadlistValue readListValue : readlist.values()) {
                        if (tsTemp < readListValue.getTs() || (tsTemp == readListValue.getTs() && vsufTemp.size() < readListValue.getVsuf().size())) {
                            tsTemp = readListValue.getTs();
                            vsufTemp.addAll(readListValue.getVsuf());
                        }
                    }
                    pv.addAll(vsufTemp);
                    for (Propose value : proposedValues) {
                        if (!pv.contains(value)) {
                           pv.add(value);
                        }
                    }
                    for (NetAddress address : topology) {
                        if(readlist.get(address) != null) {
                            Integer tempL = decided.get(address);
                            List<Propose> tempSuffix = getSuffix(pv, tempL);
                            trigger(new Message(self, address, new Accept(pts, tempSuffix, tempL, t, prepareAck.id)), net);
                        }
                    }
                } else if (readlist.size() > ((N/2) + 1)) {
                    trigger(new Message(self, context.getSource(), new Accept(pts, getSuffix(pv, prepareAck.l), prepareAck.l, t, prepareAck.id)), net);
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
                trigger(new Message(self, context.getSource(), new Nack(accept.pts, t, accept.id)), net);
            }
            else {
                ats = accept.pts;
                if (accept.pvLength < av.size()) {
                    av = getPrefix(av, accept.pvLength);
                }
                av.addAll(accept.values);
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

    {
        subscribe(startHandler, asc);
        subscribe(proposeHandler, asc);
        subscribe(prepareHandler, net);
        subscribe(nackHandler, net);
        subscribe(prepareAckHandler, net);
        subscribe(acceptHandler, net);
        subscribe(acceptAckHandler, net);
        subscribe(decideHandler, net);
    }

    public List<Propose> getPrefix(List<Propose> list, int prefix) {
        List<Propose> prefixList = new ArrayList<>();
        for (int i = 0; i < prefix; i++) {
            prefixList.add(list.get(i));
        }
        return prefixList;
    }

    public List<Propose> getSuffix(List<Propose> list, int suffix) {
        List<Propose> suffixList = new ArrayList<>();
        for (int i = suffix; i < list.size(); i++) {
            suffixList.add(list.get(i));
        }
        return suffixList;
    }

    public int selfRank() {
        return self.getIp().hashCode();
    }

}
