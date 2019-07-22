package cucumber.perf.formatter;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cucumber.api.Result;
import cucumber.api.Result.Type;
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
import cucumber.runner.TimeService;

public class StatisticsFormatterTest {
	private TimeServiceEventBus eventBus = new TimeServiceEventBus(TimeService.SYSTEM);
	private long value = (long) 0;
	private String groupName = "test";
	private String type = "min";
	
	@Test
	public void testStatistics() {
		List<GroupResult> res = new ArrayList<GroupResult>();
		res.add(new GroupResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Type.PASSED, (long)1200, null), LocalDateTime.now(),LocalDateTime.now()));
		StatisticsFormatter s = new StatisticsFormatter();
		PluginFactory pf = new PluginFactory();
		PerfRuntimeOptions options = new PerfRuntimeOptions();
		Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
		plugins.addPlugin(s);
		plugins.setEventBusOnPlugins(eventBus);
		value = (long)1200;
		type = "max";
		eventBus.registerHandlerFor(StatisticsFinished.class, statsEventhandler);
		eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), new SimulationResult("test",new Result(Result.Type.PASSED, (long)(0), null), LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), res)));
	}

	@Test
	public void testGetAvg() {
		List<GroupResult> res = new ArrayList<GroupResult>();
		res.add(new GroupResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Type.PASSED, (long)1100, null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Type.PASSED, (long)1200, null), LocalDateTime.now(),LocalDateTime.now()));
		StatisticsFormatter s = new StatisticsFormatter();
		s.setEventBus(eventBus);
		PluginFactory pf = new PluginFactory();
		PerfRuntimeOptions options = new PerfRuntimeOptions();
		Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
		plugins.addPlugin(s);
		plugins.setEventBusOnPlugins(eventBus);
		value = (long)1100;
		type = "avg";
		eventBus.registerHandlerFor(StatisticsFinished.class, statsEventhandler);
		eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), new SimulationResult("test",new Result(Result.Type.PASSED, (long)(0), null), LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), res)));
	}

	@Test
	public void testGetMin() {
		List<GroupResult> res = new ArrayList<GroupResult>();
		res.add(new GroupResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Type.PASSED, (long)1200, null), LocalDateTime.now(),LocalDateTime.now()));
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
		eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), new SimulationResult("test",new Result(Result.Type.PASSED, (long)(0), null),  LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), res)));
	}

	@Test
	public void testGetMax() {
		List<GroupResult> res = new ArrayList<GroupResult>();
		res.add(new GroupResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new GroupResult("test", new Result(Type.PASSED, (long)1200, null), LocalDateTime.now(),LocalDateTime.now()));
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
		eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), new SimulationResult("test",new Result(Result.Type.PASSED, (long)(0), null),  LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), res)));
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
		 		assertEquals((long)value,(long)result.getMin().get(groupName).getResultDuration());
		 		break;
		 	case "max":
		 		assertEquals((long)value,(long)result.getMax().get(groupName).getResultDuration());
		 		break;
		 	case "avg":
		 		assertEquals((long)value,(long)result.getAvg().get(groupName).getResultDuration());
		 		break;
		 }
	 }

}
