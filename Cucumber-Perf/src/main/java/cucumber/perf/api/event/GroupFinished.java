package cucumber.perf.api.event;

import cucumber.perf.api.PerfGroup;
import cucumber.perf.api.result.GroupResult;

public class GroupFinished extends GroupEvent {

	private final GroupResult result;
	
    public GroupFinished(Long timeStamp, long timeStampMillis, int groupId, PerfGroup group, GroupResult result) {
        super(timeStamp, timeStampMillis,groupId, group);
        this.result = result;
    }
    
    public GroupResult getResult() {
        return result;
    }
 
}
