package cucumber.perf.api.result;

import java.util.List;

import cucumber.api.TestStep;
import gherkin.pickles.PickleTag;

public class TestCase implements cucumber.api.TestCase{

	private int line;
	private String uri;
	private String name;
	private String scenarioDesignation;
	private List<PickleTag> tags;
	private List<TestStep> testSteps;
	
	public TestCase()
	{
		
	}
	
	public TestCase(int line,String uri,String name,String scenarioDesignation,List<PickleTag> tags,List<TestStep> testSteps)
	{
		this.line = line;
		this.uri = uri;
		this.name = name;
		this.scenarioDesignation = scenarioDesignation;
		this.tags = tags;
		this.testSteps = testSteps;
	}
	
	public TestCase(cucumber.api.TestCase testCase)
	{
		this(testCase.getLine(),testCase.getUri(),testCase.getName(),testCase.getScenarioDesignation(),testCase.getTags(),testCase.getTestSteps());
	}
	
	@Override
	public int getLine() {
		return line;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getScenarioDesignation() {
		return scenarioDesignation;
	}

	@Override
	public List<PickleTag> getTags() {
		return tags;
	}
	
	@Override
	public List<TestStep> getTestSteps() {
		return testSteps;
	}

	@Override
	public String getUri() {
		return uri;
	}
}
