package cucumber.perf.runtime.formatter;

import static org.junit.Assert.*;


import java.util.Arrays;

import org.junit.Test;

import cucumber.perf.runtime.PerfRuntimeOptions;
import cucumber.perf.runtime.TimeServiceEventBus;
import cucumber.perf.runtime.formatter.PluginFactory;
import cucumber.perf.runtime.formatter.Plugins;
import cucumber.runner.TimeService;
import cucumber.runtime.CucumberException;

public class PluginsTest {
	private TimeServiceEventBus eventBus = new TimeServiceEventBus(TimeService.SYSTEM);

	@Test
	public void testIgnoreMinions() {
		try {
			PluginFactory pf = new PluginFactory();
			PerfRuntimeOptions options = new PerfRuntimeOptions();
			options.addPlugins(Arrays.asList(new String[] {"prcntl:40"}));
			Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
			plugins.setEventBusOnPlugins(eventBus);
			for (cucumber.api.Plugin p : plugins.getPlugins())
			{
				assertFalse(PluginFactory.isMinionName(p.getClass().getName()));
			}
		} catch (CucumberException e) {
			fail("CucumberException");
		}
		assertTrue(true);
	}

}
