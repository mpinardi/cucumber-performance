package cucumber.perf.runtime;

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
import java.util.concurrent.ThreadLocalRandom;

import cucumber.api.Result;
import cucumber.perf.api.FeatureBuilder;
import cucumber.perf.api.PerfGroup;
import cucumber.perf.api.PerfPlan;
import cucumber.perf.api.PlanBuilder;
import cucumber.perf.api.event.GroupFinished;
import cucumber.perf.api.event.GroupStarted;
import cucumber.perf.api.event.PerfRunFinished;
import cucumber.perf.api.event.PerfRunStarted;
import cucumber.perf.api.event.SimulationFinished;
import cucumber.perf.api.event.SimulationStarted;
import cucumber.perf.api.result.GroupResult;
import cucumber.perf.api.result.SimulationResult;
import cucumber.perf.runtime.filter.FeatureFilter;
import cucumber.perf.runtime.filter.Filters;
import cucumber.perf.runtime.formatter.PluginFactory;
import cucumber.perf.runtime.formatter.Plugins;
import cucumber.perf.salad.ast.Group;
import cucumber.perf.salad.ast.SaladDocument;
import cucumber.perf.salad.ast.Simulation;
import cucumber.perf.salad.ast.SimulationDefinition;
import cucumber.perf.salad.ast.SimulationPeriod;
import cucumber.runner.TimeService;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.model.CucumberFeature;
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
	private Filters filters;
	private Class<?> clazz = null;
	private List<CucumberFeature> features = null;
	private FeatureFilter featureFilter = null;
	private PerfRuntimeOptions options = null;
	private Plugins plugins = null;
	private List<PerfPlan> plans = null;
	
	private long ranCount = 0;
	private long totalRanCount = 0;
	private int runningCount = 0;
	private List<PerfGroup> groups = null;
	private List<List<FutureTask<Object>>> running = new ArrayList<List<FutureTask<Object>>>();
	private List<GroupResult> finished = new ArrayList<GroupResult>();
	
	private LocalDateTime start = LocalDateTime.now();
	private LocalDateTime end = LocalDateTime.now();
	private Duration wait = null;
	
	private long maxRan = 0;
	private int maxRampPeriods = 10;
	private TimeServiceEventBus eventBus = new TimeServiceEventBus(TimeService.SYSTEM);
	/**
	 * Create a new CucumberPerf instance using a existing class for the cucumber options.
	 * All expected features and scenarios must be included in options.
	 * Other wise that group will be skipped.
	 * @param clazz An existing class with cucumber options (annotations) and cucumber perf options
	 */
	public CucumberPerf(Class<?> clazz) {
		this.clazz = clazz;
		this.options = new PerfRuntimeOptionsFactory(clazz).create();
		RuntimeOptions ro =  FeatureBuilder.createRuntimeOptions(clazz);
		this.buildRuntime(ro);
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
		RuntimeOptions ro =  FeatureBuilder.createRuntimeOptions(clazz);
		this.buildRuntime(ro);
	}

	/**
	 * Create a new CucumberPerf instance using passed in perf runtime options.
	 * @param options Cucumber perf runtime options.
	 */
	public CucumberPerf(PerfRuntimeOptions options) {
		this.options = options;
		RuntimeOptions ro =  FeatureBuilder.createRuntimeOptions(options.getCucumberOptions());
		this.buildRuntime(ro);
	}
	
	/**
	 * Create a new CucumberPerf instance using CLI arguments for the cucumber options.
	 * All expected features and scenarios must be included in options.
	 * Other wise that group will be skipped.
	 * @param args Combined cucumber and cucumber performance CLI arguments.
	 */
	public CucumberPerf(String[] args) {
		options = new PerfRuntimeOptions(Arrays.asList(args));
		RuntimeOptions ro = FeatureBuilder.createRuntimeOptions(options.getCucumberOptions());
		this.buildRuntime(ro);
	}

	/**
	 * Starts the execution of the performance test scenario.
	 * @throws Throwable Any number of errors can be returned.
	 */
	public void runThreads() throws Throwable {
		eventBus.send(new PerfRunStarted(eventBus.getTime(),eventBus.getTimeMillis()));
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
						eventBus.send(new SimulationStarted(eventBus.getTime(),eventBus.getTimeMillis(),sim.getName()));
						LocalDateTime start = LocalDateTime.now();
						
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
						while (!options.isDryRun()&&executing) {
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
									CucumberRunner runner = null;
									//Seed for random start location to avoid clumping
									int loc = ThreadLocalRandom.current().nextInt(0, 99999999);
									for (int l = 0; (runner == null  && l < groups.size()); l++) {
										PerfGroup pg = groups.get((loc + l) % groups.size());
										if (pg.getRunning() < pg.getThreads() && (scheduledRuntime != null ||pg.getRan() < pg.getCount())) {
											RunnerOptions ro = new RunnerOptions(pg);
											if (options.getCucumberOptions() != null && options.getCucumberOptions().size() > 0) {
												try {
													runner = new CucumberRunner(ro,options.getCucumberOptions(),this.wait,options.isFailFast());
												} catch (Exception e) {
													throw e;
												}
											} else {
												try {
													runner = new CucumberRunner(ro,clazz,this.wait,options.isFailFast());
												} catch (Exception e) {
													throw e;
												}
											}
											loc = ((loc + l) % groups.size());
											pg.incrementRunning();
											eventBus.send(new GroupStarted(eventBus.getTime(), eventBus.getTimeMillis(),p,pg));
										}
										
									}
									if (runner!=null)
									{
										FutureTask<Object> task = new FutureTask<Object>(runner);
										pool.execute(task);
										running.get(loc).add(task);
										ranCount++;
										runningCount++;
									}
								}
							}
							int gc = 0;
							for (List<FutureTask<Object>> g : running) {
								for (int r = 0; r < g.size(); r++) {
									if (g.get(r).isDone()) {
										finished.add((GroupResult) g.get(r).get());
										eventBus.send(new GroupFinished(eventBus.getTime(), eventBus.getTimeMillis(),gc,groups.get(gc), (GroupResult) g.get(r).get()));
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
						eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(),new SimulationResult(sim.getName(), new Result(Result.Type.PASSED,  Duration.between(start, LocalDateTime.now()).getSeconds(), null), start,LocalDateTime.now(), this.finished)));
					}
				}
			}
		}
		end = LocalDateTime.now();
		eventBus.send(new PerfRunFinished(eventBus.getTime(),eventBus.getTimeMillis()));
	}
	
	private void buildRuntime(RuntimeOptions ro)
	{
		this.features = FeatureBuilder.getFeatures(ro);
		this.featureFilter = new FeatureFilter(this.features);
		this.warnProgressFormatter(ro);
		this.filters = new Filters(this.options);
		PluginFactory pf = new PluginFactory();
		this.plugins = new Plugins(this.getClass().getClassLoader(), pf, this.options);
		this.plugins.setEventBusOnPlugins(this.eventBus);
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

		return filters.matchesFilters(si);
	}

	private void buildGroups(SimulationDefinition sim) {
		if (sim instanceof Simulation) {
			this.wait = ((Simulation) sim).getRandomWait()!= null ? convertRuntime(((Simulation) sim).getRandomWait().getText()) : null;
		} else if (sim instanceof SimulationPeriod) {
			this.wait = ((SimulationPeriod) sim).getRandomWait() != null ? convertRuntime(((SimulationPeriod) sim).getRandomWait().getText()) : null;
		}
		if (wait !=null && (wait.isZero() || wait.isNegative()))
		{
			wait = null;
			System.err.println("WARNING: Random wait is not a valid duration. It will be ignored."); 
		}
		this.groups = new ArrayList<PerfGroup>();
		this.running = new ArrayList<List<FutureTask<Object>>>();
		for (Group g : sim.getGroups()) {
			PerfGroup pg = new PerfGroup(g);
			pg.setFeatures(this.getFeatures(pg));
			groups.add(pg);
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
						finished.add((GroupResult) g.get(r).get());
					} catch (InterruptedException e) {

					} catch (ExecutionException e) {

					}
					g.remove(r);
					runningCount--;
					groups.get(gc).decrementRunning();
					eventBus.send(new GroupFinished(eventBus.getTime(), eventBus.getTimeMillis(),gc,groups.get(gc), finished.get(finished.size()-1)));
				}
			}
			if (!g.isEmpty())
				return false;
			gc++;
		}
		return true;
	}
	
	public void printResult() {
		System.out.println("Total Ran: "+this.getTotalRanCount());
		System.out.println("RunTime: "+this.getRunTime());		
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
	
	private List<CucumberFeature> getFeatures(PerfGroup pg){
		return this.featureFilter.filter(pg.getText());
	}
	
	private void warnProgressFormatter(RuntimeOptions runtimeOptions)
	{
		for(String plugin: runtimeOptions.getPluginNames())
		{
			if (plugin.equalsIgnoreCase("progress"))
			{
				options.disableDisplay();
				System.out.println("WARNING: Cucumber options contains Progress formatter.");
				System.out.println(	"	This is enabled by default in Cucumber when no formatter is passed in.");
				System.out.println(	"	Disabling all display printers. To enable pass in plugin \"cucumber.formatter.NullFormatter\"");
			} else if (plugin.equalsIgnoreCase("default_summary"))
			{
				options.disableDisplay();
				System.out.println("WARNING: Cucumber options contains default summary.");
				System.out.println(	"	This is enabled by default in Cucumber when no formatter is passed in.");
				System.out.println(	"	Disabling all display printers. To enable pass in plugin \"null_summary\"");
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

	
	public Plugins getPlugins() {
		return plugins;
	}

}
