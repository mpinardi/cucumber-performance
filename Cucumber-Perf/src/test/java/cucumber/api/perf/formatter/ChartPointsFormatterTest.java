package cucumber.api.perf.formatter;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.HashMap;

import org.junit.Test;

import cucumber.api.Result;
import cucumber.api.Result.Type;
import cucumber.api.perf.result.FeatureResult;

public class ChartPointsFormatterTest {

	@Test
	public void testChartPointsFormatter() {
		try {
			new ChartPointsFormatter("file://C:/test/chartpoints.csv");
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
		assertTrue(true);
	}

	@Test
	public void testAddLines() {
		try {
			ChartPointsFormatter cpf = new ChartPointsFormatter("file://C:/test/chartpoints.csv");
			HashMap<String,FeatureResult> fr = new HashMap<String,FeatureResult>();
			fr.put("sum", new FeatureResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(), LocalDateTime.now()));
			cpf.addLines(fr);
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
			ChartPointsFormatter cpf = new ChartPointsFormatter("file://C:/test/chartpoints.csv");
			HashMap<String,FeatureResult> fr = new HashMap<String,FeatureResult>();
			fr.put("sum", new FeatureResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(), LocalDateTime.now()));
			cpf.addLines(fr);
			cpf.finishReport();
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}

		assertTrue(deleteFile("C:/test/chartpoints.csv"));
	}
	
	@Test
	public void testFinishReportWPrefix1() {
		try {
			ChartPointsFormatter cpf = new ChartPointsFormatter("file://C:/test/chartpoints|@H#1.csv");
			HashMap<String,FeatureResult> fr = new HashMap<String,FeatureResult>();
			fr.put("sum", new FeatureResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(), LocalDateTime.now()));
			cpf.addLines(fr);
			cpf.finishReport();
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
		
		assertTrue(deleteFile("C:/test/chartpoints"+LocalDateTime.now().getHour()+"1.csv"));
	}
	
	@Test
	public void testFinishReportWPrefix2() {
		try {
			ChartPointsFormatter cpf = new ChartPointsFormatter("file://C:/test/chartpoints|-#1-@yyyy.csv");
			HashMap<String,FeatureResult> fr = new HashMap<String,FeatureResult>();
			fr.put("sum", new FeatureResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(), LocalDateTime.now()));
			cpf.addLines(fr);
			cpf.finishReport();
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
		
		assertTrue(deleteFile("C:/test/chartpoints-1-"+LocalDateTime.now().getYear()+".csv"));
	}
	
	@Test
	public void testFinishReportWPrefixPaddingZeros() {
		try {
			ChartPointsFormatter cpf = new ChartPointsFormatter("file://C:/test/chartpoints|-#0001-@yyyy.csv");
			HashMap<String,FeatureResult> fr = new HashMap<String,FeatureResult>();
			fr.put("sum", new FeatureResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(), LocalDateTime.now()));
			cpf.addLines(fr);
			cpf.finishReport();
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
		
		assertTrue(deleteFile("C:/test/chartpoints-0001-"+LocalDateTime.now().getYear()+".csv"));
	}
	
	@Test
	public void testFinishReportWPrefixCountUp() {
		try {
			ChartPointsFormatter cpf = new ChartPointsFormatter("file://C:/test/chartpoints|-#0001.csv");
			for (int i = 1; i < 12; i ++)
			{
				HashMap<String,FeatureResult> fr = new HashMap<String,FeatureResult>();
				fr.put("sum", new FeatureResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(), LocalDateTime.now()));
				cpf.addLines(fr);
				cpf.finishReport();
				cpf.reset();
				if (i < 10)
				{
					assertTrue(deleteFile("C:/test/chartpoints-000"+i+".csv"));
				}
				else
				{
					assertTrue(deleteFile("C:/test/chartpoints-00"+i+".csv"));

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
