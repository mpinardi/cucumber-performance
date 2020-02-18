package cucumber.perf.api.result.statistics;

import org.junit.Assert;
import org.junit.Test;

public class StatTest {

	@Test
	public void testConstructor_Perfix_Stat() {
		Stat stat = new Stat("pre",Stats.StatType.AVERAGE.type);
		Assert.assertEquals("pre_avg",stat.getAbbrivation());
		Assert.assertEquals("pre_avg",stat.getKey());
		Assert.assertEquals("pre_Average",stat.getFullName());
		Assert.assertEquals("pre_Avg",stat.getShortName());
		Assert.assertEquals("NANOS",stat.getDataType().name());
	}

	@Test
	public void testConstructor_Stat_Postfix() {
		Stat stat = new Stat(Stats.StatType.AVERAGE.type,"post");
		Assert.assertEquals("avg_post",stat.getAbbrivation());
		Assert.assertEquals("avg_post",stat.getKey());
		Assert.assertEquals("Average_post",stat.getFullName());
		Assert.assertEquals("Avg_post",stat.getShortName());
		Assert.assertEquals("NANOS",stat.getDataType().name());
	}

	@Test
	public void testConstructor() {
		Stat stat = new Stat("rnd","Random","Rand","rnd",Stat.StatDataType.OTHER);
		Assert.assertEquals("rnd",stat.getAbbrivation());
		Assert.assertEquals("rnd",stat.getKey());
		Assert.assertEquals("Random",stat.getFullName());
		Assert.assertEquals("Rand",stat.getShortName());
		Assert.assertEquals("OTHER",stat.getDataType().name());
	}
}
