package cucumber.perf.formatter;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import cucumber.perf.api.event.ConfigStatistics;
import cucumber.perf.api.event.EventHandler;
import cucumber.perf.api.event.PerfRunStarted;
import cucumber.perf.api.event.StatisticsFinished;
import cucumber.perf.api.formatter.Statistics;
import cucumber.perf.runtime.PerfRuntimeOptions;
import cucumber.perf.runtime.TimeServiceEventBus;
import cucumber.perf.runtime.formatter.AppendableBuilder;
import cucumber.perf.runtime.formatter.ChartPointsFormatter;
import cucumber.perf.runtime.formatter.PluginFactory;
import cucumber.perf.runtime.formatter.Plugins;
import cucumber.perf.runtime.formatter.StatisticsFormatter;
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
			eventBus.registerHandlerFor(ConfigStatistics.class, statsEventhandler);
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

		assertTrue(deleteFile("C:/test/chartpoints.csv"));
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
	
	private EventHandler<ConfigStatistics> statsEventhandler = new EventHandler<ConfigStatistics>() {
        @Override
        public void receive(ConfigStatistics event) {
            assertTrue(event.setting.equalsIgnoreCase(StatisticsFormatter.CONFIG_MAXPOINTS));
        }
 };

}
