package cucumber.perf.api;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import cucumber.perf.api.FeatureBuilder;
import cucumber.perf.runtime.PerfRuntimeOptions;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.ScenarioDefinition;

public class FeatureBuilderTest {

	@Test
	public void testGetFeaturesClazz() {
		List<Feature> features = FeatureBuilder.getFeatures(FeatureBuilder.createRuntimeOptions(options1.class));
		assertEquals(3,features.size());
	}

	@Test
	public void testGetFeaturesClazzAndArgs() {
		PerfRuntimeOptions options = new PerfRuntimeOptions(Arrays.asList(new String[] {"-g steps","src/test/java/resources"}));
		List<Feature>  features = FeatureBuilder.getFeatures(FeatureBuilder.createRuntimeOptions(options.getCucumberOptions()));
		assertEquals(3,features.size());
	}

	@Test
	public void testFindFeatures() {
		List<Feature> features = FeatureBuilder.getFeatures(FeatureBuilder.createRuntimeOptions(options1.class));
		List<Feature> f= FeatureBuilder.FindFeatures("t",features);
		assertEquals(1,f.size());
	}

	@Test
	public void testFindScenarios() {
		List<Feature> features = FeatureBuilder.getFeatures(FeatureBuilder.createRuntimeOptions(options1.class));
		List<ScenarioDefinition> f= FeatureBuilder.FindScenarios("scenario 1", "test", features);
		assertEquals(1,f.size());
	}

	@Test
	public void testGetScenarios() {
		List<Feature> features = FeatureBuilder.getFeatures(FeatureBuilder.createRuntimeOptions(options1.class));
		List<List<ScenarioDefinition>> s = FeatureBuilder.GetScenarios(features);
		assertEquals(3,s.size());
	}

	@CucumberOptions(
			features = {"src/test/java/resources"},
			dryRun = true)
    class options1
    {
    }
}
