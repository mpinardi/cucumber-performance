package cucumber.perf.api.event;

public abstract class SimulationEvent extends TimeStampedEvent {

    SimulationEvent(Long timeStamp, long timeStampMillis) {
        super(timeStamp, timeStampMillis);
    }
}