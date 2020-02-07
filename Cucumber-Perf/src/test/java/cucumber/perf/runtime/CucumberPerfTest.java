package cucumber.perf.runtime;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.Test;

import cucumber.perf.api.CucumberOptions;
import cucumber.perf.api.CucumberPerfOptions;
import cucumber.perf.runtime.CucumberPerf;
import cucumber.perf.runtime.PerfRuntimeOptions;
import io.cucumber.plugin.Plugin;

public class CucumberPerfTest {

	@Test
	public void testCucumberPerfClassOfQ() {
		CucumberPerf cp = new CucumberPerf(options1.class);
		try {
			cp.runThreads();
		} catch (Throwable e) {
			fail("Error:"+e.getMessage());
		}
		assertEquals(cp.getMaxThreads(),3);
	}
	
	@Test
	public void testCucumberPerfClassOfQWithNull() {
		CucumberPerf cp = new CucumberPerf(options3.class);
		try {
			cp.runThreads();
		} catch (Throwable e) {
			fail("Error:"+e.getMessage());
		}
		assertEquals(cp.getMaxThreads(),3);
	}
	
	@Test
	public void testCucumberPerfClassOfQWithPlugins() {
		new CucumberPerf(options4.class);
	}

	@Test
	public void testCucumberPerfClassOfQPerfRuntimeOptions() {
		PerfRuntimeOptions options = new PerfRuntimeOptions();
		options.addTagFilters(Arrays.asList(new String[]{"not @tskip"}));
		//options.addNameFilters(Arrays.asList(new String[]{"^(?!.*period).*$"}));
		options.addPlanPaths(Arrays.asList(new String[]{"src/test/java/resources"}));
		CucumberPerf cp = new CucumberPerf(options2.class,options);
		try {
			cp.runThreads();
		} catch (Throwable e) {
			fail("Error:"+e.getMessage());
		}
		assertEquals(cp.getMaxThreads(),3);
	}

	@Test
	public void testCucumberPerfPerfRuntimeOptions() {
		PerfRuntimeOptions options = new PerfRuntimeOptions();
		options.addTagFilters(Arrays.asList(new String[]{"not @tskip"}));
		//options.addNameFilters(Arrays.asList(new String[]{"^(?!.*period).*$"}));
		//options.addPlugins(Arrays.asList(new String[]{"pretty_display"}));
		options.addPlanPaths(Arrays.asList(new String[]{"src/test/java/resources"}));
		options.addCucumberOptions(Arrays.asList(new String[]{"--dry-run","-g","steps","src/test/java/resources"}));
		//Originally had a null plugin "--plugin null"
		CucumberPerf cp = new CucumberPerf(options);
		try {
			cp.runThreads();
		} catch (Throwable e) {
			fail("Error:"+e.getMessage());
		}
		assertEquals(cp.getMaxThreads(),3);
	}
	
	@Test
	public void testCucumberPerfPerfRuntimeOptionsVerifyNoPlugins() {
		PerfRuntimeOptions options = new PerfRuntimeOptions();
		options.addTagFilters(Arrays.asList(new String[]{"not @tskip"}));
		//options.addNameFilters(Arrays.asList(new String[]{"^(?!.*period).*$"}));
		options.addPlugins(Arrays.asList(new String[]{"detail_display"}));
		options.addPlanPaths(Arrays.asList(new String[]{"src/test/java/resources"}));
		options.addCucumberOptions(Arrays.asList(new String[]{"--dry-run","-g","steps","src/test/java/resources"}));
		CucumberPerf cp = new CucumberPerf(options);
		try {
			cp.runThreads();
			for (Plugin p : cp.getPlugins().getPlugins())
			{
				if (p.getClass().getSimpleName().equalsIgnoreCase("PrettyDisplayPrinter"))
				{
					fail("Did not delete printer");
				}
			}
		} catch (Throwable e) {
			fail("Error:"+e.getMessage());
		}
		assertEquals(cp.getMaxThreads(),3);
	}

	@Test
	public void testCucumberPerfStringArray() {
		String[] args = new String[] {"plans=src/test/java/resources","tags=not @tskip","--dry-run", "-g","steps","src/test/java/resources"};
		CucumberPerf cp = new CucumberPerf(args);
		try {
			cp.runThreads();
		} catch (Throwable e) {
			fail("Error:"+e.getMessage());
		}
		assertEquals(3,cp.getMaxThreads());
	}

	@Test
	public void testRunThreads() {
		CucumberPerf cp = new CucumberPerf(options1.class);
		try {
			cp.runThreads();
		} catch (Throwable e) {
			fail("Error:"+e.getMessage());
		}
		assertTrue(true);
	}

	@Test
	public void testConvertRuntime() {
		CucumberPerf cp = new CucumberPerf(options1.class);
		Duration rt = cp.convertRuntime("00:00:30");
		assertEquals(rt.toString(),"PT30S");
	}

	@Test
	public void testGetEndLocalDateTimeString() {
		CucumberPerf cp = new CucumberPerf(options1.class);
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime rt = cp.getEnd(now, "00:00:30");
		assertEquals(rt,LocalDateTime.from(now).plus(cp.convertRuntime("00:00:30")));
	}

	@Test
	public void testGetMaxRan() {
		CucumberPerf cp = new CucumberPerf(options1.class);
		try {
			cp.runThreads();
		} catch (Throwable e) {
			fail("Error:"+e.getMessage());
		}
		assertEquals(cp.getMaxRan(),6);
	}

	@Test
	public void testGetTotalRanCount() {
		CucumberPerf cp = new CucumberPerf(options0.class);
		try {
			cp.runThreads();
		} catch (Throwable e) {
			fail("Error:"+e.getMessage());
		}
		assertEquals(cp.getTotalRanCount(),6);
	}

	@Test
	public void testGetStart() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetEnd() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetRunTime() {
		//fail("Not yet implemented");
	}
	
	@Test
	public void testGetMaxThreads() {
		CucumberPerf cp = new CucumberPerf(options1.class);
		try {
			cp.runThreads();
		} catch (Throwable e) {
			fail("Error:"+e.getMessage());
		}
		assertEquals(cp.getMaxThreads(),3);
	}

	
	@CucumberPerfOptions(
			plans = {"src/test/java/resources"},
			tags = {"not @tskip"})
	@CucumberOptions(
			features = {"src/test/java/resources"},
			dryRun = true)
	public class options0
    {
    }
	
	@CucumberPerfOptions(
			plans = {"src/test/java/resources"},
			tags = {"not @tskip"},
			dryRun = true)
	@CucumberOptions(
			features = {"src/test/java/resources"},
			dryRun = true)
	public class options1
    {
    }
	
	@CucumberPerfOptions(
			plans = {"src/test/java/resources"},
			tags = {"not @tskip"},
			plugin = {"null_display"},
			dryRun = true)
	@CucumberOptions(
			features = {"src/test/java/resources"},
			dryRun = true)
    class options3
    {
    }


	@CucumberOptions(
			features = {"src/test/java/resources"},
			dryRun = true)
    class options2
    {
    }
	
	@CucumberPerfOptions(
			plans = {"src/test/java/resources"},
			tags = {"not @tskip"},
			plugin = {"chart_points:target/chartpoints|-#0001.csv:10","summary_text:target/summary|-#0001.txt"},
			dryRun = true)
	@CucumberOptions(
			features = {"src/test/java/resources"},
			dryRun = true)
    class options4
    {
    }

}
