package cucumber.perf.api.result;

import java.net.URI;
import java.util.List;
import java.util.UUID;

public class TestCase implements io.cucumber.plugin.event.TestCase{

	private int line;
	private URI uri;
	private String name;
	private String scenarioDesignation;
	private List<String> tags;
	private List<io.cucumber.plugin.event.TestStep> testSteps;
	
	public TestCase()
	{
		
	}
	
	public TestCase(int line,URI uri,String name,String scenarioDesignation,List<String> tags,List<io.cucumber.plugin.event.TestStep> testSteps)
	{
		this.line = line;
		this.uri = uri;
		this.name = name;
		this.scenarioDesignation = scenarioDesignation;
		this.tags = tags;
		this.testSteps = testSteps;
	}
	
	public TestCase(io.cucumber.plugin.event.TestCase testCase)
	{
		this(testCase.getLine(),testCase.getUri(),testCase.getName(),testCase.getScenarioDesignation(),testCase.getTags(),testCase.getTestSteps());
	}
	
	@Override
	public Integer getLine() {
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
	public List<String> getTags() {
		return tags;
	}
	
	@Override
	public List<io.cucumber.plugin.event.TestStep> getTestSteps() {
		return testSteps;
	}

	@Override
	public URI getUri() {
		return uri;
	}

	@Override
	public String getKeyword() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UUID getId() {
		// TODO Auto-generated method stub
		return null;
	}

}
