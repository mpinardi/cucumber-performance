package cucumber.perf.api.event;

public class PerfRunStarted extends TimeStampedEvent {

    public PerfRunStarted(Long timeStamp, long timeStampMillis) {
        super(timeStamp, timeStampMillis);
    }

}
