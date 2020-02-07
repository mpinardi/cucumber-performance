package cucumber.perf.formatter;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.Clock;

import org.junit.Test;

import cucumber.perf.api.event.StatisticsFinished;
import cucumber.perf.api.formatter.Statistics;
import cucumber.perf.runtime.PerfRuntimeOptions;
import cucumber.perf.runtime.TimeServiceEventBus;
import cucumber.perf.runtime.formatter.AppendableBuilder;
import cucumber.perf.runtime.formatter.JUnitFormatter;
import cucumber.perf.runtime.formatter.PluginFactory;
import cucumber.perf.runtime.formatter.Plugins;

public class JUnitFormatterTest {
	private TimeServiceEventBus eventBus = new TimeServiceEventBus(Clock.systemDefaultZone());

	@Test
	public void testJUnitFormatter() {
		try {
			new JUnitFormatter(new AppendableBuilder("C:/test/junit.xml"));
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
		assertTrue(true);
	}

	@Test
	public void testFinishReport() {
		try {
			JUnitFormatter junit = new JUnitFormatter(new AppendableBuilder("C:/test/junittest.xml"));
			//junit.addFeatureResult(new GroupResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(), LocalDateTime.now()));
			PluginFactory pf = new PluginFactory();
			PerfRuntimeOptions options = new PerfRuntimeOptions();
			Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
			plugins.addPlugin(junit);
			plugins.setEventBusOnPlugins(eventBus);
			eventBus.send(new StatisticsFinished(eventBus.getTime(),eventBus.getTimeMillis(),new Statistics()));
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}

		assertTrue(deleteFile("C:/test/junittest.xml"));
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
