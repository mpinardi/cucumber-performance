package cucumber.perf.runtime;

import static org.junit.Assert.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cucumber.api.CucumberOptions;
import cucumber.perf.api.CucumberPerfOptions;
import cucumber.perf.api.FeatureBuilder;
import cucumber.perf.api.PerfGroup;
import cucumber.perf.api.PerfPlan;
import cucumber.perf.api.PlanBuilder;
import cucumber.perf.api.result.GroupResult;
import cucumber.perf.runtime.CucumberPerfTest.options1;
import cucumber.perf.runtime.filter.FeatureFilter;
import cucumber.perf.runtime.CucumberRunner;
import cucumber.perf.runtime.PerfRuntimeOptions;
import cucumber.perf.runtime.PerfRuntimeOptionsFactory;
import cucumber.perf.salad.ast.Group;
import cucumber.perf.salad.ast.SimulationDefinition;

public class PerfCucumberRunnerTest {

	@Test
	public void testPerfCucumberRunnerCucumberFeatureListOfStringSliceDuration() {
		PerfRuntimeOptionsFactory optf = new PerfRuntimeOptionsFactory(options1.class);
		PerfRuntimeOptions opt = optf.create();
		List<PerfPlan> res = PlanBuilder.LoadPlans(this.getClass(), new ArrayList<String>(opt.getPlanPaths()));
		List <PerfGroup> pg =buildGroups(res.get(0).getSaladPlan().getPlan().getChildren().get(0));
		RunnerOptions ro = new RunnerOptions(pg.get(1));
		ro.setFeatures(FeatureBuilder.getFeatures(FeatureBuilder.createRuntimeOptions(options1.class)));
		CucumberRunner runner = new CucumberRunner(ro,opt.getCucumberOptions(),Duration.parse("PT5.1S"),false);
		assertEquals("test",runner.getFeatures().get(2).getGherkinFeature().getFeature().getName());
	}

	@Test
	public void testPerfCucumberRunnerCucumberFeatureClassOfQSliceDuration() {
		PerfRuntimeOptionsFactory optf = new PerfRuntimeOptionsFactory(options1.class);
		PerfRuntimeOptions opt = optf.create();
		List<PerfPlan> res = PlanBuilder.LoadPlans(this.getClass(), new ArrayList<String>(opt.getPlanPaths()));
		List <PerfGroup> pg =buildGroups(res.get(0).getSaladPlan().getPlan().getChildren().get(0));
		RunnerOptions ro = new RunnerOptions(pg.get(1));
		ro.setFeatures(FeatureBuilder.getFeatures(FeatureBuilder.createRuntimeOptions(options1.class)));
		CucumberRunner runner = new CucumberRunner(ro,options1.class,Duration.parse("PT5.1S"),false);
		assertEquals("test",runner.getFeatures().get(2).getGherkinFeature().getFeature().getName());
	}
	
	@Test
	public void testCucumberRunnerFailFast() {
		PerfRuntimeOptionsFactory optf = new PerfRuntimeOptionsFactory(optionsff.class);
		PerfRuntimeOptions opt = optf.create();
		List<PerfPlan> res = PlanBuilder.LoadPlans(this.getClass(), new ArrayList<String>(opt.getPlanPaths()));
		List<PerfGroup> pg =buildGroups(res.get(0).getSaladPlan().getPlan().getChildren().get(4));
		RunnerOptions ro = new RunnerOptions(pg.get(0));
		FeatureFilter featureFilter = new FeatureFilter(FeatureBuilder.getFeatures(FeatureBuilder.createRuntimeOptions(optionsff.class)));
		ro.setFeatures(featureFilter.filter(pg.get(0).getText()));
		CucumberRunner runner = new CucumberRunner(ro,optionsff.class,Duration.parse("PT5.1S"),true);
		GroupResult result = (GroupResult) runner.call();
		assertEquals("fail test",runner.getFeatures().get(0).getGherkinFeature().getFeature().getName());
		assertEquals(result.getChildResults().size(),1);
		assertEquals(result.getChildResults().get(0).getChildResults().size(),2);
	}
	
	@Test
	public void testCucumberRunnerNotFailFast() {
		PerfRuntimeOptionsFactory optf = new PerfRuntimeOptionsFactory(options1.class);
		PerfRuntimeOptions opt = optf.create();
		List<PerfPlan> res = PlanBuilder.LoadPlans(this.getClass(), new ArrayList<String>(opt.getPlanPaths()));
		List<PerfGroup> pg =buildGroups(res.get(0).getSaladPlan().getPlan().getChildren().get(4));
		RunnerOptions ro = new RunnerOptions(pg.get(0));
		FeatureFilter featureFilter = new FeatureFilter(FeatureBuilder.getFeatures(FeatureBuilder.createRuntimeOptions(options1.class)));
		ro.setFeatures(featureFilter.filter(pg.get(0).getText()));
		CucumberRunner runner = new CucumberRunner(ro,options1.class,Duration.parse("PT5.1S"),true);
		GroupResult result = (GroupResult) runner.call();
		assertEquals("fail test",runner.getFeatures().get(0).getGherkinFeature().getFeature().getName());
		assertEquals(result.getChildResults().size(),3);
	}
	
	private List<PerfGroup> buildGroups(SimulationDefinition sim) {
		List<PerfGroup> groups = new ArrayList<PerfGroup>();
		for (Group g : sim.getGroups()) {
			groups.add(new PerfGroup(g));
		}
		return groups;
	}
	
	@CucumberPerfOptions(
			plans = {"src/test/java/resources"},
			tags = {"@failFastTest"},
			failfast = true)
	@CucumberOptions(
			features = {"src/test/java/resources"},
			glue = { "steps" })
	public class optionsff
    {
    }

}
