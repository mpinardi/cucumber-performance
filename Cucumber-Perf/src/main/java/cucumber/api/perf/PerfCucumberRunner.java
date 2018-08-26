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
import cucumber.runtime.ClassFinder;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.model.CucumberFeature;
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
	private Runtime runtime;
	private RuntimeOptions runtimeOptions;
	private ResourceLoader resourceLoader;
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
		ClassLoader classLoader = this.getClass().getClassLoader();
		resourceLoader = new MultiLoader(classLoader);
		runtimeOptions = new RuntimeOptions(args);

		// prepare runtime
		ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
		runtime = new Runtime(resourceLoader, classFinder, classLoader, runtimeOptions);
		runtimeOptions.getPlugins();
		feature.sendTestSourceRead(runtime.getEventBus());

		resultListener = new FeatureResultListener(feature.getUri().substring(feature.getUri().lastIndexOf("/") + 1));
		resultListener.setEventPublisher(runtime.getEventBus());

		testCaseResultListener = new TestCaseResultListener();
		testCaseResultListener.setEventPublisher(runtime.getEventBus());

		stepResultListener = new StepResultListener();
		stepResultListener.setEventPublisher(runtime.getEventBus());
	}

	public PerfCucumberRunner(CucumberFeature feature, Class<?> clazz, Slice slice, Duration wait) {
		this.features = new ArrayList<CucumberFeature>(Arrays.asList(feature));
		this.slice = slice;
		this.wait = wait;
		// prepare runtime options
		ClassLoader classLoader = clazz.getClassLoader();
		resourceLoader = new MultiLoader(classLoader);
		RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz);
		runtimeOptions = runtimeOptionsFactory.create();

		// prepare runtime
		ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
		runtime = new Runtime(resourceLoader, classFinder, classLoader, runtimeOptions);
		runtimeOptions.getPlugins();
		feature.sendTestSourceRead(runtime.getEventBus());

		// trying to remove pretty but doesn't seem possible
		// you can do getPlugins() before creating the runtime to disable all plugins
		// however
		/*
		 * for (int i = 0; i <runtimeOptions.getPlugins().size();i++) {
		 * if(runtimeOptions.getPlugins().get(i).getClass().isInstance(new
		 * PluginFactory().create("pretty"))) { runtimeOptions.getPlugins().remove(i);
		 * System.out.println(""+runtimeOptions.getPlugins().get(i).getClass()); } }
		 */
		// reporter.setEventPublisher(runtime.getEventBus());
		resultListener = new FeatureResultListener(feature.getUri().substring(feature.getUri().lastIndexOf("/") + 1));
		resultListener.setEventPublisher(runtime.getEventBus());

		testCaseResultListener = new TestCaseResultListener();
		testCaseResultListener.setEventPublisher(runtime.getEventBus());

		stepResultListener = new StepResultListener();
		stepResultListener.setEventPublisher(runtime.getEventBus());
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
			runScenario(pickle);
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
		runtime.getRunner().runPickle(pickle);

		ScenarioResult sr = testCaseResultListener.getResult();
		sr.setChildResults(stepResultListener.getResults());
		scenarioResults.add(sr);
		stepResultListener.reset();
		
		if (!testCaseResultListener.getResult().isPassed()) {
			throw testCaseResultListener.getResult().getError();
		}
	}

	/**
	 * Sends the test run start to Cucumber runtime bus.
	 */
	private void start() {
		runtime.getEventBus().send(new TestRunStarted(runtime.getEventBus().getTime()));
	}

	/**
	 * Sends the test run finished to Cucumber runtime bus.
	 */
	private void finish() {
		runtime.getEventBus().send(new TestRunFinished(runtime.getEventBus().getTime()));
	}

	/**
	 * @return List of detected cucumber features
	 */
	public List<CucumberFeature> getFeatures() {

		if (features == null) {
			return runtimeOptions.cucumberFeatures(resourceLoader, runtime.getEventBus());
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