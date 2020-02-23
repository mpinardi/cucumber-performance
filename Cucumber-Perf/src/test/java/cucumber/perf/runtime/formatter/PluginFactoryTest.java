package cucumber.perf.runtime.formatter;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import io.cucumber.core.exception.CucumberException;

public class PluginFactoryTest {
	 PluginFactory pf = new PluginFactory();
	 @Test
     public void instantiates_junit_plugin() throws IOException {
		try {
			@SuppressWarnings("unused")
			Object plugin = pf.create("junit");
		} catch( CucumberException e) {
			assertTrue(e.getMessage().contains("must have a constructor"));
		}
	 }
	 
	 @Test
     public void instantiates_junit_plugin_with_arg() throws IOException {
        Object plugin = pf.create("junit:C:/test/junittest.xml");
        assertEquals(plugin.getClass(),JUnitFormatter.class);
	 }

	 @Test
     public void instantiates_junit_plugin_with_file_arg() throws IOException {
        Object plugin = pf.create("junit:file://C:/test/junittest.xml");
        assertEquals(plugin.getClass(),JUnitFormatter.class);
	 }
	 
	 @Test
     public void instantiates_chart_points_plugin() throws IOException {
		try {
			@SuppressWarnings("unused")
			Object plugin = pf.create("chart_points");
		} catch( CucumberException e) {
			assertTrue(e.getMessage().contains("must supply an output argument"));
		}
	 }
	 
	 @Test
     public void instantiates_chart_points_plugin_with_arg() throws IOException {
	        Object plugin = pf.create("chart_points:C:/test/ccp.csv");
	        assertEquals(plugin.getClass(),ChartPointsFormatter.class);
	 }
	 
	 @Test
     public void instantiates_chart_points_plugin_with_arg_and_count() throws IOException {
	        Object plugin = pf.create("chart_points:C:/test/ccp.csv:30");
	        assertEquals(plugin.getClass(),ChartPointsFormatter.class);
	 }
	 
	 @Test
     public void instantiates_chart_points_plugin_with_arg_and_count_and_minions() throws IOException {
	        Object plugin = pf.create("chart_points:C:/test/ccp.csv:30,prcntl:50,prcntl:60");
	        assertEquals(plugin.getClass(),ChartPointsFormatter.class);
	 }
	 
	 @Test
     public void instantiates_chart_points_plugin_with_arg_and_count_and_minions_warpped() throws IOException {
	        Object plugin = pf.create("chart_points:C:/test/ccp.csv:{30,prcntl:50,prcntl:60}");
	        assertEquals(plugin.getClass(),ChartPointsFormatter.class);
	 }
	 
	 @Test
     public void instantiates_statistics_plugin() throws IOException {
        Object plugin = pf.create("statistics");
        assertEquals(plugin.getClass(),StatisticsFormatter.class);
	 }
	 
	 @Test
     public void instantiates_statistics_plugin_with_arg_as_plugin_w_option() throws IOException {
	        Object plugin = pf.create("statistics:prcntl:50");
	        assertEquals(plugin.getClass(),StatisticsFormatter.class);
	 }
	 
	 @Test
     public void instantiates_statistics_plugin_with_args_as_plugin_w_option() throws IOException {
	        Object plugin = pf.create("statistics:prcntl:50,stdev");
	        assertEquals(plugin.getClass(),StatisticsFormatter.class);
	 }
	 
	 @Test
     public void instantiates_statistics_plugin_with_arg_as_plugins_warpped() throws IOException {
	        Object plugin = pf.create("statistics:{30,prcntl:50,prcntl:60}");
	        assertEquals(plugin.getClass(),StatisticsFormatter.class);
	 } 
}
