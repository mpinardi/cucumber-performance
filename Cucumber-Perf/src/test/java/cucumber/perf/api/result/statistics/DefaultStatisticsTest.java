package cucumber.perf.api.result.statistics;

import static org.junit.Assert.*;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import cucumber.perf.api.result.BaseResult;
import cucumber.perf.api.result.GroupResult;
import cucumber.perf.api.result.ScenarioResult;
import cucumber.perf.api.result.StepResult;
import cucumber.perf.api.result.TestCase;
import cucumber.perf.api.result.TestStep;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;

public class DefaultStatisticsTest {
	
	@Test
	public void testGetStats_Strict() {
			HashMap<String,List<GroupResult>> results = new HashMap<String,List<GroupResult>>();
			List<GroupResult> res = new ArrayList<GroupResult>();
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(20000000000L), null), LocalDateTime.parse("2007-12-12T05:20:35"),LocalDateTime.parse("2007-12-12T05:20:55")));
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(30000000000L), null), LocalDateTime.parse("2007-12-12T05:21:10"),LocalDateTime.parse("2007-12-12T05:21:40")));
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(32000000000L), null), LocalDateTime.parse("2007-12-12T05:22:01"),LocalDateTime.parse("2007-12-12T05:22:33")));
			res.add(new GroupResult("test", new Result(Status.FAILED, Duration.ofNanos(1000000000L), null), LocalDateTime.parse("2007-12-12T05:22:30"),LocalDateTime.parse("2007-12-12T05:22:31")));
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(25000000000L), null), LocalDateTime.parse("2007-12-12T05:22:40"),LocalDateTime.parse("2007-12-12T05:23:05")));
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(40000000000L), null), LocalDateTime.parse("2007-12-12T05:23:10"),LocalDateTime.parse("2007-12-12T05:23:50")));
			results.put("test",res);
			DefaultStatistics statistics = new DefaultStatistics(new BaseResult("sim",new Result(Status.PASSED,Duration.ofNanos(240000000L), null),LocalDateTime.parse("2007-12-12T05:20:00"),LocalDateTime.parse("2007-12-12T05:24:00")),results);
			Stats stats = statistics.getStats(true);
			assertEquals(2.94E10,stats.getStatistic("avg", "test"),.1);
			assertEquals(5.0,stats.getStatistic("cnt", "test"),.1);
			assertEquals(null,stats.getStatistic("fail", "test"));
	}
	
	@Test
	public void testGetStats_Strict_MultiGroup() {
			HashMap<String,List<GroupResult>> results = new HashMap<String,List<GroupResult>>();
			List<GroupResult> res = new ArrayList<GroupResult>();
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(20000000000L), null), LocalDateTime.parse("2007-12-12T05:20:35"),LocalDateTime.parse("2007-12-12T05:20:55")));
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(30000000000L), null), LocalDateTime.parse("2007-12-12T05:21:10"),LocalDateTime.parse("2007-12-12T05:21:40")));
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(32000000000L), null), LocalDateTime.parse("2007-12-12T05:22:01"),LocalDateTime.parse("2007-12-12T05:22:33")));
			res.add(new GroupResult("test", new Result(Status.FAILED, Duration.ofNanos(1000000000L), null), LocalDateTime.parse("2007-12-12T05:22:30"),LocalDateTime.parse("2007-12-12T05:22:31")));
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(25000000000L), null), LocalDateTime.parse("2007-12-12T05:22:40"),LocalDateTime.parse("2007-12-12T05:23:05")));
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(40000000000L), null), LocalDateTime.parse("2007-12-12T05:23:10"),LocalDateTime.parse("2007-12-12T05:23:50")));
			results.put("test",res);
			List<GroupResult> res2 = new ArrayList<GroupResult>();
			res2.add(new GroupResult("test2", new Result(Status.PASSED, Duration.ofNanos(20000000000L), null), LocalDateTime.parse("2007-12-12T05:20:35"),LocalDateTime.parse("2007-12-12T05:20:55")));
			res2.add(new GroupResult("test2", new Result(Status.PASSED, Duration.ofNanos(20000000000L), null), LocalDateTime.parse("2007-12-12T05:20:35"),LocalDateTime.parse("2007-12-12T05:20:55")));
			res2.add(new GroupResult("test2", new Result(Status.PASSED, Duration.ofNanos(31000000000L), null), LocalDateTime.parse("2007-12-12T05:20:54"),LocalDateTime.parse("2007-12-12T05:21:25")));
			res2.add(new GroupResult("test2", new Result(Status.PASSED, Duration.ofNanos(30000000000L), null), LocalDateTime.parse("2007-12-12T05:21:10"),LocalDateTime.parse("2007-12-12T05:21:40")));
			results.put("test2",res2);
			DefaultStatistics statistics = new DefaultStatistics(new BaseResult("sim",new Result(Status.PASSED, Duration.ofNanos(240000000L), null),LocalDateTime.parse("2007-12-12T05:20:00"),LocalDateTime.parse("2007-12-12T05:24:00")),results);
			Stats stats = statistics.getStats(true);
			assertEquals(2.94E10,stats.getStatistic("avg", "test"),.1);
			assertEquals(5.0,stats.getStatistic("cnt", "test"),.1);
			assertEquals(0.75,stats.getStatistic("cncrnt", "test"),.1);
			assertEquals(null,stats.getStatistic("fail", "test"));
			
			assertEquals(2.525E10,stats.getStatistic("avg", "test2"),.1);
			assertEquals(4.0,stats.getStatistic("cnt", "test2"),.1);
			assertEquals(1.53,stats.getStatistic("cncrnt", "test2"),.1);
			assertEquals(null,stats.getStatistic("fail", "test2"));
	}
	
	@Test
	public void testGetStats_NonStrict() {
			HashMap<String,List<GroupResult>> results = new HashMap<String,List<GroupResult>>();
			List<GroupResult> res = new ArrayList<GroupResult>();
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(20000000000L), null), LocalDateTime.parse("2007-12-12T05:20:35"),LocalDateTime.parse("2007-12-12T05:20:55")));
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(30000000000L), null), LocalDateTime.parse("2007-12-12T05:21:10"),LocalDateTime.parse("2007-12-12T05:21:40")));
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(32000000000L), null), LocalDateTime.parse("2007-12-12T05:22:01"),LocalDateTime.parse("2007-12-12T05:22:33")));
			res.add(new GroupResult("test", new Result(Status.FAILED, Duration.ofNanos(1000000000L), null), LocalDateTime.parse("2007-12-12T05:22:30"),LocalDateTime.parse("2007-12-12T05:22:31")));
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(25000000000L), null), LocalDateTime.parse("2007-12-12T05:22:40"),LocalDateTime.parse("2007-12-12T05:23:05")));
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(40000000000L), null), LocalDateTime.parse("2007-12-12T05:23:10"),LocalDateTime.parse("2007-12-12T05:23:50")));
			results.put("test",res);
			DefaultStatistics statistics = new DefaultStatistics(new BaseResult("sim",new Result(Status.PASSED,Duration.ofMillis(240000000), null),LocalDateTime.parse("2007-12-12T05:20:00"),LocalDateTime.parse("2007-12-12T05:24:00")),results);
			Stats stats = statistics.getStats(false);
			assertEquals(2.4666666666666668E10,stats.getStatistic("avg", "test"),.001);
			assertEquals(6.0,stats.getStatistic("cnt", "test"),.1);
			assertEquals(1.0,stats.getStatistic("fail", "test"),.1);
			assertEquals(5.0,stats.getStatistic("pass", "test"),.1);
	}
	
	@Test
	public void testGetStats_Concurrency() {
			HashMap<String,List<GroupResult>> results = new HashMap<String,List<GroupResult>>();
			List<GroupResult> res = new ArrayList<GroupResult>();
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(20000000000L), null), LocalDateTime.parse("2007-12-12T05:20:35"),LocalDateTime.parse("2007-12-12T05:20:55")));
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(20000000000L), null), LocalDateTime.parse("2007-12-12T05:20:35"),LocalDateTime.parse("2007-12-12T05:20:55")));
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(31000000000L), null), LocalDateTime.parse("2007-12-12T05:20:54"),LocalDateTime.parse("2007-12-12T05:21:25")));
			res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(30000000000L), null), LocalDateTime.parse("2007-12-12T05:21:10"),LocalDateTime.parse("2007-12-12T05:21:40")));
			results.put("test",res);
			DefaultStatistics statistics = new DefaultStatistics(new BaseResult("sim",new Result(Status.PASSED,Duration.ofMillis(120000000), null),LocalDateTime.parse("2007-12-12T05:20:00"),LocalDateTime.parse("2007-12-12T05:22:00")),results);
			Stats stats = statistics.getStats(true);
			assertEquals(1.53,stats.getStatistic("cncrnt", "test"),.1);
	}

	@Test
	public void testGetStatsBooleanLocalDateTimeLocalDateTime() {
		HashMap<String,List<GroupResult>> results = new HashMap<String,List<GroupResult>>();
		List<GroupResult> res = new ArrayList<GroupResult>();
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(20000000000L), null), LocalDateTime.parse("2007-12-12T05:20:35"),LocalDateTime.parse("2007-12-12T05:20:55")));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(30000000000L), null), LocalDateTime.parse("2007-12-12T05:21:10"),LocalDateTime.parse("2007-12-12T05:21:40")));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(32000000000L), null), LocalDateTime.parse("2007-12-12T05:22:01"),LocalDateTime.parse("2007-12-12T05:22:33")));
		res.add(new GroupResult("test", new Result(Status.FAILED, Duration.ofNanos(1000000000L), null), LocalDateTime.parse("2007-12-12T05:22:30"),LocalDateTime.parse("2007-12-12T05:22:31")));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(25000000000L), null), LocalDateTime.parse("2007-12-12T05:22:40"),LocalDateTime.parse("2007-12-12T05:23:05")));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(40000000000L), null), LocalDateTime.parse("2007-12-12T05:23:10"),LocalDateTime.parse("2007-12-12T05:23:50")));
		results.put("test",res);
		DefaultStatistics statistics = new DefaultStatistics(new BaseResult("sim",new Result(Status.PASSED,Duration.ofMillis(240000000), null),LocalDateTime.parse("2007-12-12T05:20:00"),LocalDateTime.parse("2007-12-12T05:24:00")),results);
		Stats stats = statistics.getStats(true,LocalDateTime.parse("2007-12-12T05:20:00"),LocalDateTime.parse("2007-12-12T05:20:59"));
		assertEquals(2.0E10,stats.getStatistic("avg", "test"),.1);
		assertEquals(1.0,stats.getStatistic("cnt", "test"),.1);
		assertEquals(null,stats.getStatistic("fail", "test"));
		stats = statistics.getStats(true,LocalDateTime.parse("2007-12-12T05:21:00"),LocalDateTime.parse("2007-12-12T05:21:59"));
		assertEquals(3.0E10,stats.getStatistic("avg", "test"),.1);
		assertEquals(1.0,stats.getStatistic("cnt", "test"),.1);
		assertEquals(null,stats.getStatistic("fail", "test"));
	}

	@Test
	public void testGetErrors() {
		HashMap<String,List<GroupResult>> results = new HashMap<String,List<GroupResult>>();
		List<GroupResult> res = new ArrayList<GroupResult>();
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(20000000000L), null), LocalDateTime.parse("2007-12-12T05:20:35"),LocalDateTime.parse("2007-12-12T05:20:55")));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(30000000000L), null), LocalDateTime.parse("2007-12-12T05:21:10"),LocalDateTime.parse("2007-12-12T05:21:40")));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(32000000000L), null), LocalDateTime.parse("2007-12-12T05:22:01"),LocalDateTime.parse("2007-12-12T05:22:33")));
		GroupResult gr = new GroupResult("test", new Result(Status.FAILED, Duration.ofNanos(1000000000L), new CucumberException("random error")), LocalDateTime.parse("2007-12-12T05:22:30"),LocalDateTime.parse("2007-12-12T05:22:31"));
		ScenarioResult sc = new ScenarioResult("scentest", new TestCase(4, URI.create("features/ScenTest.feature"), "ScenTest 1", null, null, null), new Result(Status.FAILED, Duration.ofMillis(1000), new Exception("Here is an error", new Throwable())), LocalDateTime.now(), LocalDateTime.now());
		sc.addChildResult(new StepResult("steptest", new TestStep(),new Result(Status.FAILED, Duration.ofMillis(1000), new Exception("Here is an error",new Throwable())), LocalDateTime.now(), LocalDateTime.parse("2007-12-12T05:22:31")));
		sc.addChildResult(new StepResult("steptest", new TestStep(),new Result(Status.FAILED, Duration.ofMillis(1000), new Exception("Here is 2nd error",new Throwable())), LocalDateTime.now(), LocalDateTime.parse("2007-12-12T05:22:31")));
		gr.addChildResult(sc);
		res.add(gr);
		GroupResult gr2 = new GroupResult("test", new Result(Status.FAILED, Duration.ofNanos(990000000L), new CucumberException("random error")), LocalDateTime.parse("2007-12-12T05:23:35"),LocalDateTime.parse("2007-12-12T05:23:36"));
		ScenarioResult sc2 = new ScenarioResult("scentest", new TestCase(4, URI.create("features/ScenTest.feature"), "ScenTest 1", null, null, null), new Result(Status.FAILED, Duration.ofMillis(1000), new Exception("Here is an error", new Throwable())), LocalDateTime.now(), LocalDateTime.now());
		sc2.addChildResult(new StepResult("steptest", new TestStep(),new Result(Status.FAILED, Duration.ofMillis(1000), new Exception("Here is 2nd error",new Throwable())), LocalDateTime.now(), LocalDateTime.parse("2007-12-12T05:23:36")));
		gr2.addChildResult(sc2);
		res.add(gr2);
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(25000000000L), null), LocalDateTime.parse("2007-12-12T05:22:40"),LocalDateTime.parse("2007-12-12T05:23:05")));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(40000000000L), null), LocalDateTime.parse("2007-12-12T05:23:10"),LocalDateTime.parse("2007-12-12T05:23:50")));
		results.put("test",res);
		DefaultStatistics statistics = new DefaultStatistics(new BaseResult("sim",new Result(Status.PASSED,Duration.ofNanos(240000000L), null),LocalDateTime.parse("2007-12-12T05:20:00"),LocalDateTime.parse("2007-12-12T05:24:00")),results);
		HashMap<String,HashMap<String,StepErrors>> errors = statistics.getErrors();
		try {
			assertEquals(1,errors.get("test").get("scentest").getError("steptest", "Here is an error").getCount());
			assertEquals(2,errors.get("test").get("scentest").getError("steptest", "Here is 2nd error").getCount());
			assertEquals("2007-12-12T05:22:31",errors.get("test").get("scentest").getError("steptest", "Here is 2nd error").getFirst().toString());
			assertEquals("2007-12-12T05:23:36",errors.get("test").get("scentest").getError("steptest", "Here is 2nd error").getLast().toString());
		} catch(Exception e) {
			fail("no error list created");
		}
	}
	
	@Test
	public void testDefaultStats_ValidateChildren() {
		HashMap<String,List<GroupResult>> results = new HashMap<String,List<GroupResult>>();
		List<GroupResult> res = new ArrayList<GroupResult>();
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(20000000000L), null), LocalDateTime.parse("2007-12-12T05:20:35"),LocalDateTime.parse("2007-12-12T05:20:55")));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(30000000000L), null), LocalDateTime.parse("2007-12-12T05:21:10"),LocalDateTime.parse("2007-12-12T05:21:40")));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(32000000000L), null), LocalDateTime.parse("2007-12-12T05:22:01"),LocalDateTime.parse("2007-12-12T05:22:33")));
		GroupResult gr = new GroupResult("test2", new Result(Status.PASSED, Duration.ofNanos(1000000000L), null), LocalDateTime.parse("2007-12-12T05:22:30"),LocalDateTime.parse("2007-12-12T05:22:31"));
		ScenarioResult sc = new ScenarioResult("scentest", new TestCase(4, URI.create("features/ScenTest.feature"), "ScenTest 1", null, null, null), new Result(Status.PASSED, Duration.ofMillis(2000), null), LocalDateTime.now(), LocalDateTime.now());
		sc.addChildResult(new StepResult("steptest", new TestStep(),new Result(Status.PASSED, Duration.ofMillis(1000), null), LocalDateTime.now(), LocalDateTime.parse("2007-12-12T05:22:31")));
		sc.addChildResult(new StepResult("steptest", new TestStep(),new Result(Status.PASSED, Duration.ofMillis(1000), null), LocalDateTime.now(), LocalDateTime.parse("2007-12-12T05:22:31")));
		gr.addChildResult(sc);
		res.add(gr);
		GroupResult gr2 = new GroupResult("test2", new Result(Status.PASSED, Duration.ofNanos(990000000L), null), LocalDateTime.parse("2007-12-12T05:23:35"),LocalDateTime.parse("2007-12-12T05:23:36"));
		ScenarioResult sc2 = new ScenarioResult("scentest", new TestCase(4, URI.create("features/ScenTest.feature"), "ScenTest 1", null, null, null), new Result(Status.PASSED, Duration.ofMillis(1500), null), LocalDateTime.now(), LocalDateTime.now());
		sc2.addChildResult(new StepResult("steptest", new TestStep(),new Result(Status.PASSED, Duration.ofMillis(1000), null), LocalDateTime.now(), LocalDateTime.parse("2007-12-12T05:23:36")));
		sc2.addChildResult(new StepResult("steptest", new TestStep(),new Result(Status.PASSED, Duration.ofMillis(500), null), LocalDateTime.now(), LocalDateTime.parse("2007-12-12T05:23:36")));
		gr2.addChildResult(sc2);
		res.add(gr2);
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(25000000000L), null), LocalDateTime.parse("2007-12-12T05:22:40"),LocalDateTime.parse("2007-12-12T05:23:05")));
		res.add(new GroupResult("test", new Result(Status.PASSED, Duration.ofNanos(40000000000L), null), LocalDateTime.parse("2007-12-12T05:23:10"),LocalDateTime.parse("2007-12-12T05:23:50")));
		results.put("test",res);
		DefaultStatistics statistics = new DefaultStatistics(new BaseResult("sim",new Result(Status.PASSED,Duration.ofNanos(240000000L), null),LocalDateTime.parse("2007-12-12T05:20:00"),LocalDateTime.parse("2007-12-12T05:24:00")),results);
		statistics.getStats(true);
		try {
			assertEquals(8.75E8,statistics.getStatistics().getAvg("test2", "scentest", "steptest"),.001);
			assertEquals(1.75E9,statistics.getStatistics().getAvg("test2", "scentest"),.001);
		} catch(Exception e) {
			fail("children are not being calculated correctly");
		}
	}

	@Test
	public void testGetDefaultStats() {
		Stats s = DefaultStatistics.getDefaultStats(true);
		assertEquals("avg",s.getStatisticType("avg").getKey());
		assertEquals("min",s.getStatisticType("min").getKey());
		assertEquals("max",s.getStatisticType("max").getKey());
		assertEquals("cnt",s.getStatisticType("cnt").getKey());
		assertEquals("cncrnt",s.getStatisticType("cncrnt").getKey());
		assertEquals(null,s.getStatisticType("fail"));
		
		s = DefaultStatistics.getDefaultStats(false);
		assertEquals("avg",s.getStatisticType("avg").getKey());
		assertEquals("min",s.getStatisticType("min").getKey());
		assertEquals("max",s.getStatisticType("max").getKey());
		assertEquals("cnt",s.getStatisticType("cnt").getKey());
		assertEquals("cncrnt",s.getStatisticType("cncrnt").getKey());
		assertEquals("pass",s.getStatisticType("pass").getKey());
		assertEquals("fail",s.getStatisticType("fail").getKey());
	}

}
