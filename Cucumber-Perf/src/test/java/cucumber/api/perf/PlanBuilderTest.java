package cucumber.api.perf;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cucumber.api.perf.CucumberPerfTest.options1;
import cucumber.api.perf.salad.ast.SimulationDefinition;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;

public class PlanBuilderTest {

	@Test
	public void testParse() {
		PerfRuntimeOptionsFactory optf = new PerfRuntimeOptionsFactory(options1.class);
		PerfRuntimeOptions opt = optf.create();
		ClassLoader classLoader = this.getClass().getClassLoader();
		ResourceLoader resourceLoader = new MultiLoader(classLoader);
	
		List<PerfPlan> plans = new ArrayList<PerfPlan>();
		PlanBuilder pb = new PlanBuilder(plans);
		Iterable<Resource> resources = resourceLoader.resources(opt.getPlanPaths().get(0), ".plan");
		for (Resource r : resources)
		{
			pb.parse(r);
		}
		assertEquals(1,plans.size());	
	}

	@Test
	public void testRead() {
		PerfRuntimeOptionsFactory optf = new PerfRuntimeOptionsFactory(options1.class);
		PerfRuntimeOptions opt = optf.create();
		ClassLoader classLoader = this.getClass().getClassLoader();
		ResourceLoader resourceLoader = new MultiLoader(classLoader);
	
		List<PerfPlan> plans = new ArrayList<PerfPlan>();
		PlanBuilder pb = new PlanBuilder(plans);
		Iterable<Resource> resources = resourceLoader.resources(opt.getPlanPaths().get(0), ".plan");
		for (Resource r : resources)
		{
			assertTrue(!pb.read(r).isEmpty());
		}
		
	}

	@Test
	public void testLoadPlans() {
		PerfRuntimeOptionsFactory optf = new PerfRuntimeOptionsFactory(options1.class);
		PerfRuntimeOptions opt = optf.create();
	
		List<PerfPlan> plans = new ArrayList<PerfPlan>();
		plans = PlanBuilder.LoadPlans(options1.class, opt.getPlanPaths());
		assertEquals(1,plans.size());
	}

	@Test
	public void testFindSimulation() {
		PerfRuntimeOptionsFactory optf = new PerfRuntimeOptionsFactory(options1.class);
		PerfRuntimeOptions opt = optf.create();
	
		List<PerfPlan> plans = new ArrayList<PerfPlan>();
		plans = PlanBuilder.LoadPlans(options1.class, opt.getPlanPaths());
		List<PerfPlan> p = PlanBuilder.FindPlan("test", plans);
		assertEquals(1,p.size());
	}

	@Test
	public void testFindScenarios() {
		PerfRuntimeOptionsFactory optf = new PerfRuntimeOptionsFactory(options1.class);
		PerfRuntimeOptions opt = optf.create();
	
		List<PerfPlan> plans = new ArrayList<PerfPlan>();
		plans = PlanBuilder.LoadPlans(options1.class, opt.getPlanPaths());
		List<SimulationDefinition> p = PlanBuilder.FindSimulations("simulation 1", "test",plans);
		assertEquals(2,p.size());
	}

	@Test
	public void testGetSimulations() {
		PerfRuntimeOptionsFactory optf = new PerfRuntimeOptionsFactory(options1.class);
		PerfRuntimeOptions opt = optf.create();
	
		List<PerfPlan> plans = new ArrayList<PerfPlan>();
		plans = PlanBuilder.LoadPlans(options1.class, opt.getPlanPaths());
		List<List<SimulationDefinition>> p = PlanBuilder.GetSimulations(plans);
		assertEquals(1,p.size());
		assertEquals(4,p.get(0).size());
	}

}
