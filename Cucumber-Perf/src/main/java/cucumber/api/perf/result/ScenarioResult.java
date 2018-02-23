package cucumber.api.perf.result;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import cucumber.api.Result;

public class ScenarioResult extends StepResult{

	private List<StepResult> childResults = new ArrayList<StepResult>();
	private String uri;
	
	public ScenarioResult(String name, String uri, Result result, LocalDateTime start, LocalDateTime stop) {
		super(name, result, start, stop);
		this.uri = uri;
	}
	
	public ScenarioResult(ScenarioResult result) {
		super(result.getName(),new Result(result.getResult().getStatus(),result.getResultDuration(),result.getError()), result.getStart(), result.getStop());
		this.uri = result.getUri();
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

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
}
