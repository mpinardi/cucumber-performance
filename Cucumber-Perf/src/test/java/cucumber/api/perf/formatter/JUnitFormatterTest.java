package cucumber.api.perf.formatter;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDateTime;

import org.junit.Test;

import cucumber.api.Result;
import cucumber.api.Result.Type;
import cucumber.api.perf.result.FeatureResult;

public class JUnitFormatterTest {

	@Test
	public void testJUnitFormatter() {
		try {
			new JUnitFormatter("file://C:/test/junit.xml");
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
			JUnitFormatter junit = new JUnitFormatter("file://C:/test/junit.xml");
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
			JUnitFormatter junit = new JUnitFormatter("file://C:/test/junittest.xml");
			junit.addFeatureResult(new FeatureResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(), LocalDateTime.now()));
			junit.finishReport();
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}

		assertTrue(deleteFile("C:/test/junittest.xml"));
	}
	
	@Test
	public void testFinishReportWPrefix1() {
		try {
			JUnitFormatter junit = new JUnitFormatter("file://C:/test/junittest|#1.xml");
			junit.addFeatureResult(new FeatureResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(), LocalDateTime.now()));
			junit.finishReport();
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
		
		//TODO compare result
		assertTrue(deleteFile("C:/test/junittest1.xml"));
	}
	
	@Test
	public void testFinishReportWPrefix2() {
		try {
			JUnitFormatter junit = new JUnitFormatter("file://C:/test/junittest|-#1-@yyyy.xml");
			junit.addFeatureResult(new FeatureResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(), LocalDateTime.now()));
			junit.finishReport();
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
		
		assertTrue(deleteFile("C:/test/junittest-1-"+LocalDateTime.now().getYear()+".xml"));
	}
	
	@Test
	public void testFinishReportWPrefixPadding() {
		try {
			JUnitFormatter junit = new JUnitFormatter("file://C:/test/junittest|-#0001-@yyyy.xml");
			junit.addFeatureResult(new FeatureResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(), LocalDateTime.now()));
			junit.finishReport();
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
		
		assertTrue(deleteFile("C:/test/junittest-0001-"+LocalDateTime.now().getYear()+".xml"));
	}

	@Test
	public void testFinishReportWPrefixCountUp() {
		try {
			JUnitFormatter junit = new JUnitFormatter("file://C:/test/junittest|-#0001.xml");
			for (int i = 1; i < 12; i ++)
			{
				junit.addFeatureResult(new FeatureResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(), LocalDateTime.now()));
				junit.finishReport();
				junit.reset();
				if (i < 10)
				{
				assertTrue(deleteFile("C:/test/junittest-000"+i+".xml"));
				}
				else
				{
					assertTrue(deleteFile("C:/test/junittest-00"+i+".xml"));
				}
			}
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
	}
	
	/**
	 * Delete a file
	 * @param filepath The file path to the file.
	 * @return True if deleted else false;
	 */
	public static boolean deleteFile(String filepath)
	{
		File file = new File(filepath);
		try {
			return file.delete();
		} catch (SecurityException e) {
			return false;
		}
	}
}
