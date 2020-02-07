package cucumber.perf;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import cucumber.perf.api.CucumberPerfOptionsTest;
import cucumber.perf.api.FeatureBuilderTest;
import cucumber.perf.api.PerfGroupTest;
import cucumber.perf.api.plan.PathPlanSupplierTest;
import cucumber.perf.api.plan.PerfPlanParserTest;
import cucumber.perf.api.plan.PerfPlanTest;
import cucumber.perf.api.plan.PlanIdentifierTest;
import cucumber.perf.api.plan.PlanParserTest;
import cucumber.perf.api.plan.PlanPathTest;
import cucumber.perf.formatter.AppendableBuilderTest;
import cucumber.perf.formatter.ChartPointsFormatterTest;
import cucumber.perf.formatter.JUnitFormatterTest;
import cucumber.perf.formatter.LoggerFormatterTest;
import cucumber.perf.formatter.StatisticsFormatterTest;
import cucumber.perf.formatter.SummaryTextFormatterTest;
import cucumber.perf.runtime.CucumberPerfTest;
import cucumber.perf.runtime.PerfCucumberRunnerTest;
import cucumber.perf.runtime.PerfRuntimeOptionsFactoryTest;
import cucumber.perf.runtime.PerfRuntimeOptionsTest;
import cucumber.perf.runtime.filter.FeatureFilterTest;
import cucumber.perf.runtime.filter.NamePredicateTest;
import cucumber.perf.runtime.filter.TagPredicateTest;

@RunWith(Suite.class)
@SuiteClasses({ 
				CucumberPerfOptionsTest.class, FeatureBuilderTest.class,PerfGroupTest.class, //api
				PathPlanSupplierTest.class, PerfPlanParserTest.class,PerfPlanTest.class,PlanIdentifierTest.class,PlanParserTest.class,PlanPathTest.class, //
				AppendableBuilderTest.class, ChartPointsFormatterTest.class,JUnitFormatterTest.class,LoggerFormatterTest.class,//formatter
				StatisticsFormatterTest.class, SummaryTextFormatterTest.class, //formatter
				CucumberPerfTest.class,  PerfCucumberRunnerTest.class, PerfRuntimeOptionsFactoryTest.class,PerfRuntimeOptionsTest.class, //runtime
				FeatureFilterTest.class, NamePredicateTest.class, TagPredicateTest.class})//filter

public class AllTests {

}
