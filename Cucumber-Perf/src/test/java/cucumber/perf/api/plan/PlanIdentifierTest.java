package cucumber.perf.api.plan;

import static org.junit.Assert.*;

import java.net.URI;
import java.nio.file.Paths;

import org.junit.Test;


public class PlanIdentifierTest {

	@Test
    public void testParseString() {
		assertTrue(PlanIdentifier.parse("baba.plan").toString().contains("/Cucumber-Perf/baba.plan"));     
    }
	
	@Test
	public void testParseClassPath() {
		URI uri = PlanIdentifier.parse("classpath:/path/to/file.plan");
		assertEquals(uri.getScheme(), "classpath");
		assertEquals(uri.getSchemeSpecificPart(), "/path/to/file.plan");
	}


	@Test
    public void testParseURI() {
		assertEquals("baba.plan",PlanIdentifier.parse(URI.create("baba.plan")).toString());
		try {
			PlanIdentifier.parse(URI.create("baba.p"));
			fail("Did not fail bogus plan URI");
		} catch (Exception e) {
			
		}
    }

	@Test
    public void testIsPlanURI() {
		assertEquals(true,PlanIdentifier.isPlan(URI.create("file:///baba.plan")));
		assertEquals(false,PlanIdentifier.isPlan(URI.create("file:///baba.p")));
    }

	@Test
    public void testIsPlanPath() {
		assertEquals(true,PlanIdentifier.isPlan(Paths.get(URI.create("file:///baba.plan"))));
		assertEquals(false,PlanIdentifier.isPlan(Paths.get(URI.create("file:///baba.p"))));
    }


}