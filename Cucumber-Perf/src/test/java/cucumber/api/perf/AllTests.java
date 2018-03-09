package cucumber.api.perf;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import cucumber.api.perf.formatter.ChartPointsFormatterTest;
import cucumber.api.perf.formatter.JUnitFormatterTest;
import cucumber.api.perf.formatter.StatisticsTest;

@RunWith(Suite.class)
@SuiteClasses({ CucumberPerfOptionsTest.class, CucumberPerfTest.class, FeatureBuilderTest.class, PerfCompilerTest.class,
		PerfCucumberRunnerTest.class, PerfGroupTest.class, PerfPlanTest.class, PerfRuntimeOptionsFactoryTest.class,
		PerfRuntimeOptionsTest.class, PlanBuilderTest.class, JUnitFormatterTest.class,ChartPointsFormatterTest.class,StatisticsTest.class })

public class AllTests {

}
