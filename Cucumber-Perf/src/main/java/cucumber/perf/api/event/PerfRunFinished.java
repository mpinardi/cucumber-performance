package cucumber.perf.api.event;

public class PerfRunFinished extends TimeStampedEvent {
	
    public PerfRunFinished(Long timeStamp, long timeStampMillis) {
        super(timeStamp, timeStampMillis);
    }

}
