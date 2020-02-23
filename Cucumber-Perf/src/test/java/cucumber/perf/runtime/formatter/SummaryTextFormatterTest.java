package cucumber.perf.runtime.formatter;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import static org.hamcrest.CoreMatchers.containsString;
import org.junit.Test;

import cucumber.perf.api.event.SimulationFinished;
import cucumber.perf.api.event.StatisticsFinished;
import cucumber.perf.api.result.GroupResult;
import cucumber.perf.api.result.ScenarioResult;
import cucumber.perf.api.result.SimulationResult;
import cucumber.perf.api.result.StepResult;
import cucumber.perf.api.result.TestCase;
import cucumber.perf.api.result.TestStep;
import cucumber.perf.api.result.statistics.Statistics;
import cucumber.perf.runtime.PerfRuntimeOptions;
import cucumber.perf.runtime.TimeServiceEventBus;
import cucumber.perf.runtime.formatter.AppendableBuilder;
import cucumber.perf.runtime.formatter.PluginFactory;
import cucumber.perf.runtime.formatter.Plugins;
import cucumber.perf.runtime.formatter.StatisticsFormatter;
import cucumber.perf.runtime.formatter.SummaryTextFormatter;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;

public class SummaryTextFormatterTest {
	private TimeServiceEventBus eventBus = new TimeServiceEventBus(Clock.systemDefaultZone());

	@Test
	public void testSummaryTextFormatter() {
		try {
			new SummaryTextFormatter(new AppendableBuilder("file://C:/test/summarytext.csv"));
		} catch (CucumberException e) {
			fail("CucumberException");
		}
		assertTrue(true);
	}
	
	
	@Test
	public void testProcess() {
		SummaryTextFormatter stf = new SummaryTextFormatter(new AppendableBuilder("file://C:/test/summarytext.txt"));
		PluginFactory pf = new PluginFactory();
		PerfRuntimeOptions options = new PerfRuntimeOptions();
		Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
		plugins.addPlugin(stf);
		plugins.setEventBusOnPlugins(eventBus);
		eventBus.send(new StatisticsFinished(eventBus.getTime(),eventBus.getTimeMillis(),new Statistics()));
		assertTrue(deleteFile("C:/test/summarytext.txt"));
	}
	
	@Test
	public void testFinishReportWPrefix1() {
		SummaryTextFormatter stf = new SummaryTextFormatter(new AppendableBuilder("file://C:/test/summarytext|@H#1.txt"));
		PluginFactory pf = new PluginFactory();
		PerfRuntimeOptions options = new PerfRuntimeOptions();
		Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
		plugins.addPlugin(stf);
		plugins.setEventBusOnPlugins(eventBus);
		eventBus.send(new StatisticsFinished(eventBus.getTime(),eventBus.getTimeMillis(),new Statistics()));
		assertTrue(deleteFile("C:/test/summarytext"+LocalDateTime.now().getHour()+"1.txt"));
	}
	
	@Test
	public void testFinishReportWPrefix2() {
		SummaryTextFormatter stf = new SummaryTextFormatter(new AppendableBuilder("file://C:/test/summarytext|-#1-@yyyy.txt"));
		PluginFactory pf = new PluginFactory();
		PerfRuntimeOptions options = new PerfRuntimeOptions();
		Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
		plugins.addPlugin(stf);
		plugins.setEventBusOnPlugins(eventBus);
		eventBus.send(new StatisticsFinished(eventBus.getTime(),eventBus.getTimeMillis(),new Statistics()));
		
		assertTrue(deleteFile("C:/test/summarytext-1-"+LocalDateTime.now().getYear()+".txt"));
	}
	
	@Test
	public void testFinishReportWPrefixPaddingZeros() {
		SummaryTextFormatter stf = new SummaryTextFormatter(new AppendableBuilder("file://C:/test/summarytext|-#0001-@yyyy.txt"));
		PluginFactory pf = new PluginFactory();
		PerfRuntimeOptions options = new PerfRuntimeOptions();
		Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
		plugins.addPlugin(stf);
		plugins.setEventBusOnPlugins(eventBus);
		eventBus.send(new StatisticsFinished(eventBus.getTime(),eventBus.getTimeMillis(),new Statistics()));
	
		assertTrue(deleteFile("C:/test/summarytext-0001-"+LocalDateTime.now().getYear()+".txt"));
	}
	
	@Test
	public void testFinishReportWPrefixCountUp() {
		SummaryTextFormatter stf = new SummaryTextFormatter(new AppendableBuilder("file://C:/test/summarytext|-#0001.txt"));
		PluginFactory pf = new PluginFactory();
		PerfRuntimeOptions options = new PerfRuntimeOptions();
		Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
		plugins.addPlugin(stf);
		plugins.setEventBusOnPlugins(eventBus);
		for (int i = 1; i < 12; i ++)
		{
			eventBus.send(new StatisticsFinished(eventBus.getTime(),eventBus.getTimeMillis(),new Statistics()));
			if (i < 10)
			{
				assertTrue(deleteFile("C:/test/summarytext-000"+i+".txt"));
			}
			else
			{
				assertTrue(deleteFile("C:/test/summarytext-00"+i+".txt"));

			}
		}
		
	}
	
	
	@Test
	public void testFinishReportErrors() {
		SummaryTextFormatter stf = new SummaryTextFormatter(new AppendableBuilder("file://C:/test/summarytext.txt"));
		List<GroupResult> list = new ArrayList<GroupResult>();
		list.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofSeconds(1), null), LocalDateTime.now(), LocalDateTime.now()));
		list.add(new GroupResult("test2", new Result(Status.PASSED, Duration.ofSeconds(1), null), LocalDateTime.now(), LocalDateTime.now()));
		Throwable error =  new Throwable();
		error.setStackTrace(new StackTraceElement[] {new StackTraceElement("src.main.test.test","TestIt","testing.class",1),new StackTraceElement("src.main.test.test","TestIt","testing.class",2)});
		GroupResult fres = new GroupResult("test", new Result(Status.FAILED, Duration.ofMillis(1000), new Exception("Here is an error",error)), LocalDateTime.now(), LocalDateTime.now());
		ScenarioResult sc = new ScenarioResult("scentest", new TestCase(4, URI.create("features/ScenTest.feature"), "ScenTest 1", null, null, null), new Result(Status.FAILED, Duration.ofMillis(1000L), new Exception("Here is an error", new Throwable())), LocalDateTime.now(), LocalDateTime.now());
		sc.addChildResult(new StepResult("steptest", new TestStep(),new Result(Status.FAILED, Duration.ofMillis(1000), new Exception("Here is an error",new Throwable())),LocalDateTime.parse("2007-12-12T05:20:40"), LocalDateTime.parse("2007-12-12T05:20:45")));
		fres.addChildResult(sc);
		list.add(fres);
		PluginFactory pf = new PluginFactory();
		PerfRuntimeOptions options = new PerfRuntimeOptions();
		Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
		plugins.addPlugin(stf);
		plugins.setEventBusOnPlugins(eventBus);
		StatisticsFormatter s = new StatisticsFormatter();
		plugins.addPlugin(s);
		plugins.setEventBusOnPlugins(eventBus);
		eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(),new SimulationResult("test", new Result(Status.PASSED, Duration.ZERO, null), LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), list)));
		String filepath = "C:/test/summarytext.txt";
		String result = readFile(filepath);
		String compare = "\r\nSimulation: test Start: 2007-12-12T05:20:22 Stop: 2007-12-12T05:25:22 Duration: PT5M"+
				"\r\n\tGroup: test2 Count: 1 Avg: 1000.0 Min: 1000.0 Max: 1000.0 Concurrency: 0.0"+
				"\r\n\tGroup: test Count: 1 Avg: 1000.0 Min: 1000.0 Max: 1000.0 Concurrency: 0.0"+
				"\r\nErrors:"+
				  "\r\n\tGroup: test"+
				  "\r\n\t\tScenario: scentest"+
				  "\r\n\t\t\tStep: steptest"+
				  "\r\n\t\t\t  Count: 1 Timing: 2007-12-12T05:20:45 - 2007-12-12T05:20:45"+
				  "\r\n\t\t\t  Message: Here is an error"+
				  "\r\n\t\t\t  cucumber.perf.runtime.formatter.SummaryTextFormatterTest.testFinishReportErrors(SummaryTextFormatterTest.java:135)"+
					"\r\n\t\t\t  sun.reflect.NativeMethodAccessorImpl.invoke0(NativeMethodAccessorImpl.java:-2)";
		deleteFile(filepath);
		assertThat(result, containsString(compare));

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
	 * Get File Length
	 * @param filepath The file path to the file.
	 * @param size The size to compare to.
	 * @return True if same size else false;
	 */
	public static boolean isFileSize(String filepath,long size)
	{
		File file = new File(filepath);
		try {
			long l = file.length();
			return l==size;
		} catch (SecurityException e) {
			return false;
		}
	}
	
	/**
	 * Get File Length
	 * @param filepath The file path to the file.
	 * @return True if same size else false;
	 */
	public static long getFileSize(String filepath)
	{
		File file = new File(filepath);
		try {
			long l = file.length();
			return l;
		} catch (SecurityException e) {
			return -1;
		}
	}

}
