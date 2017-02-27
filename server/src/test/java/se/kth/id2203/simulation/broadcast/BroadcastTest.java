package se.kth.id2203.simulation.broadcast;

import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.simulation.ScenarioGen;
import se.kth.id2203.simulation.SimulationResultMap;
import se.kth.id2203.simulation.SimulationResultSingleton;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.run.LauncherComp;

/**
 * Created by sindrikaldal on 26/02/17.
 */
public class BroadcastTest {

    private int NUM_MESSAGES = 10;

    private final SimulationResultMap res = SimulationResultSingleton.getInstance();
    final static Logger LOG = LoggerFactory.getLogger(BroadcastTest.class);

    @Test
    public void BestEffortBroadcastTest() {
        long seed = 123;
        SimulationScenario.setSeed(seed);
        SimulationScenario simpleBootScenario = ScenarioGenBroadcast.simpleBroadcast(NUM_MESSAGES);
        simpleBootScenario.simulate(LauncherComp.class);
        Assert.assertEquals(Integer.toString(NUM_MESSAGES), res.get("broadcast", String.class));
    }
}
