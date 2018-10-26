package cucumber.api.perf.formatter;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cucumber.api.Result;
import cucumber.api.Result.Type;
import cucumber.api.perf.result.FeatureResult;
import cucumber.api.perf.result.ScenarioResult;

public class SummaryTextFormatterTest {

	@Test
	public void testSummaryTextFormatter() {
		try {
			new SummaryTextFormatter("file://C:/test/summary.txt");
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
		assertTrue(true);
	}

	@Test
	public void testProcess() {
		try {
			SummaryTextFormatter stf = new SummaryTextFormatter("file://C:/test/summarytext.txt");
			 List<FeatureResult> list = new ArrayList<FeatureResult>();
			list.add(new FeatureResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(), LocalDateTime.now()));
			Statistics s = new Statistics(list,false);
			stf.process(s);
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
		assertTrue(deleteFile("C:/test/summarytext.txt"));
	}
	
	@Test
	public void testCreateLines() {
		try {
			SummaryTextFormatter stf = new SummaryTextFormatter("file://C:/test/summarytext.txt");
			 List<FeatureResult> list = new ArrayList<FeatureResult>();
			list.add(new FeatureResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(), LocalDateTime.now()));
			Statistics s = new Statistics(list,false);
			stf.createLines(s);
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
		assertTrue(true);
	}


	
	@Test
	public void testReset() {
		try {
			SummaryTextFormatter stf = new SummaryTextFormatter("file://C:/test/summarytext.txt");
			 List<FeatureResult> list = new ArrayList<FeatureResult>();
			list.add(new FeatureResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(), LocalDateTime.now()));
			Statistics s = new Statistics(list,false);
			stf.createLines(s);
			stf.reset();
			stf.finishReport();
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
		assertTrue(isFileSize("C:/test/summarytext.txt",0));
	}
	
	@Test
	public void testFinishReport() {
		try {
			SummaryTextFormatter stf = new SummaryTextFormatter("file://C:/test/summarytext.txt");
			 List<FeatureResult> list = new ArrayList<FeatureResult>();
			list.add(new FeatureResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(), LocalDateTime.now()));
			Statistics s = new Statistics(list,false);
			stf.createLines(s);
			stf.finishReport();
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
		assertTrue(isFileSize("C:/test/summarytext.txt",56));
	}
	
	@Test
	public void testFinishReportWPrefix1() {
		try {
			SummaryTextFormatter stf = new SummaryTextFormatter("file://C:/test/summarytext|@H#1.txt");
			 List<FeatureResult> list = new ArrayList<FeatureResult>();
			list.add(new FeatureResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(), LocalDateTime.now()));
			Statistics s = new Statistics(list,false);
			stf.createLines(s);
			stf.finishReport();
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
		
		assertTrue(deleteFile("C:/test/summarytext"+LocalDateTime.now().getHour()+"1.txt"));
	}
	
	@Test
	public void testFinishReportWPrefix2() {
		try {
			SummaryTextFormatter stf = new SummaryTextFormatter("file://C:/test/summarytext|-#1-@yyyy.txt");
			 List<FeatureResult> list = new ArrayList<FeatureResult>();
			list.add(new FeatureResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(), LocalDateTime.now()));
			Statistics s = new Statistics(list,false);
			stf.createLines(s);
			stf.finishReport();
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
		
		assertTrue(deleteFile("C:/test/summarytext-1-"+LocalDateTime.now().getYear()+".txt"));
	}
	
	@Test
	public void testFinishReportWPrefixPaddingZeros() {
		try {
			SummaryTextFormatter stf = new SummaryTextFormatter("file://C:/test/summarytext|-#0001-@yyyy.txt");
			 List<FeatureResult> list = new ArrayList<FeatureResult>();
			list.add(new FeatureResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(), LocalDateTime.now()));
			Statistics s = new Statistics(list,false);
			stf.createLines(s);
			stf.finishReport();
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
		
		assertTrue(deleteFile("C:/test/summarytext-0001-"+LocalDateTime.now().getYear()+".txt"));
	}
	
	@Test
	public void testFinishReportWPrefixCountUp() {
		try {
			SummaryTextFormatter stf = new SummaryTextFormatter("file://C:/test/summarytext|-#0001.txt");
			List<FeatureResult> list = new ArrayList<FeatureResult>();
			for (int i = 1; i < 12; i ++)
			{
				list.add(new FeatureResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(), LocalDateTime.now()));
				Statistics s = new Statistics(list,false);
				stf.createLines(s);
				stf.finishReport();
				stf.reset();
				if (i < 10)
				{
					assertTrue(deleteFile("C:/test/summarytext-000"+i+".txt"));
				}
				else
				{
					assertTrue(deleteFile("C:/test/summarytext-00"+i+".txt"));

				}
			}
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
		
	}
	
	
	@Test
	public void testFinishReportErrors() {
		try {
			SummaryTextFormatter stf = new SummaryTextFormatter("file://C:/test/summarytext.txt");
			 List<FeatureResult> list = new ArrayList<FeatureResult>();
			list.add(new FeatureResult("test", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(), LocalDateTime.now()));
			list.add(new FeatureResult("test2", new Result(Type.PASSED, (long)1000, null), LocalDateTime.now(), LocalDateTime.now()));
			Throwable error =  new Throwable();
			error.setStackTrace(new StackTraceElement[] {new StackTraceElement("src.main.test.test","TestIt","testing.class",1),new StackTraceElement("src.main.test.test","TestIt","testing.class",2)});
			FeatureResult fres = new FeatureResult("test", new Result(Type.FAILED, (long)1000, new Exception("Here is an error",error)), LocalDateTime.now(), LocalDateTime.now());
			fres.addChildResult(new ScenarioResult("scentest", "test.scentest", new Result(Type.FAILED, (long)1000, new Exception("Here is an error",error)), LocalDateTime.now(), LocalDateTime.now()));
			list.add(fres);
			Statistics s = new Statistics(list,false);
			stf.createLines(s);
			stf.finishReport();
		} catch (MalformedURLException e) {
			fail("MalformedURL");
		} catch (IOException e) {
			fail("IOException");
		}
		//2074
		//2911
		//size is different if running in build
		long fs = getFileSize("C:/test/summarytext.txt");
		if (fs > 2074)
		{
		assertEquals(2911,fs);
		}
		else
		{
			assertEquals(2074,fs);
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
	
	/**
	 * Get File Length
	 * @param filepath The file path to the file.
	 * @param size The size to compare to.
	 * @return True if same size else false;
	 */
	public static boolean isFileSize(String filepath,long size)
	{
		File file = new File(filepath);
		try {
			long l = file.length();
			return l==size;
		} catch (SecurityException e) {
			return false;
		}
	}
	
	/**
	 * Get File Length
	 * @param filepath The file path to the file.
	 * @return True if same size else false;
	 */
	public static long getFileSize(String filepath)
	{
		File file = new File(filepath);
		try {
			long l = file.length();
			return l;
		} catch (SecurityException e) {
			return -1;
		}
	}

}
