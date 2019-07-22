package cucumber.perf.runtime;

import static org.junit.Assert.*;

import org.junit.Test;

import cucumber.perf.runtime.CucumberPerfTest.options1;
import cucumber.perf.runtime.PerfRuntimeOptions;
import cucumber.perf.runtime.PerfRuntimeOptionsFactory;

public class PerfRuntimeOptionsFactoryTest {

	@Test
	public void testCreate() {
		PerfRuntimeOptionsFactory optf = new PerfRuntimeOptionsFactory(options1.class);
		PerfRuntimeOptions opt = optf.create();
		assertEquals("not @tskip",opt.getTagFilters().get(0));
	}
}
