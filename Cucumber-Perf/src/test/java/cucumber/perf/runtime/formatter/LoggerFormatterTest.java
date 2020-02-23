package cucumber.perf.runtime.formatter;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.Ignore;

import cucumber.perf.api.PerfGroup;
import cucumber.perf.api.event.EventHandler;
import cucumber.perf.api.event.GroupFinished;
import cucumber.perf.api.event.PerfRunStarted;
import cucumber.perf.api.event.SimulationFinished;
import cucumber.perf.api.event.SimulationStarted;
import cucumber.perf.api.event.StatisticsStarted;
import cucumber.perf.api.result.GroupResult;
import cucumber.perf.api.result.ScenarioResult;
import cucumber.perf.api.result.StepResult;
import cucumber.perf.api.result.TestCase;
import cucumber.perf.api.result.TestStep;
import cucumber.perf.runtime.PerfRuntimeOptions;
import cucumber.perf.runtime.TimeServiceEventBus;
import cucumber.perf.runtime.formatter.AppendableBuilder;
import cucumber.perf.runtime.formatter.LoggerFormatter;
import cucumber.perf.runtime.formatter.PluginFactory;
import cucumber.perf.runtime.formatter.Plugins;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.internal.gherkin.deps.com.google.gson.Gson;
import io.cucumber.core.internal.gherkin.deps.com.google.gson.stream.JsonReader;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;

public class LoggerFormatterTest {
	private TimeServiceEventBus eventBus = new TimeServiceEventBus(Clock.systemDefaultZone());
	private boolean isStatisticsStarted = false;
	private EventHandler<StatisticsStarted> statisticsStartedListener = new EventHandler<StatisticsStarted>() {
		@Override
		public void receive(StatisticsStarted event) {
			isStatisticsStarted = true;
		}
    };
    
	@Test
	public void testLoggerFormatter() {
		try {
			new LoggerFormatter(new AppendableBuilder("file://C:/test/log.json"));
			assertFalse(deleteFile("C:/test/log.json"));
		} catch (CucumberException e) {
			fail("Issue");
		}
	}

	@Test
	public void testOutput() {
		try {
			LoggerFormatter stf = new LoggerFormatter(new AppendableBuilder("file://C:/test/log.json"));
			PluginFactory pf = new PluginFactory();
			PerfRuntimeOptions options = new PerfRuntimeOptions();
			Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
			plugins.addPlugin(stf);
			plugins.setEventBusOnPlugins(eventBus);
			eventBus.send(new SimulationStarted(eventBus.getTime(),eventBus.getTimeMillis(), "test"));
			eventBus.send(new GroupFinished(eventBus.getTime(),eventBus.getTimeMillis(),0, new PerfGroup("group", "test.feature", 1, 10, null),new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1000L), null), LocalDateTime.now(), LocalDateTime.now())));
		} catch (CucumberException e) {
			fail("Issue");
		}
		eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), null));
		long fs = getFileSize("C:/test/log.json");
		assertTrue(fs>=1);
		assertTrue(deleteFile("C:/test/log.json"));
	}
	
	@Test @Ignore
	public void processFileNotExist() {
		try {
			LoggerFormatter stf = new LoggerFormatter(new AppendableBuilder("file://C:/test/log.json"),new String[] {"file://C:/test/log.json"});
			PluginFactory pf = new PluginFactory();
			PerfRuntimeOptions options = new PerfRuntimeOptions();
			Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
			plugins.addPlugin(stf);
			plugins.setEventBusOnPlugins(eventBus);
		} catch (CucumberException e) {
			fail("Issue");
		}
		eventBus.send(new PerfRunStarted(eventBus.getTime(),eventBus.getTimeMillis()));
		assertFalse(deleteFile("C:/test/log.json"));
	}
	
	@Test
	public void testProcessFile() {
		try {
			LoggerFormatter stf = new LoggerFormatter(new AppendableBuilder("file://C:/test/log.json"));
			PluginFactory pf = new PluginFactory();
			PerfRuntimeOptions options = new PerfRuntimeOptions();
			Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
			plugins.addPlugin(stf);
			plugins.setEventBusOnPlugins(eventBus);
			eventBus.send(new SimulationStarted(eventBus.getTime(),eventBus.getTimeMillis(), "test"));
			eventBus.send(new GroupFinished(eventBus.getTime(),eventBus.getTimeMillis(),0, new PerfGroup("group", "test.feature", 1, 10, null),new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1000L), null), LocalDateTime.now(), LocalDateTime.now())));
			eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), null));
		} catch (CucumberException e) {
			fail("Issue");
		}
		try {
			eventBus.registerHandlerFor(StatisticsStarted.class, statisticsStartedListener);
			LoggerFormatter stf = new LoggerFormatter(new AppendableBuilder("file://C:/test/log.json"),new String[] {"file://C:/test/log.json"});
			PluginFactory pf = new PluginFactory();
			PerfRuntimeOptions options = new PerfRuntimeOptions();
			Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
			plugins.addPlugin(stf);
			plugins.setEventBusOnPlugins(eventBus);		
			eventBus.send(new PerfRunStarted(eventBus.getTime(),eventBus.getTimeMillis()));
		} catch (CucumberException e) {
			fail("Issue");
		}
		assertTrue(isStatisticsStarted);
		assertTrue(deleteFile("C:/test/log.json"));
	}
	

	@SuppressWarnings("unchecked")
	@Test
	public void testLogErrors() {
		LoggerFormatter stf = null;
		try {
			stf = new LoggerFormatter(new AppendableBuilder("file://C:/test/log.json"));
			List<GroupResult> list = new ArrayList<GroupResult>();
			list.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1000L), null), LocalDateTime.now(), LocalDateTime.now()));
			list.add(new GroupResult("test2", new Result(Status.PASSED, Duration.ofMillis(2300L), null), LocalDateTime.now(), LocalDateTime.now()));
			Throwable error =  new Throwable();
			error.setStackTrace(new StackTraceElement[] {new StackTraceElement("src.main.test.test","TestIt","testing.class",1),new StackTraceElement("src.main.test.test","TestIt","testing.class",2)});
			GroupResult fres = new GroupResult("test", new Result(Status.FAILED, Duration.ofMillis(1000L), new Exception("Here is an error",error)), LocalDateTime.now(), LocalDateTime.now());
			fres.addChildResult(new ScenarioResult("scentest", new TestCase(4, URI.create("features/ScenTest.feature"), "ScenTest 1", null, null, null), new Result(Status.FAILED, Duration.ofMillis(1000L), new Exception("Here is an error",error)), LocalDateTime.now(), LocalDateTime.now()));
			list.add(fres);
			PluginFactory pf = new PluginFactory();
			PerfRuntimeOptions options = new PerfRuntimeOptions();
			Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
			plugins.addPlugin(stf);
			plugins.setEventBusOnPlugins(eventBus);
			eventBus.send(new SimulationStarted(eventBus.getTime(),eventBus.getTimeMillis(), "test"));
			for (GroupResult res : list) {
				eventBus.send(new GroupFinished(eventBus.getTime(),eventBus.getTimeMillis(),0, new PerfGroup("group", "test.feature", 1, 10, null),res));
			}
			
		} catch (CucumberException e) {
			fail("Issue");
		}
		eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), null));
		List<GroupResult> resultList = new ArrayList<GroupResult>();
		List<Map<String, Object>> json = (List<Map<String, Object>>) getFileJSON("C:/test/log.json",resultList.getClass());
		json.remove(0);
		resultList = stf.createGroupResultList(json);
		assertEquals((long)2300,resultList.get(1).getResultDuration().toMillis());
		assertTrue(deleteFile("C:/test/log.json"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testLogWithChildren() {
		LoggerFormatter stf = null;
		try {
			stf = new LoggerFormatter(new AppendableBuilder("file://C:/test/log.json"));
			GroupResult gr = new GroupResult("test", new Result(Status.PASSED, Duration.ofMillis(1000), null), LocalDateTime.now(), LocalDateTime.now());
			List<String> pickletags = new ArrayList<String>();
			List<GroupResult> list = new ArrayList<GroupResult>();
			pickletags.add("mytag");
			List<io.cucumber.plugin.event.TestStep> teststeps = new ArrayList<io.cucumber.plugin.event.TestStep>();
			teststeps.add(new TestStep("my step"));
			ScenarioResult scnr = new ScenarioResult("test", new TestCase(0, URI.create("testcases/testCase"), "testCase", "scenario", pickletags,teststeps),new Result(Status.PASSED, Duration.ofMillis(1000L), null), LocalDateTime.now(), LocalDateTime.now());
			StepResult stpr = new StepResult("test", new TestStep("my step"), new Result(Status.PASSED, Duration.ofMillis(222L), null), LocalDateTime.now(), LocalDateTime.now());
			scnr.addChildResult(stpr);
			gr.addChildResult(scnr);
			list.add(gr);
			list.add(new GroupResult("test2", new Result(Status.PASSED, Duration.ofMillis(1000L), null), LocalDateTime.now(), LocalDateTime.now()));
			PluginFactory pf = new PluginFactory();
			PerfRuntimeOptions options = new PerfRuntimeOptions();
			Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
			plugins.addPlugin(stf);
			plugins.setEventBusOnPlugins(eventBus);
			eventBus.send(new SimulationStarted(eventBus.getTime(),eventBus.getTimeMillis(), "test"));
			for (GroupResult res : list) {
				eventBus.send(new GroupFinished(eventBus.getTime(),eventBus.getTimeMillis(),0, new PerfGroup("group", "test.feature", 1, 10, null),res));
			}
			
		} catch (CucumberException e) {
			fail("Issue");
		}
		eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(), null));
		List<GroupResult> resultList = new ArrayList<GroupResult>();
		List<Map<String, Object>> json = (List<Map<String, Object>>) getFileJSON("C:/test/log.json",resultList.getClass());
		json.remove(0);
		resultList = stf.createGroupResultList(json);
		assertEquals((long)222,(long)resultList.get(0).getChildResults().get(0).getChildResults().get(0).getResultDuration().toMillis());
		assertTrue(deleteFile("C:/test/log.json"));
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

	/**
	 * Get File Length
	 * @param filepath The file path to the file.
	 * @return True if same size else false;
	 */
	public static Object getFileJSON(String filepath,Class<?> c)
	{
		Gson gson = new Gson();
		JsonReader jsonReader;
		try {
			jsonReader = new JsonReader(new FileReader(filepath));
		    Object result = gson.fromJson(jsonReader, c);
			jsonReader.close();
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
		
	}
}
