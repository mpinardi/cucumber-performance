package cucumber.perf.api.event;

import cucumber.perf.api.formatter.Statistics;

public class StatisticsFinished extends StatisticsEvent {

	private final Statistics result;
	
    public Statistics getResult() {
		return result;
	}

	public StatisticsFinished(Long timeStamp, long timeStampMillis, Statistics result) {
        super(timeStamp, timeStampMillis);
        this.result = result;
    }
 
}
