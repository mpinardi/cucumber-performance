package cucumber.perf.runtime.filter;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import cucumber.perf.api.FeatureBuilder;
import cucumber.perf.runtime.PerfRuntimeOptions;
import cucumber.perf.runtime.filter.FeatureFilter;
import io.cucumber.core.gherkin.Feature;

public class FeatureFilterTest {
	
	@Test
	public void filterTagReduceScenarioTest() {
		PerfRuntimeOptions options = new PerfRuntimeOptions(Arrays.asList(new String[] {"-g steps","src/test/java/resources"}));
		List<Feature>  features = FeatureBuilder.getFeatures(FeatureBuilder.createRuntimeOptions(options.getCucumberOptions()));
		FeatureFilter filter = new FeatureFilter(features);
		List<Feature> ffs = filter.filter("@onlyfilter1");
		assertEquals(ffs.get(0).getPickles().size(),1);
	}
	
	@Test
	public void filterTagTest() {
		PerfRuntimeOptions options = new PerfRuntimeOptions(Arrays.asList(new String[] {"-g steps","src/test/java/resources"}));
		List<Feature>  features = FeatureBuilder.getFeatures(FeatureBuilder.createRuntimeOptions(options.getCucumberOptions()));
		FeatureFilter filter = new FeatureFilter(features);
		List<Feature> ffs = filter.filter("@onlyfilter");
		assertEquals(ffs.get(0).getPickles().size(),2);
	}
	
	@Test
	public void filterMultiTagTest() {
		PerfRuntimeOptions options = new PerfRuntimeOptions(Arrays.asList(new String[] {"-g steps","src/test/java/resources"}));
		List<Feature>  features = FeatureBuilder.getFeatures(FeatureBuilder.createRuntimeOptions(options.getCucumberOptions()));
		FeatureFilter filter = new FeatureFilter(features);
		List<Feature> ffs = filter.filter("@onlyfilter or @only");
		assertEquals(ffs.size(),2);
		assertEquals(ffs.get(0).getPickles().size(),2);
		assertEquals(ffs.get(1).getPickles().size(),2);
	}
	
	
	@Test
	public void filterNameTest() {
		PerfRuntimeOptions options = new PerfRuntimeOptions(Arrays.asList(new String[] {"-g steps","src/test/java/resources"}));
		List<Feature>  features = FeatureBuilder.getFeatures(FeatureBuilder.createRuntimeOptions(options.getCucumberOptions()));
		FeatureFilter filter = new FeatureFilter(features);
		List<Feature> ffs = filter.filter("test.feature");
		assertEquals(ffs.get(0).getName(),"test");
		assertEquals(ffs.get(0).getPickles().size(),4);
	}
	
	@Test
	public void isMatchTrueTest() {
		PerfRuntimeOptions options = new PerfRuntimeOptions(Arrays.asList(new String[] {"-g steps","src/test/java/resources"}));
		List<Feature> features = FeatureBuilder.getFeatures(FeatureBuilder.createRuntimeOptions(options.getCucumberOptions()));
		assertTrue(FeatureFilter.isMatch(features.get(1), "@onlyfilter"));
	}
	
	@Test
	public void isMatchFalseTest() {
		PerfRuntimeOptions options = new PerfRuntimeOptions(Arrays.asList(new String[] {"-g steps","src/test/java/resources"}));
		List<Feature> features = FeatureBuilder.getFeatures(FeatureBuilder.createRuntimeOptions(options.getCucumberOptions()));
		assertFalse(FeatureFilter.isMatch(features.get(0), "@notefilter"));
	}
	
	@Test
	public void getGroupTagsTest() {
		assertTrue(FeatureFilter.getGroupTags("@onlyfilter,@only").size()==2);
	}

}
