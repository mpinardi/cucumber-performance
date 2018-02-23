package cucumber.api.perf;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

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

}
