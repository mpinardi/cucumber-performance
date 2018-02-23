package cucumber.api.perf;

import org.junit.Assert;
import org.junit.Test;

import cucumber.api.SnippetType;

public class CucumberPerfOptionsTest {

	@Test
	public void testDryRun() {
		CucumberPerfOptions options = this.getOptions(options4.class);
		Assert.assertTrue(options.dryRun());
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
	public void testSnippets() {
		CucumberPerfOptions options = this.getOptions(options2.class);
		Assert.assertFalse(options.dryRun());
		Assert.assertTrue(options.snippets().equals(SnippetType.UNDERSCORE));
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

}
