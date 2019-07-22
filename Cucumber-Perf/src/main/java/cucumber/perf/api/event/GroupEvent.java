package cucumber.perf.api.event;

import cucumber.perf.api.PerfGroup;

public abstract class GroupEvent extends TimeStampedEvent {

    private final PerfGroup group;
    private final int groupId;

    GroupEvent(Long timeStamp, long timeStampMillis, int groupId, PerfGroup group) {
        super(timeStamp, timeStampMillis);
        this.group = group;
        this.groupId = groupId;
    }

    public PerfGroup getGroup() {
        return group;
    }
    
    public int getGroupId() {
        return groupId;
    }
}