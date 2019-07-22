package cucumber.perf.api.event;

public abstract class StatisticsEvent extends TimeStampedEvent {

    StatisticsEvent(Long timeStamp, long timeStampMillis) {
        super(timeStamp, timeStampMillis);
    }
}