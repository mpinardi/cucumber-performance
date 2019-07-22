package cucumber.perf.api.event;

public class StatisticsStarted extends StatisticsEvent {

    public StatisticsStarted(Long timeStamp, long timeStampMillis) {
        super(timeStamp, timeStampMillis);
    }

}
