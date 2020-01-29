package cucumber.perf.formatter;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import static org.hamcrest.CoreMatchers.containsString;
import org.junit.Test;

import cucumber.api.Result;
import cucumber.api.Result.Type;
import cucumber.perf.api.event.SimulationFinished;
import cucumber.perf.api.event.StatisticsFinished;
import cucumber.perf.api.formatter.Statistics;
import cucumber.perf.api.result.GroupResult;
import cucumber.perf.api.result.ScenarioResult;
import cucumber.perf.api.result.SimulationResult;
import cucumber.perf.api.result.TestCase;
import cucumber.perf.runtime.PerfRuntimeOptions;
import cucumber.perf.runtime.TimeServiceEventBus;
import cucumber.perf.runtime.formatter.AppendableBuilder;
import cucumber.perf.runtime.formatter.PluginFactory;
import cucumber.perf.runtime.formatter.Plugins;
import cucumber.perf.runtime.formatter.StatisticsFormatter;
import cucumber.perf.runtime.formatter.SummaryTextFormatter;
import cucumber.runner.TimeService;

public class SummaryTextFormatterTest {
	private TimeServiceEventBus eventBus = new TimeServiceEventBus(TimeService.SYSTEM);
	
	@Test
	public void testSummaryTextFormatter() {
		try {
			new SummaryTextFormatter(new AppendableBuilder("file://C:/test/summary.txt"));
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
		assertTrue(true);
	}

	@Test
	public void testProcess() {
		try {
			SummaryTextFormatter stf = new SummaryTextFormatter(new AppendableBuilder("file://C:/test/summarytext.txt"));
			PluginFactory pf = new PluginFactory();
			PerfRuntimeOptions options = new PerfRuntimeOptions();
			Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
			plugins.addPlugin(stf);
			plugins.setEventBusOnPlugins(eventBus);
			eventBus.send(new StatisticsFinished(eventBus.getTime(),eventBus.getTimeMillis(),new Statistics()));
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
		assertTrue(deleteFile("C:/test/summarytext.txt"));
	}
	
	@Test
	public void testFinishReportWPrefix1() {
		try {
			SummaryTextFormatter stf = new SummaryTextFormatter(new AppendableBuilder("file://C:/test/summarytext|@H#1.txt"));
			PluginFactory pf = new PluginFactory();
			PerfRuntimeOptions options = new PerfRuntimeOptions();
			Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
			plugins.addPlugin(stf);
			plugins.setEventBusOnPlugins(eventBus);
			eventBus.send(new StatisticsFinished(eventBus.getTime(),eventBus.getTimeMillis(),new Statistics()));
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
		
		assertTrue(deleteFile("C:/test/summarytext"+LocalDateTime.now().getHour()+"1.txt"));
	}
	
	@Test
	public void testFinishReportWPrefix2() {
		try {
			SummaryTextFormatter stf = new SummaryTextFormatter(new AppendableBuilder("file://C:/test/summarytext|-#1-@yyyy.txt"));
			PluginFactory pf = new PluginFactory();
			PerfRuntimeOptions options = new PerfRuntimeOptions();
			Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
			plugins.addPlugin(stf);
			plugins.setEventBusOnPlugins(eventBus);
			eventBus.send(new StatisticsFinished(eventBus.getTime(),eventBus.getTimeMillis(),new Statistics()));
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
		
		assertTrue(deleteFile("C:/test/summarytext-1-"+LocalDateTime.now().getYear()+".txt"));
	}
	
	@Test
	public void testFinishReportWPrefixPaddingZeros() {
		try {
			SummaryTextFormatter stf = new SummaryTextFormatter(new AppendableBuilder("file://C:/test/summarytext|-#0001-@yyyy.txt"));
			PluginFactory pf = new PluginFactory();
			PerfRuntimeOptions options = new PerfRuntimeOptions();
			Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
			plugins.addPlugin(stf);
			plugins.setEventBusOnPlugins(eventBus);
			eventBus.send(new StatisticsFinished(eventBus.getTime(),eventBus.getTimeMillis(),new Statistics()));
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
		
		assertTrue(deleteFile("C:/test/summarytext-0001-"+LocalDateTime.now().getYear()+".txt"));
	}
	
	@Test
	public void testFinishReportWPrefixCountUp() {
		try {
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
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
		
	}
	
	
	@Test
	public void testFinishReportErrors() {
		try {
			SummaryTextFormatter stf = new SummaryTextFormatter(new AppendableBuilder("file://C:/test/summarytext.txt"));
			 List<GroupResult> list = new ArrayList<GroupResult>();
			list.add(new GroupResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(), LocalDateTime.now()));
			list.add(new GroupResult("test2", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(), LocalDateTime.now()));
			Throwable error =  new Throwable();
			error.setStackTrace(new StackTraceElement[] {new StackTraceElement("src.main.test.test","TestIt","testing.class",1),new StackTraceElement("src.main.test.test","TestIt","testing.class",2)});
			GroupResult fres = new GroupResult("test", new Result(Type.FAILED, (long)1000, new Exception("Here is an error",error)), LocalDateTime.now(), LocalDateTime.now());
			fres.addChildResult(new ScenarioResult("scentest", new TestCase(4, "features/ScenTest.feature", "ScenTest 1", null, null, null), new Result(Type.FAILED, (long)1000, new Exception("Here is an error",error)), LocalDateTime.now(), LocalDateTime.now()));
			list.add(fres);
			PluginFactory pf = new PluginFactory();
			PerfRuntimeOptions options = new PerfRuntimeOptions();
			Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
			plugins.addPlugin(stf);
			plugins.setEventBusOnPlugins(eventBus);
			StatisticsFormatter s = new StatisticsFormatter();
			plugins.addPlugin(s);
			plugins.setEventBusOnPlugins(eventBus);
			eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(),new SimulationResult("test", new Result(Result.Type.PASSED, (long)0, null), LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), list)));
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
		String filepath = "C:/test/summarytext.txt";
		String result = readFile(filepath);
		String compare = "\r\nSimulation: test Start: 2007-12-12T05:20:22 Stop: 2007-12-12T05:25:22 Duration: PT5M"+
				"\r\n\tGroup: test2 Count: 1 Avg: 0 Min: 0 Max: 0"+
				"\r\n\tGroup: test Count: 1 Avg: 0 Min: 0 Max: 0"+
				"\r\nErrors:"+
				  "\r\n\tScenario: test"+
				  "\r\n\t\tStep: test"+
					"\r\n\t\tHere is an error"+
					"\r\n\t\tcucumber.perf.formatter.SummaryTextFormatterTest.testFinishReportErrors(SummaryTextFormatterTest.java:164)"+
					"\r\n\t\tsun.reflect.NativeMethodAccessorImpl.invoke0(NativeMethodAccessorImpl.java:-2)"+
					"\r\n\t\tsun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)"+
					"\r\n\t\tsun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)"+
					"\r\n\t\tjava.lang.reflect.Method.invoke(Method.java:498)"+
					"\r\n\t\torg.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)"+
					"\r\n\t\torg.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)"+
					"\r\n\t\torg.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)"+
					"\r\n\t\torg.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)"+
					"\r\n\t\torg.junit.runners.ParentRunner.runLeaf(ParentRunner.java:325)"+
					"\r\n\t\torg.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:78)"+
					"\r\n\t\torg.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:57)"+
					"\r\n\t\torg.junit.runners.ParentRunner$3.run(ParentRunner.java:290)"+
					"\r\n\t\torg.junit.runners.ParentRunner$1.schedule(ParentRunner.java:71)"+
					"\r\n\t\torg.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)"+
					"\r\n\t\torg.junit.runners.ParentRunner.access$000(ParentRunner.java:58)"+
					"\r\n\t\torg.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:268)"+
					"\r\n\t\torg.junit.runners.ParentRunner.run(ParentRunner.java:363)";
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
