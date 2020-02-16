package cucumber.perf;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import cucumber.perf.api.CucumberPerfOptionsTest;
import cucumber.perf.api.FeatureBuilderTest;
import cucumber.perf.api.PerfCompilerTest;
import cucumber.perf.api.PerfGroupTest;
import cucumber.perf.api.PerfPlanTest;
import cucumber.perf.api.PlanBuilderTest;
import cucumber.perf.api.result.statistics.DefaultStatisticsTest;
import cucumber.perf.api.result.statistics.StatTest;
import cucumber.perf.api.result.statistics.StatsTest;
import cucumber.perf.runtime.CucumberPerfTest;
import cucumber.perf.runtime.PerfCucumberRunnerTest;
import cucumber.perf.runtime.PerfRuntimeOptionsFactoryTest;
import cucumber.perf.runtime.PerfRuntimeOptionsTest;
import cucumber.perf.runtime.filter.FeatureFilterTest;
import cucumber.perf.runtime.filter.NamePredicateTest;
import cucumber.perf.runtime.filter.TagPredicateTest;
import cucumber.perf.runtime.formatter.AppendableBuilderTest;
import cucumber.perf.runtime.formatter.ChartPointsFormatterTest;
import cucumber.perf.runtime.formatter.JUnitFormatterTest;
import cucumber.perf.runtime.formatter.LoggerFormatterTest;
import cucumber.perf.runtime.formatter.PercentileCreatorTest;
import cucumber.perf.runtime.formatter.StatisticsFormatterTest;
import cucumber.perf.runtime.formatter.StdDeviationCreatorTest;
import cucumber.perf.runtime.formatter.SummaryTextFormatterTest;
import cucumber.perf.runtime.formatter.TaurusFormatterTest;

@RunWith(Suite.class)
@SuiteClasses({ 
				CucumberPerfOptionsTest.class, FeatureBuilderTest.class, PerfCompilerTest.class,PerfGroupTest.class, //api
				PerfPlanTest.class, PlanBuilderTest.class, //api
				StatsTest.class,StatTest.class,DefaultStatisticsTest.class, //stats
				AppendableBuilderTest.class, ChartPointsFormatterTest.class,JUnitFormatterTest.class,LoggerFormatterTest.class,//formatter
				StatisticsFormatterTest.class, SummaryTextFormatterTest.class,TaurusFormatterTest.class, //formatter
				StdDeviationCreatorTest.class,PercentileCreatorTest.class,  //formatter
				CucumberPerfTest.class,  PerfCucumberRunnerTest.class, PerfRuntimeOptionsFactoryTest.class,PerfRuntimeOptionsTest.class, //runtime
				FeatureFilterTest.class, NamePredicateTest.class, TagPredicateTest.class})//filter

public class AllTests {

}
