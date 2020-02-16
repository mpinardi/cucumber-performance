package cucumber.perf.api.event;

import cucumber.perf.api.result.statistics.Chart;

public class ChartFinished extends StatisticsEvent{

	private final Chart result;
	
    public Chart getResult() {
		return result;
	}

	public ChartFinished(Long timeStamp, long timeStampMillis, Chart result) {
        super(timeStamp, timeStampMillis);
        this.result = result;
    }
 
}
