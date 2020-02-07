package cucumber.perf.api.result;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;

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
		Status curtype = Status.PASSED;
		for (GroupResult child : childResults)
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
		default:
			break;
		}
		return curtype;
	}
}
