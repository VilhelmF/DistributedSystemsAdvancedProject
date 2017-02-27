package se.kth.id2203.Paxos;

import com.sun.org.apache.xpath.internal.operations.Mult;
import jdk.management.resource.internal.inst.FileOutputStreamRMHooks;
import se.kth.id2203.ReadWrite.ReadListValue;
import se.kth.id2203.broadcasting.BEB_Broadcast;
import se.kth.id2203.broadcasting.BestEffortBroadcast;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.*;
import se.sics.kompics.network.Address;
import se.sics.kompics.network.Network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sindrikaldal on 27/02/17.
 */
public class MultiPaxos extends ComponentDefinition {

    //******* Ports ******
    Positive<Network> net = requires(Network.class);
    Negative<AbortableSequenceConsensus> asc = provides(AbortableSequenceConsensus.class);
    Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);

    //******* Fields ******
    public List<NetAddress> topology = new ArrayList<>();
    public int N;
    private int t;
    private int prepts;
    private int ats;
    private List<Object> av;
    private int al;
    private int pts;
    private List<Object> pv;
    private int pl;
    private List<Object> proposedValues;
    private HashMap<Address, PaxosReadlistValue> readlist;
    private HashMap<Address, Integer> accepted;
    private HashMap<Address, Integer> decided;
    final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);

    //******* Constructor ******
    public MultiPaxos(Init init) {
        this.topology = new ArrayList<>(); // TODO assign topology
        this.N = 3; // TODO assign proper N
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
        public List<Object> av;
        public int al;
        public int pts;
        public List<Object> pv;
        public int pl;
        public List<Object> proposedValues;
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
            this.readlist = new HashMap<>();
            this.accepted = new HashMap<>();
            this.decided = new HashMap<>();

        }
    }

    //******* Handlers ******
    protected final ClassMatchedHandler<Propose, Message> proposeHandler = new ClassMatchedHandler<Propose, Message>() {

        @Override
        public void handle(Propose propose, Message context) {
            t++;
            if (pts == 0) {
                pts = (t * readlist.size()) + selfRank();
                pv = getPrefix(av, al);
                pl = 0;
                proposedValues = propose.values;
                readlist.clear();
                accepted.clear();
                decided.clear();
                for (NetAddress address : topology) {
                    trigger(new Message(self, address, new Prepare(pts, al, t)), net);
                }
            }
            else if (readlist.size() <= N / 2) {
                for (Object obj : propose.values) {
                    proposedValues.add(obj);
                }
            } else if (!pv.equals(propose.values)) {
                for (Object obj : propose.values) {
                    pv.add(obj);
                }
                for (NetAddress address : topology) {
                    trigger(new Message(self, address, new Accept(pts, propose.values, pv.size() - 1, t)), net);
                }
            }
        }
    };

    protected final ClassMatchedHandler<Prepare, Message> prepareHandler = new ClassMatchedHandler<Prepare, Message>() {

        @Override
        public void handle(Prepare prepare, Message context) {
            t = Integer.max(t, prepare.t) + 1;
            if (prepare.pts < prepts) {
                trigger(new Message(self, context.getSource(), new Nack(prepare.pts, t)), net);
            } else {
                prepts = prepare.pts;
                trigger(new Message(self, context.getSource(), new PrepareAck(prepare.pts, ats, getSuffix(av, prepare.al), al, t)), net);
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

                        }
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
                for (Object obj : accept.values) {
                    av.add(obj);
                }
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
        List<Object> prefixList = new ArrayList<>();
        for (int i = suffix; i < list.size(); i++) {
            prefixList.add(list.get(i));
        }
        return prefixList;
    }

    public int selfRank() {
        return self.getPort();
    }



}
