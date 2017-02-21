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
import se.kth.id2203.Util.MurmurHasher;
import se.kth.id2203.kvstore.OpResponse.Code;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.kth.id2203.overlay.Routing;
import se.sics.kompics.ClassMatchedHandler;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;

import java.util.HashMap;

/**
 *
 * @author Lars Kroll <lkroll@kth.se>
 */
public class KVService extends ComponentDefinition {

    final static Logger LOG = LoggerFactory.getLogger(KVService.class);
    //******* Ports ******
    protected final Positive<Network> net = requires(Network.class);
    protected final Positive<Routing> route = requires(Routing.class);
    //******* Fields ******
    final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    private final HashMap<Integer, String> keyValueStore = new HashMap<>();

    //******* Handlers ******
    protected final ClassMatchedHandler<GetOperation, Message> getHandler = new ClassMatchedHandler<GetOperation, Message>() {

        @Override
        public void handle(GetOperation content, Message context) {

            if(keyValueStore.isEmpty()) {
                keyValueStore.put(MurmurHasher.keyToHash("asdf1"), "Hestur");
                keyValueStore.put(MurmurHasher.keyToHash("asdf2"), "Mús");
            }

            LOG.info("Got GET operation {}!", content);

            int hashedKey = MurmurHasher.keyToHash(content.key);
            String value = keyValueStore.get(hashedKey);
            if (value == null) {
                trigger(new Message(self, context.getSource(), new OpResponse(content.id, "", Code.NOT_FOUND)), net);
            }
            trigger(new Message(self, context.getSource(), new OpResponse(content.id, value, Code.OK)), net);
        }

    };

    protected final ClassMatchedHandler<PutOperation, Message> putHandler = new ClassMatchedHandler<PutOperation, Message>() {

        @Override
        public void handle(PutOperation content, Message context) {
            LOG.info("Got PUT operation {}!", content);
            int hashedKey = MurmurHasher.keyToHash(content.key);
            keyValueStore.put(hashedKey, content.value);
            trigger(new Message(self, context.getSource(), new OpResponse(content.id, "", Code.OK)), net);
        }
    };

    protected final ClassMatchedHandler<CASOperation, Message> casHandler = new ClassMatchedHandler<CASOperation, Message>() {

        @Override
        public void handle(CASOperation content, Message context) {
            LOG.info("Got CAS operation {}!", content);
            int hashedKey = MurmurHasher.keyToHash(content.key);
            String value = keyValueStore.get(hashedKey);
            if (value == null) {
                trigger(new Message(self, context.getSource(), new OpResponse(content.id, "", Code.NOT_FOUND)), net);
            } else if (!value.equals(content.referenceValue)) {
                trigger(new Message(self, context.getSource(), new OpResponse(content.id, "", Code.KEY_MISMATCH)), net);
            } else if (value.equals(content.referenceValue)) {
                keyValueStore.put(hashedKey, content.newValue);
                trigger(new Message(self, context.getSource(), new OpResponse(content.id, value, Code.OK)), net);
            }
        }
    };

    {
        subscribe(getHandler, net);
        subscribe(putHandler, net);
        subscribe(casHandler, net);
    }

}
