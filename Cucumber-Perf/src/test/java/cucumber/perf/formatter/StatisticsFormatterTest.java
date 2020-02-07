package cucumber.perf.formatter;

import static org.junit.Assert.*;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cucumber.perf.api.event.ConfigStatistics;
import cucumber.perf.api.event.EventHandler;
import cucumber.perf.api.event.SimulationFinished;
import cucumber.perf.api.event.StatisticsFinished;
import cucumber.perf.api.formatter.Statistics;
import cucumber.perf.api.result.GroupResult;
import cucumber.perf.api.result.SimulationResult;
import cucumber.perf.runtime.PerfRuntimeOptions;
import cucumber.perf.runtime.TimeServiceEventBus;
import cucumber.perf.runtime.formatter.PluginFactory;
import cucumber.perf.runtime.formatter.Plugins;
import cucumber.perf.runtime.formatter.StatisticsFormatter;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;

public class StatisticsFormatterTest {
	private TimeServiceEventBus eventBus = new TimeServiceEventBus(Clock.systemDefaultZone());
	private long value = (long) 0;
	private String groupName = "test";
	private String type = "min";
	private int points = 0;
	
	@Test
	public void testStatistics() {
		List<GroupResult> res = new ArrayList<GroupResult>();
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1000), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1200), null), LocalDateTime.now(),LocalDateTime.now()));
		StatisticsFormatter s = new StatisticsFormatter();
		PluginFactory pf = new PluginFactory();
		PerfRuntimeOptions options = new PerfRuntimeOptions();
		Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
		plugins.addPlugin(s);
		plugins.setEventBusOnPlugins(eventBus);
		value = (long)1200;
		type = "max";
		eventBus.registerHandlerFor(StatisticsFinished.class, statsEventhandler);
		eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), new SimulationResult("test",new Result(Status.PASSED, Duration.ZERO, null), LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), res)));
	}
	
	@Test
	public void testGetCnt() {
		List<GroupResult> res = new ArrayList<GroupResult>();
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1000), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1100), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1200), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Status.FAILED, Duration.ofMillis(100), null), LocalDateTime.now(),LocalDateTime.now()));
		StatisticsFormatter s = new StatisticsFormatter();
		s.setEventBus(eventBus);
		PluginFactory pf = new PluginFactory();
		PerfRuntimeOptions options = new PerfRuntimeOptions();
		Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
		plugins.addPlugin(s);
		plugins.setEventBusOnPlugins(eventBus);
		value = (long)3;
		type = "cnt";
		eventBus.registerHandlerFor(StatisticsFinished.class, statsEventhandler);
		eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), new SimulationResult("test",new Result(Status.PASSED, Duration.ZERO, null), LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), res)));
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
		eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), new SimulationResult("test",new Result(Status.PASSED, Duration.ZERO, null), LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), res)));
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
		value = (long)1100;
		type = "avg";
		eventBus.registerHandlerFor(StatisticsFinished.class, statsEventhandler);
		eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), new SimulationResult("test",new Result(Status.PASSED, Duration.ZERO, null), LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), res)));
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
		value = (long)850;
		type = "avg";
		eventBus.registerHandlerFor(StatisticsFinished.class, statsEventhandler);
		eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), new SimulationResult("test",new Result(Status.PASSED, Duration.ZERO, null), LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), res)));
	}

	@Test
	public void testGetMin() {
		List<GroupResult> res = new ArrayList<GroupResult>();
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1000), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1200), null), LocalDateTime.now(),LocalDateTime.now()));
		StatisticsFormatter s = new StatisticsFormatter();
		s.setEventBus(eventBus);
		PluginFactory pf = new PluginFactory();
		PerfRuntimeOptions options = new PerfRuntimeOptions();
		Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
		plugins.addPlugin(s);
		plugins.setEventBusOnPlugins(eventBus);
		value = (long)1000;
		type = "min";
		eventBus.registerHandlerFor(StatisticsFinished.class, statsEventhandler);
		eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), new SimulationResult("test",new Result(Status.PASSED, Duration.ofMillis(0), null),  LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), res)));
	}

	@Test
	public void testGetMax() {
		List<GroupResult> res = new ArrayList<GroupResult>();
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1000), null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1200), null), LocalDateTime.now(),LocalDateTime.now()));
		StatisticsFormatter s = new StatisticsFormatter();
		s.setEventBus(eventBus);
		PluginFactory pf = new PluginFactory();
		PerfRuntimeOptions options = new PerfRuntimeOptions();
		Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
		plugins.addPlugin(s);
		plugins.setEventBusOnPlugins(eventBus);
		value = (long)1200;
		type = "max";
		eventBus.registerHandlerFor(StatisticsFinished.class, statsEventhandler);
		eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), new SimulationResult("test",new Result(Status.PASSED, Duration.ZERO, null),  LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), res)));
	}
	
	@Test
	public void testChartPoints() {
		List<GroupResult> res = new ArrayList<GroupResult>();
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(20000), null), LocalDateTime.parse("2007-12-12T05:20:35"),LocalDateTime.parse("2007-12-12T05:20:55")));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(30000), null), LocalDateTime.parse("2007-12-12T05:21:10"),LocalDateTime.parse("2007-12-12T05:21:40")));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(32000), null), LocalDateTime.parse("2007-12-12T05:22:01"),LocalDateTime.parse("2007-12-12T05:22:33")));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(25000), null), LocalDateTime.parse("2007-12-12T05:22:40"),LocalDateTime.parse("2007-12-12T05:23:05")));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(40000), null), LocalDateTime.parse("2007-12-12T05:23:10"),LocalDateTime.parse("2007-12-12T05:23:50")));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(30000), null), LocalDateTime.parse("2007-12-12T05:23:55"),LocalDateTime.parse("2007-12-12T05:24:25")));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(26000), null), LocalDateTime.parse("2007-12-12T05:24:30"),LocalDateTime.parse("2007-12-12T05:24:56")));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(20000), null), LocalDateTime.parse("2007-12-12T05:25:00"),LocalDateTime.parse("2007-12-12T05:25:20")));
		PluginFactory pf = new PluginFactory();
		PerfRuntimeOptions options = new PerfRuntimeOptions();
		Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
		plugins.setEventBusOnPlugins(eventBus);
		value = (long)25000;
		//(([0]start-[size-1]start).getSeconds()*1000) / times; = 20:35-25:00=265*1000/3 = 88,333
		//20:35+88.333=22:03.333 //2007-12-12T05:22:03.333
		points = 3;
		type = "chart";
		eventBus.send(new ConfigStatistics(eventBus.getTime(),eventBus.getTimeMillis(),StatisticsFormatter.CONFIG_MAXPOINTS,3));
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
		 		assertEquals((long)value,(long)result.getMin().get(groupName).getResultDuration().toMillis());
		 		break;
		 	case "max":
		 		assertEquals((long)value,(long)result.getMax().get(groupName).getResultDuration().toMillis());
		 		break;
		 	case "avg":
		 		assertEquals((long)value,(long)result.getAvg().get(groupName).getResultDuration().toMillis());
		 		break;
		 	case "chart":
		 		assertEquals((long)value,(long)result.getChartPoints().get(groupName).get(0).get("avg").getResultDuration().toMillis());
		 		assertEquals(points,result.getChartPoints().get(groupName).size());
		 		break;
		 }
	 }

}
