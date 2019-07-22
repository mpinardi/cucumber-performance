package cucumber.perf.api.event;

public class SimulationStarted extends SimulationEvent {

	private final String name; 
	
    public SimulationStarted(Long timeStamp, long timeStampMillis, String name) {
        super(timeStamp, timeStampMillis);
        this.name = name;
    }
    
	public String getName() {
		return name;
	}

}
