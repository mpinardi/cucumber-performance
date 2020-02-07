package cucumber.perf.api.result;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import io.cucumber.plugin.event.Result;

public class ScenarioResult extends BaseResult{

	private List<StepResult> childResults = new ArrayList<StepResult>();
	private TestCase testCase;
	
	public ScenarioResult() {
	}
	
	public ScenarioResult(String name, TestCase testCase, Result result, LocalDateTime start, LocalDateTime stop) {
		super(name, result, start, stop);
		this.setTestCase(testCase);
	}
	
	public ScenarioResult(ScenarioResult result) {
		super(result.getName(),new Result(result.getResult().getStatus(),result.getResultDuration(),result.getError()), result.getStart(), result.getStop());
		this.setTestCase(new TestCase(result.getTestCase()));
		for (StepResult sec: result.getChildResults())
		{
			StepResult nsec = new StepResult(sec);
			childResults.add(nsec);
		}
	}

	public List<StepResult> getChildResults() {
		return childResults;
	}

	public void setChildResults(List<StepResult> childResults) {
		this.childResults = childResults;
	}
	
	public void addChildResult(StepResult childResult) {
		this.childResults.add(childResult);
	}

	public TestCase getTestCase() {
		return testCase;
	}

	public void setTestCase(TestCase testCase) {
		this.testCase = testCase;
	}
}
