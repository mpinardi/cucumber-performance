package cucumber.perf.formatter;

import static org.junit.Assert.*;

import java.io.File;
import java.time.LocalDateTime;

import org.junit.Test;

import cucumber.perf.api.event.StatisticsFinished;
import cucumber.perf.api.formatter.Statistics;
import cucumber.perf.runtime.PerfRuntimeOptions;
import cucumber.perf.runtime.TimeServiceEventBus;
import cucumber.perf.runtime.formatter.AppendableBuilder;
import cucumber.perf.runtime.formatter.ChartPointsFormatter;
import cucumber.perf.runtime.formatter.PluginFactory;
import cucumber.perf.runtime.formatter.Plugins;
import cucumber.runner.TimeService;
import cucumber.runtime.CucumberException;

public class AppendableBuilderTest {
	private TimeServiceEventBus eventBus = new TimeServiceEventBus(TimeService.SYSTEM);
	
	@Test
	public void testFinishReportWPrefix1() {
		try {
			ChartPointsFormatter cpf = new ChartPointsFormatter(new AppendableBuilder("file://C:/test/chartpoints|@H#1.csv"));
			PluginFactory pf = new PluginFactory();
			PerfRuntimeOptions options = new PerfRuntimeOptions();
			Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
			plugins.addPlugin(cpf);
			plugins.setEventBusOnPlugins(eventBus);
			eventBus.send(new StatisticsFinished(eventBus.getTime(),eventBus.getTimeMillis(),new Statistics()));
		} catch (CucumberException e) {
			fail("CucumberException");
		}
		
		assertTrue(deleteFile("C:/test/chartpoints"+LocalDateTime.now().getHour()+"1.csv"));
	}
	
	@Test
	public void testFinishReportWPrefix2() {
		try {
			ChartPointsFormatter cpf = new ChartPointsFormatter(new AppendableBuilder("file://C:/test/chartpoints|-#1-@yyyy.csv"));
			PluginFactory pf = new PluginFactory();
			PerfRuntimeOptions options = new PerfRuntimeOptions();
			Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
			plugins.addPlugin(cpf);
			plugins.setEventBusOnPlugins(eventBus);
			eventBus.send(new StatisticsFinished(eventBus.getTime(),eventBus.getTimeMillis(),new Statistics()));
		} catch (CucumberException e) {
			fail("CucumberException");
		}
		assertTrue(deleteFile("C:/test/chartpoints-1-"+LocalDateTime.now().getYear()+".csv"));
	}
	
	@Test
	public void testFinishReportWPrefixPaddingZeros() {
		try {
			ChartPointsFormatter cpf = new ChartPointsFormatter(new AppendableBuilder("file://C:/test/chartpoints|-#0001-@yyyy.csv"));
			PluginFactory pf = new PluginFactory();
			PerfRuntimeOptions options = new PerfRuntimeOptions();
			Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
			plugins.addPlugin(cpf);
			plugins.setEventBusOnPlugins(eventBus);
			eventBus.send(new StatisticsFinished(eventBus.getTime(),eventBus.getTimeMillis(),new Statistics()));
		} catch (CucumberException e) {
			fail("CucumberException");
		}
		assertTrue(deleteFile("C:/test/chartpoints-0001-"+LocalDateTime.now().getYear()+".csv"));
	}
	
	@Test
	public void testFinishReportWPrefixCountUp() {
		try {
			ChartPointsFormatter cpf = new ChartPointsFormatter(new AppendableBuilder("file://C:/test/chartpoints|-#0001.csv"));
			PluginFactory pf = new PluginFactory();
			PerfRuntimeOptions options = new PerfRuntimeOptions();
			Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
			plugins.addPlugin(cpf);
			plugins.setEventBusOnPlugins(eventBus);
			
			for (int i = 1; i < 12; i ++)
			{
				eventBus.send(new StatisticsFinished(eventBus.getTime(),eventBus.getTimeMillis(),new Statistics()));
				if (i < 10)
				{
					assertTrue(deleteFile("C:/test/chartpoints-000"+i+".csv"));
				}
				else
				{
					assertTrue(deleteFile("C:/test/chartpoints-00"+i+".csv"));

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
