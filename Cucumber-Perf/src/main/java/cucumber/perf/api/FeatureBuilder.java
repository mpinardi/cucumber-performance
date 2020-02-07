package cucumber.perf.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Node;
import io.cucumber.core.gherkin.ScenarioDefinition;
import io.cucumber.core.options.CommandlineOptionsParser;
import io.cucumber.core.options.CucumberOptionsAnnotationParser;
import io.cucumber.core.options.CucumberProperties;
import io.cucumber.core.options.CucumberPropertiesParser;
import io.cucumber.core.options.RuntimeOptions;

import io.cucumber.core.runtime.FeaturePathFeatureSupplier;

public class FeatureBuilder {

	public static RuntimeOptions createRuntimeOptions(Class<?> clazz) {
		RuntimeOptions propertiesFileOptions = new CucumberPropertiesParser().parse(CucumberProperties.fromPropertiesFile()).build();
		RuntimeOptions systemOptions = new CucumberPropertiesParser().parse(CucumberProperties.fromSystemProperties()) .build(propertiesFileOptions);
		RuntimeOptions envOptions = new CucumberPropertiesParser().parse(CucumberProperties.fromEnvironment()).build(systemOptions);
		RuntimeOptions runtimeOptions = new CucumberOptionsAnnotationParser().withOptionsProvider(new CucumberOptionsProvider()).parse(clazz).build(envOptions);
		return runtimeOptions;
	}

	public static RuntimeOptions createRuntimeOptions(List<String> args) {
		RuntimeOptions propertiesFileOptions = new CucumberPropertiesParser().parse(CucumberProperties.fromPropertiesFile()).build();
		RuntimeOptions systemOptions = new CucumberPropertiesParser().parse(CucumberProperties.fromSystemProperties()) .build(propertiesFileOptions);
		RuntimeOptions envOptions = new CucumberPropertiesParser().parse(CucumberProperties.fromEnvironment()).build(systemOptions);
		RuntimeOptions runtimeOptions = new CommandlineOptionsParser().parse(args).build(envOptions);
		return runtimeOptions;
	}

	public static List<Feature> getFeatures(RuntimeOptions runtimeOptions) {
		Supplier<ClassLoader> classLoader = FeatureBuilder.class::getClassLoader;
		FeatureParser parser = new FeatureParser(UUID::randomUUID);
		return new FeaturePathFeatureSupplier(classLoader, runtimeOptions,parser).get();
	}

	public static List<Feature> FindFeatures(String prefix, List<Feature> features) {
		List<Feature> result = new ArrayList<Feature>();
		for (Feature f : features) {
			if (f.getName().toLowerCase().startsWith(prefix)) {
				result.add(f);
			}
		}
		return result;
	}

	public static List<ScenarioDefinition> FindScenarios(String prefix, String feature,
			List<Feature> features) {
		List<ScenarioDefinition> result = new ArrayList<ScenarioDefinition>();
		for (Feature f : features) {
			if (f.getName().equalsIgnoreCase(feature)) {
				for (Iterator<Node> iterator = f.children().iterator(); iterator.hasNext();) {
					Node n = iterator.next();
					 if (n instanceof ScenarioDefinition) {
						 if (((ScenarioDefinition)n).getName().startsWith(prefix)) {
							result.add(((ScenarioDefinition)n));
						}
					 }
				}
			}
		}
		return result;
	}

	public static List<List<ScenarioDefinition>> GetScenarios(List<Feature> features) {
		List<List<ScenarioDefinition>> result = new ArrayList<List<ScenarioDefinition>>();
		for (Feature f : features) {
			List<ScenarioDefinition> sc = new ArrayList<ScenarioDefinition>();
			for (Iterator<Node> iterator = f.children().iterator(); iterator.hasNext();) {
				Node n = iterator.next();
				 if (n instanceof ScenarioDefinition) {
						sc.add(((ScenarioDefinition)n));
				 }
			}
			result.add(sc);
		}
		return result;
	}

}
