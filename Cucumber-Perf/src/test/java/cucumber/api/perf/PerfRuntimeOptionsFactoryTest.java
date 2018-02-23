package cucumber.api.perf;

import static org.junit.Assert.*;

import org.junit.Test;

import cucumber.api.perf.CucumberPerfTest.options1;

public class PerfRuntimeOptionsFactoryTest {

	@Test
	public void testCreate() {
		PerfRuntimeOptionsFactory optf = new PerfRuntimeOptionsFactory(options1.class);
		PerfRuntimeOptions opt = optf.create();
		assertEquals("not @tskip",opt.getTagFilters().get(0));
	}

}
