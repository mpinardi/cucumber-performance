package cucumber.api.perf.result;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import cucumber.api.Result;

public class FeatureResult extends StepResult{
	
	private List<ScenarioResult> childResults = new ArrayList<ScenarioResult>();
	
	public FeatureResult(String name,Result result, LocalDateTime start, LocalDateTime stop) {
		super(name,result, start, stop);
	}
	
	public FeatureResult(FeatureResult result) {
		super(result.getName(), new Result(result.getResult().getStatus(),result.getResultDuration(),result.getResult().getError()) ,LocalDateTime.from(result.getStart()), LocalDateTime.from(result.getStop()));

		for (ScenarioResult sec: result.getChildResults())
		{
			ScenarioResult nsec = new ScenarioResult(sec);
			childResults.add(nsec);
		}
		
	}

	public List<ScenarioResult> getChildResults() {
		return childResults;
	}

	public void setChildResults(List<ScenarioResult> childResults) {
		this.childResults = childResults;
	}

	public void addChildResult(ScenarioResult childResult) {
		this.childResults.add(childResult);
	}
}
