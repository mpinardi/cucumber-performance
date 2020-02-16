package cucumber.perf.runtime.formatter;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.Test;

import cucumber.api.Result;
import cucumber.api.Result.Type;
import cucumber.perf.api.event.PerfRunStarted;
import cucumber.perf.api.event.SimulationFinished;
import cucumber.perf.api.event.StatisticsFinished;
import cucumber.perf.api.result.GroupResult;
import cucumber.perf.api.result.SimulationResult;
import cucumber.perf.api.result.statistics.Statistics;
import cucumber.perf.runtime.PerfRuntimeOptions;
import cucumber.perf.runtime.TimeServiceEventBus;
import cucumber.perf.runtime.formatter.AppendableBuilder;
import cucumber.perf.runtime.formatter.ChartPointsFormatter;
import cucumber.perf.runtime.formatter.PluginFactory;
import cucumber.perf.runtime.formatter.Plugins;
import cucumber.runner.TimeService;
import cucumber.runtime.CucumberException;

public class ChartPointsFormatterTest {
	private TimeServiceEventBus eventBus = new TimeServiceEventBus(TimeService.SYSTEM);
	
	@Test
	public void testChartPointsFormatter() {
		try {
			new ChartPointsFormatter(new AppendableBuilder("file://C:/test/chartpoints.csv"));
		} catch (CucumberException e) {
			fail("CucumberException");
		}
		assertTrue(true);
	}
	
	@Test
	public void testChartPointsFormatterTwoArg() {
		try {
			ChartPointsFormatter cpf =new ChartPointsFormatter(new AppendableBuilder("file://C:/test/chartpoints.csv"), new String[]{"2"});
			PluginFactory pf = new PluginFactory();
			PerfRuntimeOptions options = new PerfRuntimeOptions();
			Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
			plugins.addPlugin(cpf);
			plugins.setEventBusOnPlugins(eventBus);
			eventBus.send(new PerfRunStarted(eventBus.getTime(),eventBus.getTimeMillis()));
		} catch (CucumberException e) {
			fail("CucumberException");
		}
		assertTrue(true);
	}

	@Test
	public void testProcess() {
		try {
			ChartPointsFormatter cpf = new ChartPointsFormatter(new AppendableBuilder("file://C:/test/chartpoints.csv"));
			PluginFactory pf = new PluginFactory();
			PerfRuntimeOptions options = new PerfRuntimeOptions();
			Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
			plugins.addPlugin(cpf);
			plugins.setEventBusOnPlugins(eventBus);
			eventBus.send(new StatisticsFinished(eventBus.getTime(),eventBus.getTimeMillis(),new Statistics()));
		} catch (CucumberException e) {
			fail("CucumberException");
		}

		assertFalse(deleteFile("C:/test/chartpoints.csv"));
	}
	
	@Test
	public void testChartPoints() {
		try {
			List<GroupResult> res = new ArrayList<GroupResult>();
			res.add(new GroupResult("test", new Result(Type.PASSED, (long)20000, null), LocalDateTime.parse("2007-12-12T05:20:35"),LocalDateTime.parse("2007-12-12T05:20:55")));
			res.add(new GroupResult("test", new Result(Type.PASSED, (long)30000, null), LocalDateTime.parse("2007-12-12T05:21:10"),LocalDateTime.parse("2007-12-12T05:21:40")));
			res.add(new GroupResult("test", new Result(Type.PASSED, (long)32000, null), LocalDateTime.parse("2007-12-12T05:22:01"),LocalDateTime.parse("2007-12-12T05:22:33")));
			res.add(new GroupResult("test", new Result(Type.PASSED, (long)25000, null), LocalDateTime.parse("2007-12-12T05:22:40"),LocalDateTime.parse("2007-12-12T05:23:05")));
			res.add(new GroupResult("test", new Result(Type.PASSED, (long)40000, null), LocalDateTime.parse("2007-12-12T05:23:10"),LocalDateTime.parse("2007-12-12T05:23:50")));
			res.add(new GroupResult("test", new Result(Type.PASSED, (long)30000, null), LocalDateTime.parse("2007-12-12T05:23:55"),LocalDateTime.parse("2007-12-12T05:24:25")));
			res.add(new GroupResult("test", new Result(Type.PASSED, (long)26000, null), LocalDateTime.parse("2007-12-12T05:24:30"),LocalDateTime.parse("2007-12-12T05:24:56")));
			res.add(new GroupResult("test", new Result(Type.PASSED, (long)20000, null), LocalDateTime.parse("2007-12-12T05:25:00"),LocalDateTime.parse("2007-12-12T05:25:20")));
			PluginFactory pf = new PluginFactory();
			PerfRuntimeOptions options = new PerfRuntimeOptions();
			Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
			plugins.setEventBusOnPlugins(eventBus);
			ChartPointsFormatter cpf = new ChartPointsFormatter(new AppendableBuilder("file://C:/test/chartpoints.csv"),new String[] {"3"});
			plugins.addPlugin(cpf);
			plugins.setEventBusOnPlugins(eventBus);

			eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), new SimulationResult("test",new Result(Result.Type.PASSED, (long)(0), null),  LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), res)));
		} catch (CucumberException e) {
			fail("CucumberException");
		}
		String filepath = "C:/test/chartpoints.csv";
		String result = readFile(filepath);
		String compare = "\r\ntest,,,cnt,2007-12-12T05:20:55,2"
				+ "\r\ntest,,,cnt,2007-12-12T05:22:33,2"
				+ "\r\ntest,,,cnt,2007-12-12T05:23:50,4"
				+ "\r\ntest,,,avg,2007-12-12T05:20:55,25000.0"
				+ "\r\ntest,,,avg,2007-12-12T05:22:33,28500.0"
				+ "\r\ntest,,,avg,2007-12-12T05:23:50,29000.0"
				+ "\r\ntest,,,min,2007-12-12T05:20:55,20000.0"
				+ "\r\ntest,,,min,2007-12-12T05:22:33,25000.0"
				+ "\r\ntest,,,min,2007-12-12T05:23:50,20000.0"
				+ "\r\ntest,,,max,2007-12-12T05:20:55,30000.0"
				+ "\r\ntest,,,max,2007-12-12T05:22:33,32000.0"
				+ "\r\ntest,,,max,2007-12-12T05:23:50,40000.0"
				+ "\r\ntest,,,cncrnt,2007-12-12T05:20:55,0.17193"
				+ "\r\ntest,,,cncrnt,2007-12-12T05:22:33,0.49825"
				+ "\r\ntest,,,cncrnt,2007-12-12T05:23:50,0.94737";
		assertTrue(deleteFile(filepath));
		assertThat(result, containsString(compare));
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
	
	/**
	 * Read a file
	 * @param filepath The file path to the file.
	 * @return String the file contents
	 */
	public static String readFile(String filepath)
	{
		String result ="";
		try {
		 Scanner sc = new Scanner(new File(filepath)); 
		    while (sc.hasNextLine()) 
		      result+="\r\n"+sc.nextLine();
		    sc.close();
		} catch (Exception e) {
			return "";
		}
		return result;
	}

}
