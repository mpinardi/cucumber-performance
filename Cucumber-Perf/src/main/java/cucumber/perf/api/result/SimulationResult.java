package cucumber.perf.api.result;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cucumber.api.Result;

public class SimulationResult extends BaseResult{
	
	private List<GroupResult> childResults = new ArrayList<GroupResult>();
	private long totalRan = 0;
	public SimulationResult() {
	}
	
	public SimulationResult(String name, Result result, LocalDateTime start, LocalDateTime stop, List<GroupResult> childResults) {
		super(name,result, start, stop);
		this.childResults = childResults;
		for (GroupResult gr: childResults)
		{
			totalRan += gr.getChildResults().size();
		}
	}
	
	public SimulationResult(SimulationResult result) {
		super(result.getName(), new Result(result.getResult().getStatus(),result.getResultDuration(),result.getResult().getError()) ,LocalDateTime.from(result.getStart()), LocalDateTime.from(result.getStop()));

		for (GroupResult gr: result.getChildResults())
		{
			totalRan += gr.getChildResults().size();
			GroupResult ngr = new GroupResult(gr);
			childResults.add(ngr);
		}
		this.updateStatus(childResults);
	}
	
	public long getTotalRan() {
		return totalRan;
	}
	
	public List<GroupResult> getChildResults() {
		return childResults;
	}

	public void setChildResults(List<GroupResult> childResults) {
		this.childResults = childResults;
		this.updateStatus(childResults);
	}

	public void addChildResult(GroupResult childResult) {
		this.childResults.add(childResult);
		this.updateStatus(Arrays.asList(childResult));
	}
	
	private void updateStatus(List<GroupResult> childResults)
	{
		Result.Type curtype = Result.Type.PASSED;
		for (GroupResult child : childResults)
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
		default:
			break;
		}
		return curtype;
	}
}
