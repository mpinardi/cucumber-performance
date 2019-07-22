package cucumber.perf.api.event;

import cucumber.perf.api.result.SimulationResult;

public class SimulationFinished extends SimulationEvent {

	private final SimulationResult result;
	
    public SimulationFinished(Long timeStamp, long timeStampMillis, SimulationResult result) {
        super(timeStamp, timeStampMillis);
        this.result = result;
    }

	public SimulationResult getResult() {
		return result;
	}
 
}
