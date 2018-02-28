package cucumber.api.perf;

import static org.junit.Assert.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cucumber.api.perf.CucumberPerfTest.options1;
import cucumber.api.perf.salad.ast.Group;
import cucumber.api.perf.salad.ast.SimulationDefinition;
import cucumber.runtime.model.CucumberFeature;

public class PerfCucumberRunnerTest {

	@Test
	public void testPerfCucumberRunnerCucumberFeatureListOfStringSliceDuration() {
		PerfRuntimeOptionsFactory optf = new PerfRuntimeOptionsFactory(options1.class);
		PerfRuntimeOptions opt = optf.create();
		List<PerfPlan> res = PlanBuilder.LoadPlans(this.getClass(), new ArrayList<String>(opt.getPlanPaths()));
		List <PerfGroup> pg =buildGroups(res.get(0).getSaladPlan().getPlan().getChildren().get(0));
		List<CucumberFeature> features = FeatureBuilder.getFeatures(FeatureBuilder.createRuntime(options1.class));
		PerfCucumberRunner runner = new PerfCucumberRunner(features.get(0),opt.getCucumberOptions(),
				pg.get(0).getSlice(),Duration.parse("PT5.1S"));
		assertEquals("test",runner.getFeatures().get(0).getGherkinFeature().getFeature().getName());
	}

	@Test
	public void testPerfCucumberRunnerCucumberFeatureClassOfQSliceDuration() {
		PerfRuntimeOptionsFactory optf = new PerfRuntimeOptionsFactory(options1.class);
		PerfRuntimeOptions opt = optf.create();
		List<PerfPlan> res = PlanBuilder.LoadPlans(this.getClass(), new ArrayList<String>(opt.getPlanPaths()));
		List <PerfGroup> pg =buildGroups(res.get(0).getSaladPlan().getPlan().getChildren().get(0));
		List<CucumberFeature> features = FeatureBuilder.getFeatures(FeatureBuilder.createRuntime(options1.class));
		PerfCucumberRunner runner = new PerfCucumberRunner(features.get(0),options1.class,
				pg.get(0).getSlice(),Duration.parse("PT5.1S"));
		assertEquals("test",runner.getFeatures().get(0).getGherkinFeature().getFeature().getName());
	}
	
	private List<PerfGroup> buildGroups(SimulationDefinition sim) {
		List<PerfGroup> groups = new ArrayList<PerfGroup>();
		for (Group g : sim.getGroups()) {
			groups.add(new PerfGroup(g));
		}
		return groups;
	}

}
