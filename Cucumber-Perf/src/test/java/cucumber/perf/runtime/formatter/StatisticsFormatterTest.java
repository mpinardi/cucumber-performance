package cucumber.perf.runtime.formatter;

import static org.junit.Assert.*;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cucumber.perf.api.event.EventHandler;
import cucumber.perf.api.event.SimulationFinished;
import cucumber.perf.api.event.StatisticsFinished;
import cucumber.perf.api.result.GroupResult;
import cucumber.perf.api.result.SimulationResult;
import cucumber.perf.api.result.statistics.Statistics;
import cucumber.perf.runtime.PerfRuntimeOptions;
import cucumber.perf.runtime.TimeServiceEventBus;
import cucumber.perf.runtime.formatter.PluginFactory;
import cucumber.perf.runtime.formatter.Plugins;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;

public class StatisticsFormatterTest {
	private TimeServiceEventBus eventBus = new TimeServiceEventBus(Clock.systemUTC());
	private long value = (long) 0;
	private String groupName = "test";
	private String type = "min";
	
	@Test
	public void testStatistics() {
		List<GroupResult> res = new ArrayList<GroupResult>();
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1000), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Status.PASSED,  Duration.ofMillis(1200), null), LocalDateTime.now(),LocalDateTime.now()));
		PluginFactory pf = new PluginFactory();
		PerfRuntimeOptions options = new PerfRuntimeOptions();
		Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
		plugins.setEventBusOnPlugins(eventBus);
		value = (long)1200000000;
		type = "max";
		eventBus.registerHandlerFor(StatisticsFinished.class, statsEventhandler);
		eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), new SimulationResult("test",new Result(Status.PASSED, Duration.ofMillis(0), null), LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), res)));
	}
	
	@Test
	public void testGetCnt() {
		List<GroupResult> res = new ArrayList<GroupResult>();
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1000), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1100), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1200), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Status.FAILED, Duration.ofMillis(100), null), LocalDateTime.now(),LocalDateTime.now()));
		PluginFactory pf = new PluginFactory();
		PerfRuntimeOptions options = new PerfRuntimeOptions();
		Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
		plugins.setEventBusOnPlugins(eventBus);
		value = (long)3;
		type = "cnt";
		eventBus.registerHandlerFor(StatisticsFinished.class, statsEventhandler);
		eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), new SimulationResult("test",new Result(Status.PASSED, Duration.ofMillis(0), null), LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), res)));
	}

	@Test
	public void testGetCntNonStrict() {
		List<GroupResult> res = new ArrayList<GroupResult>();
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1000), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1100), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1200), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Status.FAILED, Duration.ofMillis(100), null), LocalDateTime.now(),LocalDateTime.now()));
		PluginFactory pf = new PluginFactory();
		List<String> l = new ArrayList<String>();
		l.add("no-strict");
		PerfRuntimeOptions options = new PerfRuntimeOptions(l);
		Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
		plugins.setEventBusOnPlugins(eventBus);
		value = (long)4;
		type = "cnt";
		eventBus.registerHandlerFor(StatisticsFinished.class, statsEventhandler);
		eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), new SimulationResult("test",new Result(Status.PASSED, Duration.ofMillis(0), null), LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), res)));
	}

	@Test
	public void testGetAvg() {
		List<GroupResult> res = new ArrayList<GroupResult>();
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1000), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1100), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1200), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Status.FAILED, Duration.ofMillis(100), null), LocalDateTime.now(),LocalDateTime.now()));
		PluginFactory pf = new PluginFactory();
		PerfRuntimeOptions options = new PerfRuntimeOptions();
		Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
		plugins.setEventBusOnPlugins(eventBus);
		value = (long)1100000000;
		type = "avg";
		eventBus.registerHandlerFor(StatisticsFinished.class, statsEventhandler);
		eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), new SimulationResult("test",new Result(Status.PASSED, Duration.ofMillis(0), null), LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), res)));
	}
	
	@Test
	public void testGetAvgNonStrict() {
		List<GroupResult> res = new ArrayList<GroupResult>();
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1000), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1100), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1200), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Status.FAILED, Duration.ofMillis(100), null), LocalDateTime.now(),LocalDateTime.now()));
		PluginFactory pf = new PluginFactory();
		List<String> l = new ArrayList<String>();
		l.add("no-strict");
		PerfRuntimeOptions options = new PerfRuntimeOptions(l);
		Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
		plugins.setEventBusOnPlugins(eventBus);
		value = (long)850000000;
		type = "avg";
		eventBus.registerHandlerFor(StatisticsFinished.class, statsEventhandler);
		eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), new SimulationResult("test",new Result(Status.PASSED, Duration.ofMillis(0), null), LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), res)));
	}

	@Test
	public void testGetMin() {
		List<GroupResult> res = new ArrayList<GroupResult>();
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1000), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1200), null), LocalDateTime.now(),LocalDateTime.now()));
		PluginFactory pf = new PluginFactory();
		PerfRuntimeOptions options = new PerfRuntimeOptions();
		Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
		plugins.setEventBusOnPlugins(eventBus);
		value = (long)1000000000;
		type = "min";
		eventBus.registerHandlerFor(StatisticsFinished.class, statsEventhandler);
		eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), new SimulationResult("test",new Result(Status.PASSED, Duration.ofMillis(0), null),  LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), res)));
	}

	@Test
	public void testGetMax() {
		List<GroupResult> res = new ArrayList<GroupResult>();
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1000), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1200), null), LocalDateTime.now(),LocalDateTime.now()));
		PluginFactory pf = new PluginFactory();
		PerfRuntimeOptions options = new PerfRuntimeOptions();
		Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
		plugins.setEventBusOnPlugins(eventBus);
		value = (long)1200000000;
		type = "max";
		eventBus.registerHandlerFor(StatisticsFinished.class, statsEventhandler);
		eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), new SimulationResult("test",new Result(Status.PASSED, Duration.ofMillis(0), null),  LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), res)));
	}
	
	@Test
	public void testAddPercentile() {
		List<GroupResult> res = new ArrayList<GroupResult>();
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1000), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1200), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1400), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test1", new Result(Status.PASSED, Duration.ofMillis(1000), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test1", new Result(Status.PASSED, Duration.ofMillis(1200), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test1", new Result(Status.PASSED, Duration.ofMillis(1400), null), LocalDateTime.now(),LocalDateTime.now()));
		PluginFactory pf = new PluginFactory();
		List<String> args = new ArrayList<String>();
		args.add("pg=statistics:prcntl:50");
		PerfRuntimeOptions options = new PerfRuntimeOptions(args);
		Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
		plugins.setEventBusOnPlugins(eventBus);
		value = (long)1200000000;
		type = "prctl50";
		eventBus.registerHandlerFor(StatisticsFinished.class, statsEventhandler);
		eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), new SimulationResult("test",new Result(Status.PASSED, Duration.ofMillis(0), null),  LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), res)));
	}
	
	@Test
	public void testAddTwoPluginPercentile() {
		List<GroupResult> res = new ArrayList<GroupResult>();
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1000), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1200), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1300), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1400), null), LocalDateTime.now(),LocalDateTime.now()));
		PluginFactory pf = new PluginFactory();
		List<String> args = new ArrayList<String>();
		args.add("pg=statistics:prcntl:50,prcntl:75");
		PerfRuntimeOptions options = new PerfRuntimeOptions(args);
		Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
		plugins.setEventBusOnPlugins(eventBus);
		value = (long)1300000000;
		type = "prctl75";
		eventBus.registerHandlerFor(StatisticsFinished.class, statsEventhandler);
		eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), new SimulationResult("test",new Result(Status.PASSED, Duration.ofMillis(0), null),  LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), res)));
	}
	
	@Test
	public void testAddPluginClass() {
		List<GroupResult> res = new ArrayList<GroupResult>();
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1000), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1200), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1400), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test1", new Result(Status.PASSED, Duration.ofMillis(1000), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test1", new Result(Status.PASSED, Duration.ofMillis(1200), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test1", new Result(Status.PASSED, Duration.ofMillis(1400), null), LocalDateTime.now(),LocalDateTime.now()));
		PluginFactory pf = new PluginFactory();
		List<String> args = new ArrayList<String>();
		args.add("pg=statistics:cucumber.perf.runtime.formatter.PercentileCreator:50");
		PerfRuntimeOptions options = new PerfRuntimeOptions(args);
		Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
		plugins.setEventBusOnPlugins(eventBus);
		value = (long)1200000000;
		type = "prctl50";
		eventBus.registerHandlerFor(StatisticsFinished.class, statsEventhandler);
		eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), new SimulationResult("test",new Result(Status.PASSED, Duration.ZERO, null),  LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), res)));
	}
	
	private EventHandler<StatisticsFinished> statsEventhandler = new EventHandler<StatisticsFinished>() {
	        @Override
	        public void receive(StatisticsFinished event) {
	            verifyOutput(event);
	        }
	 };
	 
	 private void verifyOutput(StatisticsFinished event)
	 {
		 Statistics result = event.getResult();
		 switch (type)
		 {
		 	case "min":
		 		assertEquals((long)value,(long)result.getMin(groupName).longValue());
		 		break;
		 	case "max":
		 		assertEquals((long)value,(long)result.getMax(groupName).longValue());
		 		break;
		 	case "avg":
		 		assertEquals((long)value,(long)result.getAvg(groupName).longValue());
		 		break;
		 	case "prctl50":
		 		assertEquals((long)value,(long)result.getStats().getStatistic("prctl_50", groupName).longValue());
		 		assertEquals((long)value,(long)result.getStats().getStatistic("prctl_50", "test1").longValue());
		 		break;
		 	case "prctl75":
		 		assertEquals((long)value,(long)result.getStats().getStatistic("prctl_75", groupName).longValue());
		 		break;
		 }
	 }
}
