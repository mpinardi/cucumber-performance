package cucumber.perf.api.event;

public class ChartStarted extends StatisticsEvent{

	public ChartStarted(Long timeStamp, long timeStampMillis) {
        super(timeStamp, timeStampMillis);
    }
 
}
