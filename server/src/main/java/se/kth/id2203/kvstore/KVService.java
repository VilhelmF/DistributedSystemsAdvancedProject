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
    //******* Fields ******
    final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    private final HashMap<UUID, NetAddress> pending = new HashMap<>();

    //******* Handlers ******
    protected final ClassMatchedHandler<GetOperation, Message> getHandler = new ClassMatchedHandler<GetOperation, Message>() {

        @Override
        public void handle(GetOperation content, Message context) {
            LOG.info("Received a get request: " + content.key);
            pending.put(content.id, context.getSource());
            trigger(new AR_Read_Request(Integer.parseInt(content.key), content.id), atomicRegister);
        }
    };

    protected final Handler<AR_Read_Response> readResponseHandler = new Handler<AR_Read_Response>() {

        @Override
        public void handle(AR_Read_Response readResponse) {
            NetAddress src = pending.get(readResponse.id);
            pending.remove(readResponse.id);
            if (readResponse.value == null) {
                LOG.info("Sending NOT FOUND back");
                trigger(new Message(self, src, new OpResponse(readResponse.id, null, Code.NOT_FOUND)), net);
            } else {
                LOG.info("Sending OK back");
                trigger(new Message(self, src, new OpResponse(readResponse.id, (String) readResponse.value, Code.OK)), net);
            }
        }
    };

    protected final ClassMatchedHandler<PutOperation, Message> putHandler = new ClassMatchedHandler<PutOperation, Message>() {

        @Override
        public void handle(PutOperation content, Message context) {
            pending.put(content.id, context.getSource());
            trigger(new AR_Write_Request(Integer.parseInt(content.key), content.value, content.id), atomicRegister);
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

    protected final ClassMatchedHandler<CASOperation, Message> casHandler = new ClassMatchedHandler<CASOperation, Message>() {

        @Override
        public void handle(CASOperation content, Message context) {
            pending.put(content.id, context.getSource());
            trigger(new AR_CAS_Request(Integer.parseInt(content.key), content.referenceValue, content.newValue, content.id), atomicRegister);

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
        subscribe(getHandler, net);
        subscribe(putHandler, net);
        subscribe(casHandler, net);
        subscribe(readResponseHandler, atomicRegister);
        subscribe(writeResponseHandler, atomicRegister);
        subscribe(casResponseHandler, atomicRegister);
    }

}
