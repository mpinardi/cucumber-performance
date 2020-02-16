package cucumber.perf.api.result.statistics;

import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.junit.Test;

public class StatsTest {

	@Test
	public void testPutAndGet_StatisticType() {
		Stats stats = new Stats();
		stats.putStatisticType(Stats.StatType.COUNT.type);
		assertEquals("cnt",stats.getStatisticType("cnt").getKey());
	}

	@Test
	public void testGetStatisticTypes() {
		Stats stats = new Stats();
		stats.putStatisticType(Stats.StatType.COUNT.type);
		assertEquals("cnt",stats.getStatisticTypes().toArray()[0]);
	}

	@Test
	public void testGetStatisticKeys() {
		Stats stats = new Stats();
		stats.putStatistic("avg", 1.0,"grp1");
		stats.putStatistic("cnt", 2.0,"grp2");
		String[] types = new String[] {"grp1","grp2"};
		for (String key : stats.getStatisticKeys())
		{
			boolean found = false;
			for (String type : types) {
				if (type.equalsIgnoreCase(key))
					found = true;
			}
			assertTrue(found);
		}
	}

	@Test
	public void testGetStatistics() {
		Stats stats = new Stats();
		stats.putStatistic("avg", 1.0,"grp","scn","stp");
		stats.putStatistic("cnt", 2.0,"grp","scn","stp");
		LinkedHashMap<String,Double> result =stats.getStatistics("grp", "scn", "stp");
		int i = 0;
		for (Entry<String, Double> set : result.entrySet()) {
			if (i==0)
				assertEquals("avg",set.getKey());
			else
				assertEquals("cnt",set.getKey());
			i++;
		}
		
	}

	@Test
	public void testGetStatistic() {
		Stats stats = new Stats();
		stats.putStatistic("avg", 1.0,"grp","scn","stp");
		stats.putStatistic("cnt", 2.0,"grp","scn","stp");
		assertEquals(1.0,stats.getStatistic("avg","grp","scn","stp"),0.1);
	}

	@Test
	public void testPutKey() {
		Stats stats = new Stats();
		stats.putStatisticType(Stats.StatType.PASSED.type);
		stats.putStatisticType(Stats.StatType.FAILED.type);
		stats.putKey("grp","scn");
		LinkedHashMap<String,Double> result =stats.getStatistics("grp", "scn");
		int i = 0;
		for (Entry<String, Double> set : result.entrySet()) {
			if (i==0)
				assertEquals("pass",set.getKey());
			else
				assertEquals("fail",set.getKey());
			i++;
		}
	}

	@Test
	public void testAddStatistics() {
		Stats fstats = new Stats();
		fstats.putStatisticType(Stats.StatType.PASSED.type);
		fstats.putStatisticType(Stats.StatType.FAILED.type);
		fstats.putKey("grp","scn");
		fstats.putKey("grp");
		Stats stats = new Stats();
		stats.addStatistics(fstats);
		LinkedHashMap<String,Double> result =stats.getStatistics("grp", "scn");
		int i = 0;
		for (Entry<String, Double> set : result.entrySet()) {
			if (i==0)
				assertEquals("pass",set.getKey());
			else
				assertEquals("fail",set.getKey());
			i++;
		}
		LinkedHashMap<String,Double> result2 =stats.getStatistics("grp");
		i = 0;
		for (Entry<String, Double> set : result2.entrySet()) {
			if (i==0)
				assertEquals("pass",set.getKey());
			else
				assertEquals("fail",set.getKey());
			i++;
		}
	}

	@Test
	public void testIsEmpty() {
		Stats stats = new Stats();
		stats.putStatisticType(Stats.StatType.PASSED.type);
		stats.putStatisticType(Stats.StatType.FAILED.type);
		stats.putKey("grp","scn");
		assertFalse(stats.isEmpty());
	}
}
