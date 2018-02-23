package cucumber.api.perf;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cucumber.api.perf.CucumberPerfTest.options1;
import cucumber.api.perf.salad.ast.Plan;
import cucumber.api.perf.salad.ast.SaladDocument;
import cucumber.api.perf.salad.ast.SimulationDefinition;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import gherkin.ast.Comment;
import gherkin.ast.Tag;

public class PerfPlanTest {


	@Test
	public void testLoadResourceLoaderListOfString() {
		PerfRuntimeOptionsFactory optf = new PerfRuntimeOptionsFactory(options1.class);
		PerfRuntimeOptions opt = optf.create();
		ClassLoader classLoader = this.getClass().getClassLoader();
		ResourceLoader resourceLoader = new MultiLoader(classLoader);
		List<PerfPlan> p = PerfPlan.load(resourceLoader, opt.getPlanPaths());
		assertEquals("test",p.get(0).getSaladPlan().getPlan().getName());
	}

	@Test
	public void testPerfPlan() {
		List<Tag> t = new ArrayList<Tag>();
		List<SimulationDefinition> s = new ArrayList<SimulationDefinition>();
		PerfPlan pp = new PerfPlan(new SaladDocument(new Plan(t, null, null, null, "test", null, s), new ArrayList<Comment>()), "test","test");
		assertEquals("test",pp.getUri());
	}

	@Test
	public void testGetSaladPlan() {
		List<Tag> t = new ArrayList<Tag>();
		List<SimulationDefinition> s = new ArrayList<SimulationDefinition>();
		PerfPlan pp = new PerfPlan(new SaladDocument(new Plan(t, null, null, null, "test", null,s), new ArrayList<Comment>()), "test","test");
		
		assertEquals("test",pp.getSaladPlan().getPlan().getName());
	}

	@Test
	public void testGetUri() {
		List<Tag> t = new ArrayList<Tag>();
		List<SimulationDefinition> s = new ArrayList<SimulationDefinition>();
		PerfPlan pp = new PerfPlan(new SaladDocument(new Plan(t, null, null, null, "test", null, s), new ArrayList<Comment>()), "test","test");
		
		assertEquals("test",pp.getUri());
	}

}