package cucumber.perf.runtime.formatter;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import cucumber.perf.api.result.statistics.Stats;
import io.cucumber.core.exception.CucumberException;

public class PercentileCreatorTest {

	@Test
	public void testPercentileCreator() {
		try {
			PercentileCreator stddev = new PercentileCreator(new String[] {"50"});
			HashMap<String, List<Long>> results = new HashMap<String, List<Long>>();
			List<Long> sorted = new ArrayList<Long>();
			sorted.add((long) 1300);
			sorted.add((long) 2000);
			sorted.add((long) 2400);
			sorted.add((long) 2300);
			sorted.add((long) 3000);
			results.put("test",sorted);
			Stats s = stddev.run(results);
			assertEquals("Failed to get proper Prctl", 2400.0d, s.getStatistic("prctl_50", "test"), 0.001);
		} catch (CucumberException e) {
			fail("CucumberException");
		}
	}
		
		
		@Test
		public void testPercentileCreatorMultiple() {
			try {
				PercentileCreator stddev = new PercentileCreator(new String[] {"50"});
				HashMap<String, List<Long>> results = new HashMap<String, List<Long>>();
				List<Long> sorted = new ArrayList<Long>();
				sorted.add((long) 1300);
				sorted.add((long) 2000);
				sorted.add((long) 2400);
				sorted.add((long) 2300);
				sorted.add((long) 3000);
				results.put("test",sorted);
				sorted = new ArrayList<Long>();
				sorted.add((long) 1300);
				sorted.add((long) 2000);
				sorted.add((long) 2400);
				sorted.add((long) 2300);
				sorted.add((long) 3000);
				results.put("test2",sorted);
				sorted = new ArrayList<Long>();
				sorted.add((long) 1300);
				sorted.add((long) 2000);
				sorted.add((long) 2400);
				sorted.add((long) 2300);
				sorted.add((long) 3000);
				results.put("test3",sorted);
				Stats s = stddev.run(results);
				assertEquals("Failed to get proper Prctl", 2400.0d, s.getStatistic("prctl_50", "test"), 0.001);
			} catch (CucumberException e) {
				fail("CucumberException");
			}

	}
}
