package se.kth.id2203.simulation.atomic_register;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.simulation.SimulationResultMap;
import se.kth.id2203.simulation.SimulationResultSingleton;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.run.LauncherComp;



/**
 * Created by vilhelm on 2017-02-27.
 */
public class RegisterTest {

    private final SimulationResultMap res = SimulationResultSingleton.getInstance();
    final static Logger LOG = LoggerFactory.getLogger(RegisterTest.class);

    @Test
    public void simpleBroadcastTest() {
        long seed = 123;
        SimulationScenario.setSeed(seed);
        SimulationScenario simpleBootScenario = ScenarioGenRegister.atomicRegister(5);
        simpleBootScenario.simulate(LauncherComp.class);
        LOG.info("Finished the test!");
    }
}



