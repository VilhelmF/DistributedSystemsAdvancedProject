package se.kth.id2203.simulation.epfd;

import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.failuredetector.EPFD;
import se.kth.id2203.simulation.SimulationResultMap;
import se.kth.id2203.simulation.SimulationResultSingleton;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.run.LauncherComp;

/**
 * Created by sindrikaldal on 26/02/17.
 */
@SuppressWarnings("Duplicates")
public class EPFDTest extends ComponentDefinition{



    private final SimulationResultMap res = SimulationResultSingleton.getInstance();
    final static Logger LOG = LoggerFactory.getLogger(EPFD.class);

    @Test
    public void strongCompletenessTest() {

        int NUM_SERVERS = 10;
        int NUM_KILLS = 2;

        long seed = 123;
        SimulationScenario.setSeed(seed);
        SimulationScenario simpleBootScenario = EPDFScenarioGen.failureDetectorSimulation(NUM_SERVERS, NUM_KILLS);
        simpleBootScenario.simulate(LauncherComp.class);
        Assert.assertEquals(Integer.toString(NUM_KILLS), res.get("epfd", String.class));
    }

    @Test
    public void eventuallyStrongAccuracyTest() {

        int NUM_SERVERS = 10;
        int NUM_KILLS = 2;

        long seed = 123;
        SimulationScenario.setSeed(seed);
        SimulationScenario simpleBootScenario = EPDFScenarioGen.failureDetectorSimulation(NUM_SERVERS, NUM_KILLS);
        simpleBootScenario.simulate(LauncherComp.class);
        Assert.assertEquals(Integer.toString(NUM_KILLS), res.get("epfd-suspects", String.class));
    }
}
