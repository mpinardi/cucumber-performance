package cucumber.perf.api.event;

import cucumber.perf.api.PerfGroup;

public class GroupStarted extends GroupEvent {

    public GroupStarted(Long timeStamp, long timeStampMillis, int groupId, PerfGroup group) {
        super(timeStamp, timeStampMillis, groupId, group);
    }

}
