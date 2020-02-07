package cucumber.perf.api.plan;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cucumber.perf.api.plan.PerfPlan;
import cucumber.perf.salad.ast.Plan;
import cucumber.perf.salad.ast.SaladDocument;
import cucumber.perf.salad.ast.SimulationDefinition;
import io.cucumber.core.internal.gherkin.ast.Comment;
import io.cucumber.core.internal.gherkin.ast.Tag;


public class PerfPlanTest {


	@Test
	public void testPerfPlan() {
		List<Tag> t = new ArrayList<Tag>();
		List<SimulationDefinition> s = new ArrayList<SimulationDefinition>();
		PerfPlan pp = new PerfPlan(new SaladDocument(new Plan(t, null, null, null, "test", null, s), new ArrayList<Comment>()), URI.create("test"),"test");
		assertEquals("test",pp.getUri().toString());
	}

	@Test
	public void testGetSaladPlan() {
		List<Tag> t = new ArrayList<Tag>();
		List<SimulationDefinition> s = new ArrayList<SimulationDefinition>();
		PerfPlan pp = new PerfPlan(new SaladDocument(new Plan(t, null, null, null, "test", null,s), new ArrayList<Comment>()), URI.create("test"),"test");
		
		assertEquals("test",pp.getSaladPlan().getPlan().getName());
	}

	@Test
	public void testGetUri() {
		List<Tag> t = new ArrayList<Tag>();
		List<SimulationDefinition> s = new ArrayList<SimulationDefinition>();
		PerfPlan pp = new PerfPlan(new SaladDocument(new Plan(t, null, null, null, "test", null, s), new ArrayList<Comment>()), URI.create("test"),"test");
		
		assertEquals("test",pp.getUri().toString());
	}

}