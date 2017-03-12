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
package se.kth.id2203.simulation;

import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.run.LauncherComp;

/**
 *
 * @author Lars Kroll <lkroll@kth.se>
 */
public class OpsTest {

    private static final int NUM_MESSAGES = 4;
    private final SimulationResultMap res = SimulationResultSingleton.getInstance();
    final static Logger LOG = LoggerFactory.getLogger(OpsTest.class);


    @Test
    public void simpleGetTest() {
        long seed = 123;
        SimulationScenario.setSeed(seed);
        SimulationScenario simpleBootScenario = ScenarioGen.simpleOps(3, 1);
        res.put("messages", NUM_MESSAGES);
        simpleBootScenario.simulate(LauncherComp.class);
        for (int i = 0; i < NUM_MESSAGES; i++) {
            LOG.info(res.get(""+i, String.class));
            Assert.assertEquals("NOT_FOUND", res.get("" + i, String.class));
        }
    }

    /**
     * Tests PUT, GET and CAS functions.
     */
    @Test
    public void simpleOperationsTest() {
        long seed = 123;
        SimulationScenario.setSeed(seed);
        SimulationScenario simpleBootScenario = ScenarioGen.simpleOps(6, 1);
        res.put("messages", NUM_MESSAGES);
        simpleBootScenario.simulate(LauncherComp.class);
        for (int i = 0; i < NUM_MESSAGES; i++) {
            LOG.info(res.get(""+i, String.class));
            Assert.assertEquals("OK", res.get("" + i, String.class));
        }
        for (int i = NUM_MESSAGES; i < NUM_MESSAGES*2; i++) {
            LOG.info(res.get(Integer.toString(i), String.class));
            int value = i - NUM_MESSAGES;
            LOG.info("Key to get:" + i + " expected value: " + value);
            Assert.assertEquals("Value: " + value, res.get("" + i, String.class));
        }
        for(int i = NUM_MESSAGES * 2; i < NUM_MESSAGES*3; i++) {
            int value = i - NUM_MESSAGES * 2;
            LOG.info("Key to get:" + i + " expected value: " + value + "0");
            String eVal = value + "0";
            Assert.assertEquals(eVal, res.get("" + i, String.class));
        }
    }


}
