package cucumber.api.perf.formatter;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cucumber.api.Result;
import cucumber.api.Result.Type;
import cucumber.api.perf.result.FeatureResult;

public class StatisticsTest {

	@Test
	public void testStatistics() {
		List<FeatureResult> res = new ArrayList<FeatureResult>();
		res.add(new FeatureResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new FeatureResult("test", new Result(Type.PASSED, (long)1200, null), LocalDateTime.now(),LocalDateTime.now()));
		Statistics s = new Statistics(res, true, true);
		assertEquals((long)1200,(long)s.getMax().get("test").getResultDuration());
	}

	@Test
	public void testGetAvg() {
		List<FeatureResult> res = new ArrayList<FeatureResult>();
		res.add(new FeatureResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new FeatureResult("test", new Result(Type.PASSED, (long)1100, null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new FeatureResult("test", new Result(Type.PASSED, (long)1200, null), LocalDateTime.now(),LocalDateTime.now()));
		Statistics s = new Statistics(res, true, true);
		assertEquals((long)1100,(long)s.getAvg().get("test").getResultDuration());
	}

	@Test
	public void testGetMin() {
		List<FeatureResult> res = new ArrayList<FeatureResult>();
		res.add(new FeatureResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new FeatureResult("test", new Result(Type.PASSED, (long)1200, null), LocalDateTime.now(),LocalDateTime.now()));
		Statistics s = new Statistics(res, true, true);
		assertEquals((long)1000,(long)s.getMin().get("test").getResultDuration());
	}

	@Test
	public void testGetMax() {
		List<FeatureResult> res = new ArrayList<FeatureResult>();
		res.add(new FeatureResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(),LocalDateTime.now()));
		res.add(new FeatureResult("test", new Result(Type.PASSED, (long)1200, null), LocalDateTime.now(),LocalDateTime.now()));
		Statistics s = new Statistics(res, true, true);
		assertEquals((long)1200,(long)s.getMax().get("test").getResultDuration());
	}

}
