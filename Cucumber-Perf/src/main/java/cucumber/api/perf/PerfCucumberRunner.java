package cucumber.api.perf;

import cucumber.api.Result;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestRunStarted;
import cucumber.api.perf.result.FeatureResult;
import cucumber.api.perf.result.FeatureResultListener;
import cucumber.api.perf.result.ScenarioResult;
import cucumber.api.perf.result.StepResultListener;
import cucumber.api.perf.result.TestCaseResultListener;
import cucumber.api.perf.salad.ast.Slice;
import cucumber.runner.TimeServiceEventBus;
import cucumber.runner.TimeService;
import cucumber.runner.EventBus;
import cucumber.runner.RunnerSupplier;
import cucumber.runner.ThreadLocalRunnerSupplier;
import cucumber.runtime.BackendModuleBackendSupplier;
import cucumber.runtime.BackendSupplier;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.FeaturePathFeatureSupplier;
import cucumber.runtime.FeatureSupplier;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.filter.Filters;
import cucumber.runtime.filter.RerunFilters;
import cucumber.runtime.formatter.Plugins;
import cucumber.runtime.formatter.PluginFactory;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.FeatureLoader;
import gherkin.ast.ScenarioDefinition;
import gherkin.events.PickleEvent;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The Cucumber Performance thread runner.
 * 
 * @author Matt Pinardi
 *
 */
public class PerfCucumberRunner implements Callable<Object> {
	private RuntimeOptions runtimeOptions;
    private RunnerSupplier runnerSupplier;
    private EventBus eventBus = new TimeServiceEventBus(TimeService.SYSTEM);
    private Filters filters;
    private FeatureSupplier featureSupplier;
    @SuppressWarnings("unused")
	private Plugins plugins;

	private FeatureResultListener resultListener;
	private TestCaseResultListener testCaseResultListener;
	private StepResultListener stepResultListener;
	private Duration wait = null;
	private List<CucumberFeature> features = null;
	List<ScenarioResult> scenarioResults = new ArrayList<ScenarioResult>();
	private Slice slice;

	/**
	 * Cause thread to random wait between 50%+- of the mean;
	 */
	private void randomWait() {
		if (wait != null)
		{
			try {
				long mean = wait.toMillis();
				Thread.sleep(ThreadLocalRandom.current().nextLong((mean/2),(mean+(mean/2))));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
			}
		}
		
	}

	public PerfCucumberRunner(CucumberFeature feature, List<String> args, Slice slice, Duration wait) {
		this.slice = slice;
		this.wait = wait;
		this.features = new ArrayList<CucumberFeature>(Arrays.asList(feature));
		// prepare runtime options
		runtimeOptions = new RuntimeOptions(args);

		// prepare runtime
		this.BuildRuntime();
		
		feature.sendTestSourceRead(eventBus);
		
		//Enable plugins
        //StepDefinitionReporter stepDefinitionReporter = plugins.stepDefinitionReporter();
        //runnerSupplier.get().reportStepDefinitions(stepDefinitionReporter);

		resultListener = new FeatureResultListener(feature.getUri().substring(feature.getUri().lastIndexOf("/") + 1));
		resultListener.setEventPublisher(eventBus);

		testCaseResultListener = new TestCaseResultListener();
		testCaseResultListener.setEventPublisher(eventBus);

		stepResultListener = new StepResultListener();
		stepResultListener.setEventPublisher(eventBus);
	}

	public PerfCucumberRunner(CucumberFeature feature, Class<?> clazz, Slice slice, Duration wait) {
		this.features = new ArrayList<CucumberFeature>(Arrays.asList(feature));
		this.slice = slice;
		this.wait = wait;
		// prepare runtime options
		RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz);
		runtimeOptions = runtimeOptionsFactory.create();

		// prepare runtime
		this.BuildRuntime();

		feature.sendTestSourceRead(eventBus);

		//Pre Cucumber 4.0.0
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
		
		//Post Cucumber 4.0.0
		//Enable plugins
        //StepDefinitionReporter stepDefinitionReporter = plugins.stepDefinitionReporter();
        //runnerSupplier.get().reportStepDefinitions(stepDefinitionReporter);
        
		resultListener = new FeatureResultListener(feature.getUri().substring(feature.getUri().lastIndexOf("/") + 1));
		resultListener.setEventPublisher(eventBus);

		testCaseResultListener = new TestCaseResultListener();
		testCaseResultListener.setEventPublisher(eventBus);

		stepResultListener = new StepResultListener();
		stepResultListener.setEventPublisher(eventBus);
	}

	@Override
	public Object call() {
		List<CucumberFeature> features = this.getFeatures();
		FeatureResult result = null;
		for (CucumberFeature f : features) {
			randomWait();
			start();
			try {
				runCucumber(f, slice);
				finish();
				result = resultListener.getResult();
				result.setChildResults(scenarioResults);
			} catch (Throwable e) {
				//Name in feature: f.getGherkinFeature().getFeature().getName()
				//Name of feature file: f.getUri().substring(f.getUri().lastIndexOf("/")+1);	
				result = (resultListener.getResult() != null) ? resultListener.getResult(): new FeatureResult(f.getUri().substring(f.getUri().lastIndexOf("/")+1), new Result(Result.Type.FAILED, (long)0, e),LocalDateTime.now(), LocalDateTime.now()) ;
				result.setChildResults(scenarioResults);
			}
		}
		return result;
	}

	private void runCucumber(CucumberFeature cucumberFeature, Slice slice) throws Throwable {
		PerfCompiler compiler = new PerfCompiler();
		List<PickleEvent> pickles = compiler.compileFeature(cucumberFeature, slice);
		resultListener.startFeature();
		for (PickleEvent pickle : pickles) {
			if (filters.matchesFilters(pickle)) {
				runScenario(pickle);
			}
		}
	}

/*	private void runCucumberDirect(CucumberFeature cucumberFeature) throws Throwable {
		resultListener.startFeature();
		runtime.runFeature(cucumberFeature);

		if (!resultListener.getResult().isPassed()) {
			throw new CucumberException(resultListener.getResult().getError());
		}
	}*/

	private void runScenario(PickleEvent pickle) throws Throwable {
		//runtime.getRunner().runPickle(pickle);
		runnerSupplier.get().runPickle(pickle);
		
		ScenarioResult sr = testCaseResultListener.getResult();
		sr.setChildResults(stepResultListener.getResults());
		scenarioResults.add(sr);
		stepResultListener.reset();
		
		if (!testCaseResultListener.getResult().isPassed()) {
			throw testCaseResultListener.getResult().getError();
		}
	}

	/**
	 * Sends the test run start to Cucumber runtime eventBus.
	 */
	private void start() {
		eventBus.send(new TestRunStarted(eventBus.getTime()));
	}

	/**
	 * Sends the test run finished to Cucumber runtime eventBus.
	 */
	private void finish() {
		eventBus.send(new TestRunFinished(eventBus.getTime()));
	}
	
	/**
	 * Builds local runtime instead of using cucumber runtime class.
	 */
	private void BuildRuntime() {
		ClassLoader classLoader = this.getClass().getClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ClassFinder classFinder =  new ResourceLoaderClassFinder(resourceLoader,classLoader);
        BackendSupplier backendSupplier =new BackendModuleBackendSupplier(resourceLoader, classFinder, this.runtimeOptions);
        //can cull out pretty here 
        // Plugins orgPlugins = new Plugins(this.classLoader, new PluginFactory(), this.eventBus, this.runtimeOptions);
        // this.plugins = new Plugins(classLoader, new PluginFactory(), eventBus, new RuntimeOptions(new ArrayList<String>()));
        //for (final Plugin plugin : orgPlugins) {
        //	if(!(plugin instanceof PrettyFormatter))
        //	{
        //  	plugins.addPlugin(plugin);
		//	}
        //}
        this.plugins = new Plugins(classLoader, new PluginFactory(), eventBus, runtimeOptions);
        this.runnerSupplier = new ThreadLocalRunnerSupplier(this.runtimeOptions, eventBus, backendSupplier);
        FeatureLoader featureLoader = new FeatureLoader(resourceLoader);
        this.featureSupplier =  new FeaturePathFeatureSupplier(featureLoader, this.runtimeOptions);
        RerunFilters rerunFilters = new RerunFilters(this.runtimeOptions, featureLoader);
        this.filters = new Filters(this.runtimeOptions, rerunFilters);
	}


	/**
	 * @return List of detected cucumber features
	 */
	public List<CucumberFeature> getFeatures() {

		if (features == null) {
			return featureSupplier.get();
		}
		return features;
	}

	/**
	 * @return List of detected cucumber scenarios
	 */
	public List<List<ScenarioDefinition>> getScenarios() {
		return FeatureBuilder.GetScenarios(this.getFeatures());
	}

}