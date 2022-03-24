package cucumber.perf.runtime;

/*import cucumber.api.Result;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestRunStarted;*/
import cucumber.perf.api.FeatureBuilder;
import cucumber.perf.api.PerfPickle;
import cucumber.perf.api.result.GroupResult;
import cucumber.perf.api.result.GroupResultListener;
import cucumber.perf.api.result.ScenarioResult;
import cucumber.perf.api.result.StepResultListener;
import cucumber.perf.api.result.TestCaseResultListener;
import cucumber.perf.salad.ast.Slice;
import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.filter.Filters;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.gherkin.ScenarioDefinition;
/*import cucumber.runner.TimeServiceEventBus;
import cucumber.runner.TimeService;
import cucumber.runner.RunnerSupplier;
import cucumber.runner.ThreadLocalRunnerSupplier;
import cucumber.runtime.BackendModuleBackendSupplier;
import cucumber.runtime.BackendSupplier;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.Env;
import cucumber.runtime.FeaturePathFeatureSupplier;
import cucumber.runtime.FeatureSupplier;
import cucumber.runtime.filter.Filters;
import cucumber.runtime.formatter.Plugins;
import cucumber.runtime.formatter.PluginFactory;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.model.Feature;
import cucumber.runtime.model.FeatureLoader;
import gherkin.ast.ScenarioDefinition;
import gherkin.events.PickleEvent;*/
//import io.cucumber.core.options.EnvironmentOptionsParser;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.plugin.PluginFactory;
import io.cucumber.core.plugin.Plugins;
import io.cucumber.core.runtime.BackendServiceLoader;
import io.cucumber.core.runtime.FeaturePathFeatureSupplier;
import io.cucumber.core.runtime.FeatureSupplier;
import io.cucumber.core.runtime.ObjectFactoryServiceLoader;
import io.cucumber.core.runtime.ObjectFactorySupplier;
import io.cucumber.core.runtime.RunnerSupplier;
import io.cucumber.core.runtime.ScanningTypeRegistryConfigurerSupplier;
import io.cucumber.core.runtime.SingletonObjectFactorySupplier;
import io.cucumber.core.runtime.ThreadLocalRunnerSupplier;
import io.cucumber.core.runtime.TypeRegistryConfigurerSupplier;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestSourceRead;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * The Cucumber Performance thread runner.
 * 
 * @author Matt Pinardi
 *
 */
public class CucumberRunner implements Callable<Object> {
	private RuntimeOptions runtimeOptions;
	private RunnerSupplier runnerSupplier;
	private io.cucumber.core.eventbus.EventBus eventBus = new io.cucumber.core.runtime.TimeServiceEventBus(Clock.systemUTC(),UUID::randomUUID);
	private Filters filters;
	private FeatureSupplier featureSupplier;
	private FeatureParser featureParser;
	private Plugins plugins;
	private GroupResultListener resultListener;
	private TestCaseResultListener testCaseResultListener;
	private StepResultListener stepResultListener;
	private Duration wait = null;
	private boolean failFast = false;
	List<ScenarioResult> scenarioResults = new ArrayList<ScenarioResult>();
	private RunnerOptions options;

	/**
	 * Cause thread to random wait between 50%+- of the mean;
	 */
	private void randomWait() {
		if (wait != null) {
			try {
				long mean = wait.toMillis();
				Thread.sleep(ThreadLocalRandom.current().nextLong((mean / 2), (mean + (mean / 2))));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
			}
		}
	}

	public CucumberRunner(RunnerOptions options, List<String> args, Duration wait, boolean failFast) {
		this.wait = wait;
		this.failFast = failFast;
		this.options = options;
		// prepare runtime options
		runtimeOptions = FeatureBuilder.createRuntimeOptions(args);
		// prepare runtime
		this.BuildRuntime();
		this.configListeners();
	}

	public CucumberRunner(RunnerOptions options, Class<?> clazz,Duration wait, boolean failFast) {
		this.wait = wait;
		this.failFast = failFast;
		this.options = options;
		// prepare runtime options
		runtimeOptions = FeatureBuilder.createRuntimeOptions(clazz);
		// prepare runtime
		this.BuildRuntime();
		this.configListeners();
	}

	private void configListeners() {
		// Pre Cucumber 4.0.0
		// trying to remove pretty but doesn't seem possible
		// you can do getPlugins() before creating the runtime to disable all plugins
		// however
		/*
		 * for (int i = 0; i <runtimeOptions.getPlugins().size();i++) {
		 * if(runtimeOptions.getPlugins().get(i).getClass().isInstance(new
		 * PluginFactory().create("pretty"))) { runtimeOptions.getPlugins().remove(i);
		 * System.out.println(""+runtimeOptions.getPlugins().get(i).getClass()); } }
		 */
		// reporter.setEventPublisher(eventBus);

		// Post Cucumber 4.0.0
		// Enable plugins
		// StepDefinitionReporter stepDefinitionReporter =
		// plugins.stepDefinitionReporter();
		// runnerSupplier.get().reportStepDefinitions(stepDefinitionReporter);

		for (Feature feature : this.options.getFeatures()) {
			 eventBus.send(new TestSourceRead(eventBus.getInstant(), feature.getUri(), feature.getSource()));
		}

		// Enable plugins
		// StepDefinitionReporter stepDefinitionReporter =
		// plugins.stepDefinitionReporter();
		// runnerSupplier.get().reportStepDefinitions(stepDefinitionReporter);

		resultListener = new GroupResultListener();
		resultListener.setEventPublisher(eventBus);
		resultListener.setGroupName(options.getGroupText());

		testCaseResultListener = new TestCaseResultListener();
		testCaseResultListener.setEventPublisher(eventBus);

		stepResultListener = new StepResultListener();
		stepResultListener.setEventPublisher(eventBus);
	}

	@Override
	public Object call() {
		List<Feature> features = this.getFeatures();
		GroupResult result = null;
		randomWait();
		try {
			start();
			for (Feature f : features) {
				runFeature(f, options.getSlice());
			}
			finish();
			result = resultListener.getResult();
			result.setChildResults(scenarioResults);
		} catch (Throwable e) {
			result = (resultListener.getResult() != null) ? resultListener.getResult()
					: new GroupResult(this.options.getGroupText(),
							new Result(Status.FAILED, Duration.ofMillis(0), e), LocalDateTime.now(), LocalDateTime.now());
			result.setChildResults(scenarioResults);
		}
		return result;
	}

    public static List<Pickle> createPickles(Feature feature, Slice slice) {
	    if (slice!= null) {
		    List<Pickle> pickles = new ArrayList<Pickle>();
		    for (Pickle pickle : feature.getPickles()) {
		    	pickles.add(new PerfPickle(pickle,slice));
		    }
		    return pickles;
	    } else {
	    	return feature.getPickles();
	    }
	}

	private void runFeature(Feature cucumberFeature, Slice slice) throws Throwable {
		List<Pickle> pickles = createPickles(cucumberFeature, slice);
		for (Pickle pickle : pickles) {
			if (filters.test(pickle)) {
				runScenario(pickle);
			}
		}
	}

	/*
	 * private void runCucumberDirect(Feature cucumberFeature) throws
	 * Throwable { resultListener.startFeature();
	 * runtime.runFeature(cucumberFeature);
	 * 
	 * if (!resultListener.getResult().isPassed()) { throw new
	 * CucumberException(resultListener.getResult().getError()); } }
	 */

	private void runScenario(Pickle pickle) throws Throwable {
		runnerSupplier.get().runPickle(pickle);

		ScenarioResult sr = testCaseResultListener.getResult();
		sr.setChildResults(stepResultListener.getResults());
		scenarioResults.add(sr);
		stepResultListener.reset();

		if (failFast && !testCaseResultListener.getResult().isPassed()) {
			throw testCaseResultListener.getResult().getError();
		}
	}

	/**
	 * Sends the test run start to Cucumber runtime eventBus.
	 */
	private void start() {
		eventBus.send(new TestRunStarted(Clock.systemUTC().instant()));
	}

	/**
	 * Sends the test run finished to Cucumber runtime eventBus.
	 */
	private void finish() {
		eventBus.send(new TestRunFinished(Clock.systemUTC().instant()));
	}

	/**
	 * Builds local runtime instead of using cucumber runtime class.
	 */
	private void BuildRuntime() {
		Supplier<ClassLoader> classLoader = CucumberRunner.class::getClassLoader;
        ObjectFactoryServiceLoader objectFactoryServiceLoader = new ObjectFactoryServiceLoader(runtimeOptions);
        ObjectFactorySupplier objectFactory = new SingletonObjectFactorySupplier(objectFactoryServiceLoader);
        BackendServiceLoader backendSupplier = new BackendServiceLoader(classLoader, objectFactory);
        TypeRegistryConfigurerSupplier typeRegistryConfigurerSupplier = new ScanningTypeRegistryConfigurerSupplier(classLoader, runtimeOptions);
        runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, eventBus, backendSupplier, objectFactory, typeRegistryConfigurerSupplier);
		// can cull out pretty here
		// Plugins orgPlugins = new Plugins(this.classLoader, new PluginFactory(),
		// this.eventBus, this.runtimeOptions);
		// this.plugins = new Plugins(classLoader, new PluginFactory(), eventBus, new
		// RuntimeOptions(new ArrayList<String>()));
		// for (final Plugin plugin : orgPlugins) {
		// if(!(plugin instanceof PrettyFormatter))
		// {
		// plugins.addPlugin(plugin);
		// }
		// }
		this.plugins = new Plugins(new PluginFactory(), runtimeOptions);
		this.plugins.setEventBusOnEventListenerPlugins(eventBus);
		this.plugins.setSerialEventBusOnEventListenerPlugins(eventBus);
		this.featureParser = new FeatureParser(UUID::randomUUID);
		this.featureSupplier = new FeaturePathFeatureSupplier(classLoader, this.runtimeOptions, this.featureParser);
		this.filters = new Filters(this.runtimeOptions);
	}

	/**
	 * @return List of detected cucumber features
	 */
	public List<Feature> getFeatures() {

		if (this.options.getFeatures() == null) {
			return featureSupplier.get();
		}
		return this.options.getFeatures();
	}

	/**
	 * @return List of detected cucumber scenarios
	 */
	public List<List<ScenarioDefinition>> getScenarios() {
		return FeatureBuilder.GetScenarios(this.getFeatures());
	}

}