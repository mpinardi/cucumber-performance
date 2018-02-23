package cucumber.api.perf.formatter;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;

import org.junit.Test;

import cucumber.api.Result;
import cucumber.api.Result.Type;
import cucumber.api.perf.result.FeatureResult;

public class JUnitFormatterTest {

	@Test
	public void testJUnitFormatter() {
		try {
			new JUnitFormatter(new URL("file://C:/test/junit.xml"));
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
		assertTrue(true);
	}

	@Test
	public void testAddFeatureResult() {
		try {
			JUnitFormatter junit = new JUnitFormatter(new URL("file://C:/test/junit.xml"));
			junit.addFeatureResult(new FeatureResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(), LocalDateTime.now()));
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
		assertTrue(true);
	}

	@Test
	public void testFinishReport() {
		try {
			JUnitFormatter junit = new JUnitFormatter(new URL("file://C:/test/junittest.xml"));
			junit.addFeatureResult(new FeatureResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(), LocalDateTime.now()));
			junit.finishReport();
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
		
		//TODO compare result
		assertTrue(true);
	}
}
