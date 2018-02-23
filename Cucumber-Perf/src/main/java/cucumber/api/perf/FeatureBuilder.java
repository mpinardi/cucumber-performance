package cucumber.api.perf;

import gherkin.ast.ScenarioDefinition;

import java.util.ArrayList;
import java.util.List;

import cucumber.runner.EventBus;
import cucumber.runner.TimeService;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;

public class FeatureBuilder {

	public static List<CucumberFeature> getFeatures(Class<?> clazz) {
		ClassLoader classLoader = clazz.getClassLoader();
		ResourceLoader resourceLoader = new MultiLoader(classLoader);
		RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz);
		RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
		return runtimeOptions.cucumberFeatures(resourceLoader, new EventBus(TimeService.SYSTEM));
	}
	
	public static List<CucumberFeature> getFeatures(Class<?> clazz,List<String> args) {
		ClassLoader classLoader = clazz.getClassLoader();
		ResourceLoader resourceLoader = new MultiLoader(classLoader);
		RuntimeOptions  runtimeOptions = new RuntimeOptions(args);
		return runtimeOptions.cucumberFeatures(resourceLoader, new EventBus(TimeService.SYSTEM));
	}
	
	public static List<CucumberFeature> FindFeatures(String prefix, List<CucumberFeature> features) {
		List<CucumberFeature> result = new ArrayList<CucumberFeature>();
		for (CucumberFeature f : features) {
			if (f.getGherkinFeature().getFeature().getName().toLowerCase().startsWith(prefix)) {
				result.add(f);
			}
		}
		return result;
	}

	public static List<ScenarioDefinition> FindScenarios(String prefix, String feature,
			List<CucumberFeature> features) {
		List<ScenarioDefinition> result = new ArrayList<ScenarioDefinition>();
		for (CucumberFeature f : features) {
			if (f.getGherkinFeature().getFeature().getName().equalsIgnoreCase(feature)) {
				for (ScenarioDefinition s : f.getGherkinFeature().getFeature().getChildren()) {
					if (s.getName().startsWith(prefix)) {
						result.add(s);
					}
				}
			}
		}
		return result;
	}

	public static List<List<ScenarioDefinition>> GetScenarios(List<CucumberFeature> features) {

		List<List<ScenarioDefinition>> result = new ArrayList<List<ScenarioDefinition>>();
		for (CucumberFeature f : features) {
			List<ScenarioDefinition> sc = new ArrayList<ScenarioDefinition>();
			for (ScenarioDefinition s : f.getGherkinFeature().getFeature().getChildren()) {
				sc.add(s);
			}
			result.add(sc);
		}
		return result;
	}

}
