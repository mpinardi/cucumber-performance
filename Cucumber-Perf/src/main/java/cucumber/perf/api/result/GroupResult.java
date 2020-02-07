package cucumber.perf.api.result;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;

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
		Status curtype = Status.PASSED;
		for (ScenarioResult child : childResults)
		{
			curtype = compareStatus(curtype,child.getResult().getStatus());
		}
		Result res = this.getResult();
		this.setResult(new Result(Status.valueOf(curtype.name()),res.getDuration(),res.getError()));
	}
	
	private Status compareStatus(Status curtype, Status compare)
	{
		switch(compare) {
 		case PASSED:
		    break;
 		case SKIPPED:
 			if (Status.SKIPPED.ordinal() > curtype.ordinal())
 				curtype = Status.SKIPPED;
		    break;
 		case PENDING:
 			if (Status.PENDING.ordinal() > curtype.ordinal())
 				curtype = Status.PENDING;
		    break;
 		case UNDEFINED:
 			if (Status.UNDEFINED.ordinal() > curtype.ordinal())
 				curtype = Status.UNDEFINED;
		    break;
 		case AMBIGUOUS:
 			if (Status.AMBIGUOUS.ordinal() > curtype.ordinal())
 				curtype = Status.AMBIGUOUS;
		    break;
 		case FAILED:
 			if (Status.FAILED.ordinal() > curtype.ordinal())
 				curtype = Status.FAILED;
 			break;
		case UNUSED:
			break;
		default:
			break;
		}
		return curtype;
	}
}
