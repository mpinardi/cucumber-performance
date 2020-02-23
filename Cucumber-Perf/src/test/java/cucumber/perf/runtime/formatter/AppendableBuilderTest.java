package cucumber.perf.runtime.formatter;

import static org.junit.Assert.*;

import java.io.File;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cucumber.perf.api.event.SimulationFinished;
import cucumber.perf.api.result.GroupResult;
import cucumber.perf.api.result.SimulationResult;
import cucumber.perf.runtime.PerfRuntimeOptions;
import cucumber.perf.runtime.TimeServiceEventBus;
import cucumber.perf.runtime.formatter.AppendableBuilder;
import cucumber.perf.runtime.formatter.SummaryTextFormatter;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import cucumber.perf.runtime.formatter.PluginFactory;
import cucumber.perf.runtime.formatter.Plugins;

public class AppendableBuilderTest {
	private TimeServiceEventBus eventBus = new TimeServiceEventBus(Clock.systemDefaultZone());
	
	@Test
	public void testFinishReportWPrefix1() {
		try {
			List<GroupResult> res = new ArrayList<GroupResult>();
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(20000L), null), LocalDateTime.parse("2007-12-12T05:20:35"),LocalDateTime.parse("2007-12-12T05:20:55")));
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(32000L), null), LocalDateTime.parse("2007-12-12T05:22:01"),LocalDateTime.parse("2007-12-12T05:22:33")));
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(20000L), null), LocalDateTime.parse("2007-12-12T05:25:00"),LocalDateTime.parse("2007-12-12T05:25:20")));
			SummaryTextFormatter cpf = new SummaryTextFormatter(new AppendableBuilder("file://C:/test/summaryout|@H#1.csv"));
			PluginFactory pf = new PluginFactory();
			PerfRuntimeOptions options = new PerfRuntimeOptions();
			Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
			plugins.addPlugin(cpf);
			plugins.setEventBusOnPlugins(eventBus);
			eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), new SimulationResult("test",new Result(Status.PASSED, Duration.ZERO, null),  LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), res)));		
		} catch (CucumberException e) {
			fail("CucumberException");
		}
		
		assertTrue(deleteFile("C:/test/summaryout"+LocalDateTime.now().getHour()+"1.csv"));
	}
	
	@Test
	public void testFinishReportWPrefix2() {
		try {
			List<GroupResult> res = new ArrayList<GroupResult>();
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(20000L), null), LocalDateTime.parse("2007-12-12T05:20:35"),LocalDateTime.parse("2007-12-12T05:20:55")));
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(32000L), null), LocalDateTime.parse("2007-12-12T05:22:01"),LocalDateTime.parse("2007-12-12T05:22:33")));
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(20000L), null), LocalDateTime.parse("2007-12-12T05:25:00"),LocalDateTime.parse("2007-12-12T05:25:20")));
			SummaryTextFormatter cpf = new SummaryTextFormatter(new AppendableBuilder("file://C:/test/summaryout|-#1-@yyyy.csv"));
			PluginFactory pf = new PluginFactory();
			PerfRuntimeOptions options = new PerfRuntimeOptions();
			Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
			plugins.addPlugin(cpf);
			plugins.setEventBusOnPlugins(eventBus);
			eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), new SimulationResult("test",new Result(Status.PASSED, Duration.ZERO, null),  LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), res)));		
		} catch (CucumberException e) {
			fail("CucumberException");
		}
		assertTrue(deleteFile("C:/test/summaryout-1-"+LocalDateTime.now().getYear()+".csv"));
	}
	
	@Test
	public void testFinishReportWPrefixPaddingZeros() {
		try {
			List<GroupResult> res = new ArrayList<GroupResult>();
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(20000L), null), LocalDateTime.parse("2007-12-12T05:20:35"),LocalDateTime.parse("2007-12-12T05:20:55")));
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(2000L), null), LocalDateTime.parse("2007-12-12T05:22:01"),LocalDateTime.parse("2007-12-12T05:22:33")));
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(20000L), null), LocalDateTime.parse("2007-12-12T05:25:00"),LocalDateTime.parse("2007-12-12T05:25:20")));
			SummaryTextFormatter cpf = new SummaryTextFormatter(new AppendableBuilder("file://C:/test/summaryout|-#0001-@yyyy.csv"));
			PluginFactory pf = new PluginFactory();
			PerfRuntimeOptions options = new PerfRuntimeOptions();
			Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
			plugins.addPlugin(cpf);
			plugins.setEventBusOnPlugins(eventBus);
			eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), new SimulationResult("test",new Result(Status.PASSED, Duration.ZERO, null),  LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), res)));		
		} catch (CucumberException e) {
			fail("CucumberException");
		}
		assertTrue(deleteFile("C:/test/summaryout-0001-"+LocalDateTime.now().getYear()+".csv"));
	}
	
	@Test
	public void testFinishReportWPrefixCountUp() {
		try {
			List<GroupResult> res = new ArrayList<GroupResult>();
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(20000L), null), LocalDateTime.parse("2007-12-12T05:20:35"),LocalDateTime.parse("2007-12-12T05:20:55")));
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(32000L), null), LocalDateTime.parse("2007-12-12T05:22:01"),LocalDateTime.parse("2007-12-12T05:22:33")));
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(20000L), null), LocalDateTime.parse("2007-12-12T05:25:00"),LocalDateTime.parse("2007-12-12T05:25:20")));
			SummaryTextFormatter cpf = new SummaryTextFormatter(new AppendableBuilder("file://C:/test/summaryout|-#0001.csv"));
			PluginFactory pf = new PluginFactory();
			PerfRuntimeOptions options = new PerfRuntimeOptions();
			Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
			plugins.addPlugin(cpf);
			plugins.setEventBusOnPlugins(eventBus);
			
			for (int i = 1; i < 12; i ++)
			{
				eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), new SimulationResult("test",new Result(Status.PASSED, Duration.ZERO, null),  LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), res)));		
				if (i < 10)
				{
					assertTrue(deleteFile("C:/test/summaryout-000"+i+".csv"));
				}
				else
				{
					assertTrue(deleteFile("C:/test/summaryout-00"+i+".csv"));

				}
			}
		} catch (CucumberException e) {
			fail("CucumberException");
		}
	}
	
	/**
	 * Delete a file
	 * @param filepath The file path to the file.
	 * @return True if deleted else false;
	 */
	public static boolean deleteFile(String filepath)
	{
		File file = new File(filepath);
		try {
			return file.delete();
		} catch (SecurityException e) {
			return false;
		}
	}

}
