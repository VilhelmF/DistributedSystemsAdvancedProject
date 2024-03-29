/*
 * The MIT License
 *
 * Copyright 2017 Lars Kroll <lkroll@kth.se>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package se.kth.id2203.kvstore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.paxos.ASCDecide;
import se.kth.id2203.paxos.Abort;
import se.kth.id2203.paxos.Paxos;
import se.kth.id2203.paxos.Propose;
import se.kth.id2203.atomicregister.*;
import se.kth.id2203.kvstore.OpResponse.Code;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.kth.id2203.overlay.Routing;
import se.sics.kompics.ClassMatchedHandler;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;

import java.util.HashMap;
import java.util.UUID;

/**
 *
 * @author Lars Kroll <lkroll@kth.se>
 */
public class KVService extends ComponentDefinition {

    final static Logger LOG = LoggerFactory.getLogger(KVService.class);
    //******* Ports ******
    protected final Positive<Network> net = requires(Network.class);
    protected final Positive<Routing> route = requires(Routing.class);
    protected final Positive<AtomicRegister> atomicRegister = requires(AtomicRegister.class);
    protected final Positive<Paxos> asc = requires(Paxos.class);
    //******* Fields ******
    final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    private final HashMap<UUID, NetAddress> pending = new HashMap<>();
    private final HashMap<Integer, String> keyValueStore = new HashMap<>();

    //******* Handlers ******
    protected final ClassMatchedHandler<GetOperation, Message> getHandler = new ClassMatchedHandler<GetOperation, Message>() {

        @Override
        public void handle(GetOperation content, Message context) {
            pending.put(content.id, context.getSource());
            try {
                Propose p = new Propose(content.id, "GET", Integer.parseInt(content.key), null, null);
                trigger(p, asc);
            } catch (Exception e) {
                pending.remove(content.id);
                trigger(new Message(self, context.getSource(), new OpResponse(content.id, null, Code.ABORT)), net);
            }
        }
    };

    protected final ClassMatchedHandler<PutOperation, Message> putHandler = new ClassMatchedHandler<PutOperation, Message>() {

        @Override
        public void handle(PutOperation content, Message context) {
            pending.put(content.id, context.getSource());
            try {
                Propose p = new Propose(content.id, "PUT", Integer.parseInt(content.key), content.value, null);
                trigger(p, asc);
            } catch (Exception e) {
                pending.remove(content.id);
                trigger(new Message(self, context.getSource(), new OpResponse(content.id, null, Code.ABORT)), net);
            }
        }
    };

    protected final ClassMatchedHandler<CASOperation, Message> casHandler = new ClassMatchedHandler<CASOperation, Message>() {

        @Override
        public void handle(CASOperation content, Message context) {
            pending.put(content.id, context.getSource());
            try {
                Propose p = new Propose(content.id, "CAS", Integer.parseInt(content.key), content.newValue, content.referenceValue);
                trigger(p, asc);
            } catch (Exception e) {
                pending.remove(content.id);
                trigger(new Message(self, context.getSource(), new OpResponse(content.id, null, Code.ABORT)), net);
            }
        }
    };

    protected  final Handler<Abort> abortHandler = new Handler<Abort>() {
        @Override
        public void handle(Abort abort) {
            NetAddress src = pending.get(abort.id);
            pending.remove(abort.id);
            trigger(new Message(self, src, new OpResponse(abort.id, null, Code.ABORT)), net);
        }
    };

    protected final Handler<ASCDecide> ascDecideHandler = new Handler<ASCDecide>() {
        @Override
        public void handle(ASCDecide ascDecide) {
            UUID id = ascDecide.propose.uuid;
            NetAddress src = pending.get(id);
            String method = ascDecide.propose.method;
            int key = ascDecide.propose.key;
            if (method.equals("GET")) {
                String value = keyValueStore.get(key);
                if(src != null) {
                    if(value != null) {
                        trigger(new Message(self, src, new OpResponse(id, value, Code.OK)), net);
                    }
                    else {
                        trigger(new Message(self, src, new OpResponse(id, value, Code.NOT_FOUND)), net);
                    }
                    pending.remove(id);
                }
            } else if (method.equals("PUT")) {
                keyValueStore.put(key, ascDecide.propose.value);
                if(src != null) {
                    pending.remove(id);
                    trigger(new Message(self, src, new OpResponse(id, null, Code.OK)), net);
                }
            } else if (method.equals("CAS")) {
                boolean validCAS = ascDecide.propose.reference.equals(keyValueStore.get(key));
                if(validCAS) {
                    keyValueStore.put(key, ascDecide.propose.value);
                }
                if(src != null) {
                    if(validCAS) {
                        trigger(new Message(self, src, new OpResponse(id, null, Code.OK)), net);
                    } else {
                        trigger(new Message(self, src, new OpResponse(id, null, Code.KEY_MISMATCH)), net);
                    }
                    pending.remove(id);
                }
            }
        }
    };

    protected final Handler<AR_Read_Response> readResponseHandler = new Handler<AR_Read_Response>() {

        @Override
        public void handle(AR_Read_Response readResponse) {
            NetAddress src = pending.get(readResponse.id);
            pending.remove(readResponse.id);
            if (readResponse.value == null) {
                trigger(new Message(self, src, new OpResponse(readResponse.id, null, Code.NOT_FOUND)), net);
            } else {
                trigger(new Message(self, src, new OpResponse(readResponse.id, (String) readResponse.value, Code.OK)), net);
            }
        }
    };

    protected final Handler<AR_Write_Response> writeResponseHandler = new Handler<AR_Write_Response>() {

        @Override
        public void handle(AR_Write_Response writeResponse) {
            NetAddress src = pending.get(writeResponse.opId);
            pending.remove(writeResponse.opId);
            trigger(new Message(self, src, new OpResponse(writeResponse.opId, null, Code.OK)), net);
        }
    };


    protected final Handler<AR_CAS_Response> casResponseHandler = new Handler<AR_CAS_Response>() {

        @Override
        public void handle(AR_CAS_Response casResponse) {
            NetAddress src = pending.get(casResponse.opId);
            pending.remove(casResponse.opId);
            trigger(new Message(self, src, new OpResponse(casResponse.opId, null, casResponse.opStatus)), net);
        }
    };


    {
        subscribe(abortHandler, asc);
        subscribe(getHandler, net);
        subscribe(putHandler, net);
        subscribe(casHandler, net);
        subscribe(ascDecideHandler, asc);
        subscribe(readResponseHandler, atomicRegister);
        subscribe(writeResponseHandler, atomicRegister);
        subscribe(casResponseHandler, atomicRegister);
    }

}
