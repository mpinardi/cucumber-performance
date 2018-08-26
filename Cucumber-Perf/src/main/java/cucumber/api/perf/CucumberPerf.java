package cucumber.api.perf;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.regex.Pattern;

import cucumber.api.perf.formatter.Statistics;
import cucumber.api.perf.formatter.SummaryPrinter;
import cucumber.api.Plugin;
import cucumber.api.perf.formatter.DisplayPrinter;
import cucumber.api.perf.formatter.Formatter;
import cucumber.api.perf.result.FeatureResult;
import cucumber.api.perf.salad.ast.Group;
import cucumber.api.perf.salad.ast.SaladDocument;
import cucumber.api.perf.salad.ast.Simulation;
import cucumber.api.perf.salad.ast.SimulationDefinition;
import cucumber.api.perf.salad.ast.SimulationPeriod;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.model.CucumberFeature;
import gherkin.ast.Node;
import gherkin.ast.Tag;

/**
 * Cucumber Perf(Performance) AKA Cucumber Salad
 * A tool that interfaces with the standard cucumber application to allow for the running of performance tests.
 * Provides multithread execution of existing functional BDD test cases.
 * @author Matt Pinardi
 */
public class CucumberPerf {
	private int maxThreads = 10;
	private ExecutorService pool = null;

	private Class<?> clazz = null;
	private List<CucumberFeature> features = null;

	private PerfRuntimeOptions options = null;
	private List<Predicate> filters = null;
	private List<PerfPlan> plans = null;
	
	private long ranCount = 0;
	private long totalRanCount = 0;
	private int runningCount = 0;
	private List<PerfGroup> groups = null;
	private List<List<FutureTask<Object>>> running = new ArrayList<List<FutureTask<Object>>>();
	private List<FeatureResult> finished = new ArrayList<FeatureResult>();
	
	private LocalDateTime start = LocalDateTime.now();
	private LocalDateTime end = LocalDateTime.now();
	private Duration wait = null;
	
	private long maxRan = 0;
	private int maxRampPeriods = 10;

	/**
	 * Create a new CucumberPerf instance using a existing class for the cucumber options.
	 * All expected features and scenarios must be included in options.
	 * Other wise that group will be skipped.
	 * @param clazz An existing class with cucumber options (annotations) and cucumber perf options
	 */
	public CucumberPerf(Class<?> clazz) {
		this.clazz = clazz;
		this.options = new PerfRuntimeOptionsFactory(clazz).create();
		RuntimeOptions ro =  FeatureBuilder.createRuntime(clazz);
		this.features = FeatureBuilder.getFeatures(ro);
		this.warnProgressFormatter(ro);
		this.setNameFilters(options.getNameFilters());
		this.setTags(options.getTagFilters());
	}

	/**
	 * Create a new CucumberPerf instance using a existing class for the cucumber options.
	 * All expected features and scenarios must be included in options.
	 * Other wise that group will be skipped.
	 * @param clazz An existing class with cucumber options (annotations) and option cucumber perf options
	 * @param options The performance runtime options.
	 */
	public CucumberPerf(Class<?> clazz, PerfRuntimeOptions options) {
		this.clazz = clazz;
		this.options = options;
		RuntimeOptions ro =  FeatureBuilder.createRuntime(clazz);
		this.warnProgressFormatter(ro);
		this.features = FeatureBuilder.getFeatures(ro);
		this.setNameFilters(options.getNameFilters());
		this.setTags(options.getTagFilters());
	}

	/**
	 * Create a new CucumberPerf instance using passed in perf runtime options.
	 * @param options Cucumber perf runtime options.
	 */
	public CucumberPerf(PerfRuntimeOptions options) {
		this.options = options;
		RuntimeOptions ro =  FeatureBuilder.createRuntime(options.getCucumberOptions());
		this.features = FeatureBuilder.getFeatures(ro);
		this.warnProgressFormatter(ro);
		this.setNameFilters(options.getNameFilters());
		this.setTags(options.getTagFilters());
	}
	
	/**
	 * Create a new CucumberPerf instance using CLI arguments for the cucumber options.
	 * All expected features and scenarios must be included in options.
	 * Other wise that group will be skipped.
	 * @param args Combined cucumber and cucumber performance CLI arguments.
	 */
	public CucumberPerf(String[] args) {
		options = new PerfRuntimeOptions(Arrays.asList(args));
		RuntimeOptions ro = FeatureBuilder.createRuntime(options.getCucumberOptions());
		this.features = FeatureBuilder.getFeatures(ro);
		this.warnProgressFormatter(ro);
		this.setNameFilters(options.getNameFilters());
		this.setTags(options.getTagFilters());
	}

	/**
	 * Starts the execution of the performance test scenario.
	 * @throws Throwable Any number of errors can be returned.
	 */
	public void runThreads() throws Throwable {

		plans = PlanBuilder.LoadPlans(this.getClass(), new ArrayList<String>(options.getPlanPaths()));
		if (plans != null && !plans.isEmpty()) {
			for (int p = 0; p < plans.size(); p++) {
				SaladDocument pl = plans.get(p).getSaladPlan();
				// List<Tag> ptags = pl.getPlan().getTags();
				for (int s = 0; s < pl.getPlan().getChildren().size(); s++) {

					if (matchFilters(pl, s)) {

						// setup
						boolean executing = true;
						SimulationDefinition sim = pl.getPlan().getChildren().get(s);
						buildGroups(sim);
						setMaxRan();
						setMaxThreads();
						ranCount = 0;
						// max threads is set when building groups
						pool = Executors.newFixedThreadPool(maxThreads);

						// simulation timing
						String scheduledRuntime = null;
						String rampUp = null;
						String rampDown = null;

						if (sim instanceof SimulationPeriod) {
							scheduledRuntime = ((SimulationPeriod) sim).getTime().getText();
							rampUp = ((SimulationPeriod) sim).getRampUp() != null
									? ((SimulationPeriod) sim).getRampUp().getText()
									: null;
							rampDown = ((SimulationPeriod) sim).getRampDown() != null
									? ((SimulationPeriod) sim).getRampDown().getText()
									: null;
						} else {

							rampUp = ((Simulation) sim).getRampUp() != null ? ((Simulation) sim).getRampUp().getText()
									: null;
							rampDown = ((Simulation) sim).getRampDown() != null
									? ((Simulation) sim).getRampDown().getText()
									: null;
						}

						// timing
						LocalDateTime beginEnd = null;
						LocalDateTime endRamp = null;
						LocalDateTime nextRamp = null;
						long rampPeriod = 0;
						int curPercent = 0;
						LocalDateTime curTime = LocalDateTime.now();
						if (scheduledRuntime != null) {
							beginEnd = this.getEnd(curTime, scheduledRuntime);
						}
						if (rampUp != null) {
							endRamp = this.getEnd(curTime, rampUp);
							rampPeriod = getRampPeriod(Duration.between(curTime, endRamp), maxRampPeriods);
							nextRamp = this.getEnd(curTime, rampPeriod);
							setCurGroupThreads(0);
						}
						while (executing) {
							curTime = LocalDateTime.now();
			
							if (endRamp == null) {
								// check if time is up
								if ((beginEnd != null && curTime.isAfter(beginEnd))
										|| (ranCount >= maxRan && beginEnd == null)) {
									if (rampDown == null) {
										executing = false;
									} else {
										endRamp = this.getEnd(curTime, rampDown);
										rampPeriod = getRampPeriod(Duration.between(curTime, endRamp), maxRampPeriods);
										nextRamp = this.getEnd(curTime, rampPeriod);
									}
								}
							} else {
								if (curTime.isAfter(endRamp)) {
									endRamp = null;
									nextRamp = null;
									if (rampUp == null) {
										rampDown = null;
										executing = false;
									} else {
										rampUp = null;
										curPercent = 100;
										setCurGroupThreads(curPercent);
									}
								} else if (nextRamp != null && curTime.isAfter(nextRamp)) {
									if (rampUp == null)
									{
										curPercent = curPercent - (100 / maxRampPeriods);
									}
									else
									{
										curPercent = curPercent + (100 / maxRampPeriods);
									}
									setCurGroupThreads(curPercent);
									nextRamp = this.getEnd(curTime, rampPeriod);
								}
							}
							if (runningCount < maxThreads) {
								for (int i = runningCount > 0 ? runningCount - 1 : 0; i < maxThreads; i++) {
									PerfCucumberRunner runner = null;
									for (int l = 0; (runner == null  && l < groups.size()); l++) {
										PerfGroup pg = groups.get((i + l) % groups.size());
										if (pg.getRunning() < pg.getThreads() && scheduledRuntime != null ||pg.getRan() < pg.getCount()) {
											if (options.getCucumberOptions() != null && options.getCucumberOptions().size() > 0) {
												try {
													runner = new PerfCucumberRunner(this.getFeature(pg.getText()),options.getCucumberOptions(),
															pg.getSlice(),this.wait);
												} catch (Exception e) {
													throw e;
												}
											} else {
												try {
													runner = new PerfCucumberRunner(this.getFeature(pg.getText()),clazz, pg.getSlice(),this.wait);
												} catch (Exception e) {
													throw e;
												}
											}
											pg.incrementRunning();
										}
										
									}
									if (runner!=null)
									{
									FutureTask<Object> task = new FutureTask<Object>(runner);
									pool.execute(task);
									running.get(i % groups.size()).add(task);
									ranCount++;
									runningCount++;
									}
								}
							}
							printDisplay();
							int gc = 0;
							for (List<FutureTask<Object>> g : running) {
								for (int r = 0; r < g.size(); r++) {
									if (g.get(r).isDone()) {
										finished.add((FeatureResult) g.get(r).get());
										g.remove(r);
										runningCount--;
										groups.get(gc).decrementRunning();
									}
								}
								gc++;
							}
						}

						// wait for all threads to stop;
						//
						this.totalRanCount += ranCount;
						waitForFinished(60);
						report();
					}
				}
			}
		}
		end = LocalDateTime.now();

	}

	private boolean matchFilters(SaladDocument pl, int s) {
		SimulationDefinition si = null;
		if (pl.getPlan().getChildren().get(s) instanceof Simulation) {
			Simulation sim = (Simulation) pl.getPlan().getChildren().get(s);
			List<Tag> t = new ArrayList<Tag>(pl.getPlan().getTags());
			t.addAll(sim.getTags());
			si = new Simulation(t, sim.getLocation(), sim.getKeyword(), sim.getName(), sim.getDescription(),
					sim.getGroups(), sim.getRampDown(), sim.getRampDown(),sim.getSynchronize(),sim.getRandomWait());
		} else if (pl.getPlan().getChildren().get(s) instanceof SimulationPeriod) {
			SimulationPeriod sim = (SimulationPeriod) pl.getPlan().getChildren().get(s);
			List<Tag> t = new ArrayList<Tag>(pl.getPlan().getTags());
			t.addAll(sim.getTags());
			si = new SimulationPeriod(t, sim.getLocation(), sim.getKeyword(), sim.getName(), sim.getDescription(),
					sim.getGroups(), sim.getTime(), sim.getRampDown(), sim.getRampDown(),sim.getSynchronize(),sim.getRandomWait());
		}

		return matchesFilters(si, filters);
	}

	private static boolean matchesFilters(Node n, List<Predicate> filters) {
		for (Predicate filter : filters) {
			if (!filter.apply(n)) {
				return false;
			}
		}
		return true;
	}

	private void buildGroups(SimulationDefinition sim) {
		if (sim instanceof Simulation) {
			this.wait = ((Simulation) sim).getRandomWait()!= null ? convertRuntime(((Simulation) sim).getRandomWait().getText()) : null;
		} else if (sim instanceof SimulationPeriod) {
			this.wait = ((SimulationPeriod) sim).getRandomWait() != null ? convertRuntime(((SimulationPeriod) sim).getRandomWait().getText()) : null;
		}
		
		this.groups = new ArrayList<PerfGroup>();
		this.running = new ArrayList<List<FutureTask<Object>>>();
		for (Group g : sim.getGroups()) {
			groups.add(new PerfGroup(g));
			running.add(new ArrayList<FutureTask<Object>>());
		}
	}

	private void waitForFinished(int timeout) {
		int count = 0;
		while (count < timeout) {
			if (isFinished()) {
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {

			}
			count++;
		}
	}

	private boolean isFinished() {
		int gc = 0;
		if (running.isEmpty())
			return true;

		for (List<FutureTask<Object>> g : running) {
			for (int r = 0; r < g.size(); r++) {
				if (g.get(r).isDone()) {
					try {
						finished.add((FeatureResult) g.get(r).get());
					} catch (InterruptedException e) {

					} catch (ExecutionException e) {

					}
					g.remove(r);
					runningCount--;
					groups.get(gc).decrementRunning();
				}
			}
			if (!g.isEmpty())
				return false;
			gc++;
		}
		return true;
	}

	public void report() throws IOException {
		Statistics stats = new Statistics(finished, true);
		printSummary(stats);
		createReports(stats);		
	}

    private void printSummary(Statistics stats) {
    	ClassLoader classLoader = this.getClass().getClassLoader();
        SummaryPrinter summaryPrinter = options.summaryPrinter(classLoader);
        summaryPrinter.print(stats);
    }
    
	private void printDisplay() {
		ClassLoader classLoader = this.getClass().getClassLoader();
        DisplayPrinter printer = options.displayPrinter(classLoader);
        printer.print(groups);
		
	}
    private void createReports(Statistics stats) {
    	ClassLoader classLoader = this.getClass().getClassLoader();
        List<Formatter> formatters = options.formatters(classLoader);
        for (Formatter f : formatters)
        {
        	f.process(stats);
        }
    }
    
	/*
	 * public void runScenario(PickleEvent pickleEvent, CucumberFeature feature)
	 * throws Throwable { perfCucumberRunner.runScenario(pickleEvent); }
	 * 
	 * 
	 * public Object[][] getScenarios() { if (perfCucumberRunner == null) { return
	 * new Object[0][0]; } return perfCucumberRunner.provideScenarios(); }
	 * 
	 * public void tearDownClass() throws Exception { if (perfCucumberRunner ==
	 * null) { return; } perfCucumberRunner.finish(); }
	 */
	public Duration convertRuntime(String time) {
		Duration d = Duration.between(LocalTime.MIN, LocalTime.parse(time));
		return d;
	}

	public LocalDateTime getEnd(LocalDateTime start, String time) {
		LocalDateTime endt = LocalDateTime.from(start).plus(convertRuntime(time));
		return endt;
	}

	private LocalDateTime getEnd(LocalDateTime start, long timeSeconds) {
		LocalDateTime endt = LocalDateTime.from(start).plus(timeSeconds, ChronoUnit.SECONDS);
		return endt;
	}

	public long getMaxRan() {
		return maxRan;
	}

	private void setMaxRan() {
		this.maxRan = 0;
		for (PerfGroup pg : groups) {
			this.maxRan += pg.getCount();
		}
	}

	
	private void setMaxThreads() {
		this.maxThreads = 0;
		for (PerfGroup pg : groups) {
			this.maxThreads += pg.getThreads();
		}
	}

	private void setCurGroupThreads(int percent) {
		float per = 1;
		if (percent < 100) {
			if (percent > 0) {
				per = ((float) percent / 100);
			} else {
				per = 0;
			}
		}

		this.maxThreads = 0;
		for (PerfGroup pg : groups) {
			pg.setThreads(Math.round(pg.getMaxThreads() * per));
			this.maxThreads = this.maxThreads + pg.getThreads();
		}
	}

	private long getRampPeriod(Duration time, int times) {
		return time.getSeconds() / times;
	}
	
	private CucumberFeature getFeature(String feature) {
		for (CucumberFeature f : features)
		{
			if (f.getGherkinFeature().getFeature().getName().equalsIgnoreCase(feature)|| f.getUri().substring(f.getUri().lastIndexOf("/")+1).equalsIgnoreCase(feature))
			{
				return f;
			}
		}
		return null;
	}
	
	private void setTags(List<String> tags) {
		if (filters == null) {
			filters = new ArrayList<Predicate>();
		}
		if (tags != null && !tags.isEmpty())
		{
		this.filters.add(new TagPredicate(tags));
		}
	}

	private void setNameFilters(List<Pattern> names) {
		if (filters == null) {
			filters = new ArrayList<Predicate>();
		}
		if (names != null && !names.isEmpty())
		{
			this.filters.add(new NamePredicate(names));
		}
		
	}
	
	private void warnProgressFormatter(RuntimeOptions runtimeOptions)
	{
		for(Plugin plugin: runtimeOptions.getPlugins())
		{
			if (plugin.getClass().getSimpleName().equalsIgnoreCase("ProgressFormatter"))
			{
				options.disableDisplay();
				System.err.println("WARNING: Cucumber options contains Progress formatter.");
				System.err.println(	"	This is enabled by default in Cucumber when no formatter is passed in.");
				System.err.println(	"	Disabling all display printers. To enable pass in NullFormatter");
			}
		}
	}
	
	/**
	 * @return Total number of threads ran so far.
	 */
	public long getTotalRanCount() {
		return totalRanCount;
	}

	/**
	 * @return Start of Cucumber Perf execution
	 */
	public LocalDateTime getStart() {
		return start;
	}
	/**
	 * @return End of Cucumber Perf execution
	 */
	public LocalDateTime getEnd() {
		return end;
	}

	/**
	 * @return Duration of of Cucumber Perf execution.
	 */
	public String getRunTime() {
		Duration d = Duration.between(start, end);
		return d.toString();
	}
	
	public int getMaxThreads() {
		return maxThreads;
	}

}
