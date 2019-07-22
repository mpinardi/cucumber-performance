package cucumber.perf.runtime;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import cucumber.perf.runtime.PerfRuntimeOptions;

public class PerfRuntimeOptionsTest {

	@Test
	public void testPerfRuntimeOptionsListOfString() {
		PerfRuntimeOptions pro = new PerfRuntimeOptions(Arrays.asList(new String[] {"plans=src/test/java/resources","-p pretty","-g steps","src/test/java/resources"}));
		assertEquals("src/test/java/resources",pro.getPlanPaths().get(0));
	}

	@Test
	public void testAddCucumberOptions() {
		PerfRuntimeOptions pro = new PerfRuntimeOptions();
		pro.addCucumberOptions(Arrays.asList(new String[] {"-p pretty", "-g steps","src/test/java/resources"}));
		assertEquals("-g",pro.getCucumberOptions().get(0));
	}
	
	@Test
	public void testSetDryRun() {
		PerfRuntimeOptions pro = new PerfRuntimeOptions(Arrays.asList(new String[] {"plans=src/test/java/resources","-p pretty","-g steps","src/test/java/resources","dryrun"}));
		assertTrue(pro.isDryRun());
	}
	
	@Test
	public void testSetMonoChrome() {
		PerfRuntimeOptions pro = new PerfRuntimeOptions(Arrays.asList(new String[] {"plans=src/test/java/resources","-p pretty","-g steps","src/test/java/resources","monochrome"}));		
		assertTrue(pro.isMonochrome());
	}
	
	@Test
	public void testSetFailFast() {
		PerfRuntimeOptions pro = new PerfRuntimeOptions(Arrays.asList(new String[] {"plans=src/test/java/resources","-p pretty","-g steps","src/test/java/resources","failfast"}));		
		assertTrue(pro.isFailFast());
	}
	
	@Test
	public void testSetStrict() {
		PerfRuntimeOptions pro = new PerfRuntimeOptions(Arrays.asList(new String[] {"plans=src/test/java/resources","-p pretty","-g steps","src/test/java/resources","no-strict"}));		
		assertFalse(pro.isStrict());
	}

}
