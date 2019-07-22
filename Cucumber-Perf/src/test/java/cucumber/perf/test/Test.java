package cucumber.perf.test;

import cucumber.api.CucumberOptions;
import cucumber.perf.api.CucumberPerfOptions;
import cucumber.perf.runtime.CucumberPerf;
import cucumber.perf.runtime.PerfRuntimeOptions;

import java.util.Arrays;


@CucumberOptions(
		// format = {"Pretty","json:target/cucumber.json"},"pretty",
		plugin = {"cucumber.formatter.NullFormatter","null_summary"},
		features = {"src/test/java/resources"},
		glue = { "steps" })
//		tags = {"*"},
//, glue = { "dhis.datim.app.test.steps" }

@CucumberPerfOptions(
		plans = {"classpath:resources"},
		tags = {"not @bskip","@planPosTest"},
		plugin = {"detail_display","chart_points:file://C:/test/chartpoints|-#0001.csv"},
		name = {"^(?!.*period).*$"})
public class Test {
	
	public static void main(String[] args)
	{
		//Option 1 pass in arguments.
		// You can freely mix cucumber and cucumber perf options.
		args = new String[] {"plans=src/test/java/resources","-p pretty -g steps src/test/java/resources"};
		//CucumberPerf cukePerf = new CucumberPerf(args);
		
		//Option 2 pass in class.
		// This class can contain both cucumber and cucumber perf annotations.
		CucumberPerf cukePerf = new CucumberPerf(Test.class);
		
		//Option 3 pass in class and runtime options
		// The class must contain cucumber runtime options
		PerfRuntimeOptions options = new PerfRuntimeOptions();
		options.addTagFilters(Arrays.asList(new String[]{"not @bskip","@simperiodtest"}));
		//options.addNameFilters(Arrays.asList(new String[]{"^(?!.*period).*$"}));
		options.addPlanPaths(Arrays.asList(new String[]{"src/test/java/resources"}));
		options.addPlugins(Arrays.asList(new String[]{"chart_points:file://C:/test/chartpoints|-@dd-#1-@HHmmss-@yyyy.csv"}));
		//CucumberPerf cukePerf = new CucumberPerf(Test.class, options);
		
		
		//Option 4 pass in runtime options
		// options must contain both cucumber and perf options
		options.addCucumberOptions(Arrays.asList(new String[]{"-g","steps","-t","@only2","src/test/java/resources",}));//"--plugin","default_summary","--plugin","null_summary","--plugin","cucumber.formatter.NullFormatter"
		//CucumberPerf cukePerf = new CucumberPerf(options);
		try {
			cukePerf.runThreads();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Total Ran: "+cukePerf.getTotalRanCount());
		System.out.println("RunTime: "+cukePerf.getRunTime());
		System.exit(0);
	}
	
}