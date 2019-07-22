package cucumber.perf.api.result;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cucumber.api.Result;

public class GroupResult extends BaseResult{
	
	private List<ScenarioResult> childResults = new ArrayList<ScenarioResult>();
	
	public GroupResult() {
	}
	
	public GroupResult(String name,Result result, LocalDateTime start, LocalDateTime stop) {
		super(name,result, start, stop);
	}
	
	public GroupResult(GroupResult result) {
		super(result.getName(), new Result(result.getResult().getStatus(),result.getResultDuration(),result.getResult().getError()) ,LocalDateTime.from(result.getStart()), LocalDateTime.from(result.getStop()));

		for (ScenarioResult sec: result.getChildResults())
		{
			ScenarioResult nsec = new ScenarioResult(sec);
			childResults.add(nsec);
		}
		this.updateStatus(childResults);
	}

	public List<ScenarioResult> getChildResults() {
		return childResults;
	}

	public void setChildResults(List<ScenarioResult> childResults) {
		this.childResults = childResults;
		this.updateStatus(childResults);
	}

	public void addChildResult(ScenarioResult childResult) {
		this.childResults.add(childResult);
		this.updateStatus(Arrays.asList(childResult));
	}
	
	private void updateStatus(List<ScenarioResult> childResults)
	{
		Result.Type curtype = Result.Type.PASSED;
		for (ScenarioResult child : childResults)
		{
			curtype = compareStatus(curtype,child.getResult().getStatus());
		}
		Result res = this.getResult();
		this.setResult(new Result(Result.Type.valueOf(curtype.name()),res.getDuration(),res.getError()));
	}
	
	private Result.Type compareStatus(Result.Type curtype, Result.Type compare)
	{
		switch(compare) {
 		case PASSED:
		    break;
 		case SKIPPED:
 			if (Result.Type.SKIPPED.ordinal() > curtype.ordinal())
 				curtype = Result.Type.SKIPPED;
		    break;
 		case PENDING:
 			if (Result.Type.PENDING.ordinal() > curtype.ordinal())
 				curtype = Result.Type.PENDING;
		    break;
 		case UNDEFINED:
 			if (Result.Type.UNDEFINED.ordinal() > curtype.ordinal())
 				curtype = Result.Type.UNDEFINED;
		    break;
 		case AMBIGUOUS:
 			if (Result.Type.AMBIGUOUS.ordinal() > curtype.ordinal())
 				curtype = Result.Type.AMBIGUOUS;
		    break;
 		case FAILED:
 			if (Result.Type.FAILED.ordinal() > curtype.ordinal())
 				curtype = Result.Type.FAILED;
 			break;
		}
		return curtype;
	}
}
