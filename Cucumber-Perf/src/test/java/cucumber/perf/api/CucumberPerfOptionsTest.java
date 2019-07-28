package cucumber.perf.api;

import org.junit.Assert;
import org.junit.Test;

import cucumber.perf.api.CucumberPerfOptions;

public class CucumberPerfOptionsTest {

	@Test
	public void testDryRun() {
		CucumberPerfOptions options = this.getOptions(options4.class);
		Assert.assertTrue(options.dryRun());
		Assert.assertTrue(options.plans().length == 0);
	}
	
	@Test
	public void testMonochrome() {
		CucumberPerfOptions options = this.getOptions(options7.class);
		Assert.assertTrue(options.monochrome());
		Assert.assertTrue(options.plans().length == 0);
	}
	
	@Test
	public void testStrictDefault() {
		CucumberPerfOptions options = this.getOptions(options7.class);
		Assert.assertTrue(options.strict());
		Assert.assertTrue(options.plans().length == 0);
	}
	
	@Test
	public void testStrict() {
		CucumberPerfOptions options = this.getOptions(options8.class);
		Assert.assertTrue(!options.strict());
		Assert.assertTrue(options.plans().length == 0);
	}
	
	@Test
	public void testFailFast() {
		CucumberPerfOptions options = this.getOptions(options9.class);
		Assert.assertTrue(options.failfast());
		Assert.assertTrue(options.plans().length == 0);
	}

	@Test
	public void testPlans() {
		CucumberPerfOptions options = this.getOptions(options1.class);
		Assert.assertFalse(options.dryRun());
		Assert.assertTrue(!options.plans()[0].isEmpty());
	}

	@Test
	public void testTags() {
		CucumberPerfOptions options = this.getOptions(options2.class);
		Assert.assertFalse(options.dryRun());
		Assert.assertTrue(!options.tags()[0].isEmpty());
	}

	@Test
	public void testName() {
		CucumberPerfOptions options = this.getOptions(options3.class);
		Assert.assertFalse(options.dryRun());
		Assert.assertTrue(!options.name()[0].isEmpty());
	}
	
	@Test
	public void testPlugins() {
		CucumberPerfOptions options = this.getOptions(options6.class);
		Assert.assertFalse(options.dryRun());
		Assert.assertTrue(options.plugin()[0].equalsIgnoreCase("chart_points:target/chartpoints|-#0001.csv:10"));
	}
	
    private CucumberPerfOptions getOptions(Class<?> clazz) {
        return clazz.getAnnotation(CucumberPerfOptions.class);
    }
    
	@CucumberPerfOptions(
			plans = {"src/test/java/resources"})
    class options1
    {
    }
	
	@CucumberPerfOptions(
			tags = {"not @bskip","@planPosTest"})
    class options2
    {
    }
	
	@CucumberPerfOptions(
			name = {"^(?!.*period).*$"})
    class options3
    {
    }
	
	@CucumberPerfOptions(
			dryRun = true)
    class options4
    {
    }
	
	@CucumberPerfOptions(
			plans = {"src/test/java/resources"},
			tags = {"not @bskip","@planPosTest"},
			name = {"^(?!.*period).*$"},
			dryRun = true)
    class options5
    {
    }
	
	@CucumberPerfOptions(
			plugin = {"chart_points:target/chartpoints|-#0001.csv:10","ssummary_text:target/summary|-#0001.txt"
			})
    class options6
    {
    }
	
	@CucumberPerfOptions(
			monochrome = true)
    class options7
    {
    }
	
	@CucumberPerfOptions(
			strict = false)
    class options8
    {
    }

	@CucumberPerfOptions(
			failfast = true)
    class options9
    {
    }

}
