package cucumber.perf.runtime;

import cucumber.api.Result;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestRunStarted;
import cucumber.perf.api.FeatureBuilder;
import cucumber.perf.api.PerfCompiler;
import cucumber.perf.api.result.GroupResult;
import cucumber.perf.api.result.GroupResultListener;
import cucumber.perf.api.result.ScenarioResult;
import cucumber.perf.api.result.StepResultListener;
import cucumber.perf.api.result.TestCaseResultListener;
import cucumber.perf.salad.ast.Slice;
import cucumber.runner.TimeServiceEventBus;
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
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.FeatureLoader;
import gherkin.ast.ScenarioDefinition;
import gherkin.events.PickleEvent;
import io.cucumber.core.options.CommandlineOptionsParser;
import io.cucumber.core.options.CucumberOptionsAnnotationParser;
import io.cucumber.core.options.EnvironmentOptionsParser;
import io.cucumber.core.options.RuntimeOptions;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The Cucumber Performance thread runner.
 * 
 * @author Matt Pinardi
 *
 */
public class CucumberRunner implements Callable<Object> {
	private RuntimeOptions runtimeOptions;
	private RunnerSupplier runnerSupplier;
	private cucumber.runner.EventBus eventBus = new TimeServiceEventBus(TimeService.SYSTEM);
	private Filters filters;
	private FeatureSupplier featureSupplier;
	@SuppressWarnings("unused")
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
		ResourceLoader resourceLoader = new MultiLoader(Thread.currentThread().getContextClassLoader());
		runtimeOptions = new CommandlineOptionsParser(resourceLoader).parse(args).build();
		new EnvironmentOptionsParser(resourceLoader).parse(Env.INSTANCE).build(runtimeOptions);
		// prepare runtime
		this.BuildRuntime();
		this.configListeners();
	}

	public CucumberRunner(RunnerOptions options, Class<?> clazz,Duration wait, boolean failFast) {
		this.wait = wait;
		this.failFast = failFast;
		this.options = options;
		// prepare runtime options
		ResourceLoader resourceLoader = new MultiLoader(Thread.currentThread().getContextClassLoader());
		runtimeOptions = new CucumberOptionsAnnotationParser(resourceLoader).parse(clazz).build();
		new EnvironmentOptionsParser(resourceLoader).parse(Env.INSTANCE).build(runtimeOptions);
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

		for (CucumberFeature feature : this.options.getFeatures()) {
			feature.sendTestSourceRead(eventBus);
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
		List<CucumberFeature> features = this.getFeatures();
		GroupResult result = null;
		randomWait();
		try {
			start();
			for (CucumberFeature f : features) {
				runFeature(f, options.getSlice());
			}
			finish();
			result = resultListener.getResult();
			result.setChildResults(scenarioResults);
		} catch (Throwable e) {
			result = (resultListener.getResult() != null) ? resultListener.getResult()
					: new GroupResult(this.options.getGroupText(),
							new Result(Result.Type.FAILED, (long) 0, e), LocalDateTime.now(), LocalDateTime.now());
			result.setChildResults(scenarioResults);
		}
		return result;
	}

	private void runFeature(CucumberFeature cucumberFeature, Slice slice) throws Throwable {
		PerfCompiler compiler = new PerfCompiler();
		List<PickleEvent> pickles = compiler.compileFeature(cucumberFeature, slice);
		for (PickleEvent pickle : pickles) {
			if (filters.matchesFilters(pickle)) {
				runScenario(pickle);
			}
		}
	}

	/*
	 * private void runCucumberDirect(CucumberFeature cucumberFeature) throws
	 * Throwable { resultListener.startFeature();
	 * runtime.runFeature(cucumberFeature);
	 * 
	 * if (!resultListener.getResult().isPassed()) { throw new
	 * CucumberException(resultListener.getResult().getError()); } }
	 */

	private void runScenario(PickleEvent pickle) throws Throwable {
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
		eventBus.send(new TestRunStarted(eventBus.getTime(), eventBus.getTimeMillis()));
	}

	/**
	 * Sends the test run finished to Cucumber runtime eventBus.
	 */
	private void finish() {
		eventBus.send(new TestRunFinished(eventBus.getTime(), eventBus.getTimeMillis()));
	}

	/**
	 * Builds local runtime instead of using cucumber runtime class.
	 */
	private void BuildRuntime() {
		ClassLoader classLoader = this.getClass().getClassLoader();
		ResourceLoader resourceLoader = new MultiLoader(classLoader);
		ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
		BackendSupplier backendSupplier = new BackendModuleBackendSupplier(resourceLoader, classFinder,
				this.runtimeOptions);
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
		this.plugins = new Plugins(classLoader, new PluginFactory(), runtimeOptions);
		this.plugins.setEventBusOnEventListenerPlugins(eventBus);
		this.plugins.setSerialEventBusOnEventListenerPlugins(eventBus);
		this.runnerSupplier = new ThreadLocalRunnerSupplier(this.runtimeOptions, eventBus, backendSupplier);
		FeatureLoader featureLoader = new FeatureLoader(resourceLoader);
		this.featureSupplier = new FeaturePathFeatureSupplier(featureLoader, this.runtimeOptions);
		this.filters = new Filters(this.runtimeOptions);
	}

	/**
	 * @return List of detected cucumber features
	 */
	public List<CucumberFeature> getFeatures() {

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