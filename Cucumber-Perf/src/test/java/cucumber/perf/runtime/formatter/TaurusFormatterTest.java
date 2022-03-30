package cucumber.perf.runtime.formatter;

import static org.junit.Assert.*;

import java.io.File;
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
import cucumber.perf.api.result.GroupResult;
import cucumber.perf.api.result.ScenarioResult;
import cucumber.perf.api.result.SimulationResult;
import cucumber.perf.api.result.TestCase;
import cucumber.perf.api.result.statistics.Statistics;
import cucumber.perf.runtime.PerfRuntimeOptions;
import cucumber.perf.runtime.TimeServiceEventBus;
import cucumber.perf.runtime.formatter.AppendableBuilder;
import cucumber.perf.runtime.formatter.PluginFactory;
import cucumber.perf.runtime.formatter.Plugins;
import cucumber.perf.runtime.formatter.StatisticsFormatter;
import cucumber.perf.runtime.formatter.TaurusFormatter;
import cucumber.runner.TimeService;
import cucumber.runtime.CucumberException;

public class TaurusFormatterTest {
	private TimeServiceEventBus eventBus = new TimeServiceEventBus(TimeService.SYSTEM);

	@Test
	public void testTaurusFormatter() {
		try {
			new TaurusFormatter(new AppendableBuilder("file://C:/test/taurusout.csv"));
		} catch (CucumberException e) {
			fail("CucumberException");
		}
		assertTrue(true);
	}
	
	
	@Test
	public void testProcess() {
		TaurusFormatter stf = new TaurusFormatter(new AppendableBuilder("file://C:/test/taurusout.csv"));
		PluginFactory pf = new PluginFactory();
		PerfRuntimeOptions options = new PerfRuntimeOptions();
		Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
		plugins.addPlugin(stf);
		plugins.setEventBusOnPlugins(eventBus);
		eventBus.send(new StatisticsFinished(eventBus.getTime(),eventBus.getTimeMillis(),new Statistics()));
		assertTrue(deleteFile("C:/test/taurusout.csv"));
	}
	
	@Test
	public void testFinishReportWPrefix1() {
		TaurusFormatter stf = new TaurusFormatter(new AppendableBuilder("file://C:/test/taurusout|@H#1.csv"));
		PluginFactory pf = new PluginFactory();
		PerfRuntimeOptions options = new PerfRuntimeOptions();
		Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
		plugins.addPlugin(stf);
		plugins.setEventBusOnPlugins(eventBus);
		eventBus.send(new StatisticsFinished(eventBus.getTime(),eventBus.getTimeMillis(),new Statistics()));
		assertTrue(deleteFile("C:/test/taurusout"+LocalDateTime.now().getHour()+"1.csv"));
	}
	
	@Test
	public void testFinishReportWPrefix2() {
		TaurusFormatter stf = new TaurusFormatter(new AppendableBuilder("file://C:/test/taurusout|-#1-@yyyy.csv"));
		PluginFactory pf = new PluginFactory();
		PerfRuntimeOptions options = new PerfRuntimeOptions();
		Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
		plugins.addPlugin(stf);
		plugins.setEventBusOnPlugins(eventBus);
		eventBus.send(new StatisticsFinished(eventBus.getTime(),eventBus.getTimeMillis(),new Statistics()));
		
		assertTrue(deleteFile("C:/test/taurusout-1-"+LocalDateTime.now().getYear()+".csv"));
	}
	
	@Test
	public void testFinishReportWPrefixPaddingZeros() {
		TaurusFormatter stf = new TaurusFormatter(new AppendableBuilder("file://C:/test/taurusout|-#0001-@yyyy.csv"));
		PluginFactory pf = new PluginFactory();
		PerfRuntimeOptions options = new PerfRuntimeOptions();
		Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
		plugins.addPlugin(stf);
		plugins.setEventBusOnPlugins(eventBus);
		eventBus.send(new StatisticsFinished(eventBus.getTime(),eventBus.getTimeMillis(),new Statistics()));
	
		assertTrue(deleteFile("C:/test/taurusout-0001-"+LocalDateTime.now().getYear()+".csv"));
	}
	
	@Test
	public void testFinishReportWPrefixCountUp() {
		TaurusFormatter stf = new TaurusFormatter(new AppendableBuilder("file://C:/test/taurusout|-#0001.csv"));
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
				assertTrue(deleteFile("C:/test/taurusout-000"+i+".csv"));
			}
			else
			{
				assertTrue(deleteFile("C:/test/taurusout-00"+i+".csv"));

			}
		}
		
	}
	
	
	@Test
	public void testFinishReportErrors() {
		TaurusFormatter stf = new TaurusFormatter(new AppendableBuilder("file://C:/test/taurusout.csv"));
		 List<GroupResult> list = new ArrayList<GroupResult>();
		list.add(new GroupResult("test", new Result(Type.PASSED, (long)1000000, null), LocalDateTime.now(), LocalDateTime.now()));
		list.add(new GroupResult("test2", new Result(Type.PASSED, (long)1000000, null), LocalDateTime.now(), LocalDateTime.now()));
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
		String filepath = "C:/test/taurusout.csv";
		String result = readFile(filepath);
		String compare = "\r\nlabel,avg_ct,avg_lt,avg_rt,bytes,concurrency,fail,stdev_rt,succ,throughput,perc_0.0,perc_50.0,perc_90.0,perc_95.0,perc_99.0,perc_99.9,perc_100.0,rc_200" + 
				"\r\n\"test2\",0.00000,0.00000,1.0,0,0.0,0,0.00000,0.00000,1,1.0,0.00000,0.00000,0.00000,0.00000,0.00000,1.0,0" + 
				"\r\n\"test\",0.00000,0.00000,1.0,0,0.0,0,0.00000,0.00000,1,1.0,0.00000,0.00000,0.00000,0.00000,0.00000,1.0,0";
		assertTrue(deleteFile(filepath));
		assertThat(result, containsString(compare));
	}
	
	@Test
	public void testFinishReportLabel() {
		TaurusFormatter stf = new TaurusFormatter(new AppendableBuilder("file://C:/test/taurusout.csv"));
		 List<GroupResult> list = new ArrayList<GroupResult>();
		list.add(new GroupResult("test,what\"as\"", new Result(Type.PASSED, (long)1000000, null), LocalDateTime.now(), LocalDateTime.now()));
		list.add(new GroupResult("test2,a,\"as\"", new Result(Type.PASSED, (long)1000000, null), LocalDateTime.now(), LocalDateTime.now()));
		Throwable error =  new Throwable();
		PluginFactory pf = new PluginFactory();
		PerfRuntimeOptions options = new PerfRuntimeOptions();
		Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
		plugins.addPlugin(stf);
		plugins.setEventBusOnPlugins(eventBus);
		StatisticsFormatter s = new StatisticsFormatter();
		plugins.addPlugin(s);
		plugins.setEventBusOnPlugins(eventBus);
		eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(),new SimulationResult("test", new Result(Result.Type.PASSED, (long)0, null), LocalDateTime.parse("2007-12-12T05:20:22"),LocalDateTime.parse("2007-12-12T05:25:22"), list)));
		String filepath = "C:/test/taurusout.csv";
		String result = readFile(filepath);
		String compare = "\r\nlabel,avg_ct,avg_lt,avg_rt,bytes,concurrency,fail,stdev_rt,succ,throughput,perc_0.0,perc_50.0,perc_90.0,perc_95.0,perc_99.0,perc_99.9,perc_100.0,rc_200" + 
				"\r\n\"test,what\"\"as\"\"\",0.00000,0.00000,1.0,0,0.0,0,0.00000,0.00000,1,1.0,0.00000,0.00000,0.00000,0.00000,0.00000,1.0,0" + 
				"\r\n\"test2,a,\"\"as\"\"\",0.00000,0.00000,1.0,0,0.0,0,0.00000,0.00000,1,1.0,0.00000,0.00000,0.00000,0.00000,0.00000,1.0,0";
		assertTrue(deleteFile(filepath));
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
