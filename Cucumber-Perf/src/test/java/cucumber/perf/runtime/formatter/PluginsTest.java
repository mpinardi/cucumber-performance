package cucumber.perf.runtime.formatter;

import static org.junit.Assert.*;

import java.time.Clock;
import java.util.Arrays;

import org.junit.Test;

import cucumber.perf.runtime.PerfRuntimeOptions;
import cucumber.perf.runtime.TimeServiceEventBus;
import cucumber.perf.runtime.formatter.PluginFactory;
import cucumber.perf.runtime.formatter.Plugins;
import io.cucumber.core.exception.CucumberException;

public class PluginsTest {
	private TimeServiceEventBus eventBus = new TimeServiceEventBus(Clock.systemDefaultZone());

	@Test
	public void testIgnoreMinions() {
		try {
			PluginFactory pf = new PluginFactory();
			PerfRuntimeOptions options = new PerfRuntimeOptions();
			options.addPlugins(Arrays.asList(new String[] {"prcntl:40"}));
			Plugins plugins = new Plugins(this.getClass().getClassLoader(), pf, options);
			plugins.setEventBusOnPlugins(eventBus);
			for (io.cucumber.plugin.Plugin p : plugins.getPlugins())
			{
				assertFalse(PluginFactory.isMinionName(p.getClass().getName()));
			}
		} catch (CucumberException e) {
			fail("CucumberException");
		}
		assertTrue(true);
	}

}
