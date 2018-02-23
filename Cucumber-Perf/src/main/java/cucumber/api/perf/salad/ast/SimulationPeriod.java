package cucumber.api.perf.salad.ast;

import java.util.Collections;
import java.util.List;

import gherkin.ast.Location;
import gherkin.ast.Tag;

public class SimulationPeriod extends SimulationDefinition {
    private final List<Tag> tags;
    private final Time time;
    private final Time rampUp;
    private final Time rampDown;
    private final Count synchronize;
    private final Time randomWait;

    public SimulationPeriod(List<Tag> tags, Location location, String keyword, String name, String description, List<Group> steps, Time time, Time rampUp, Time rampDown, Count synchronize, Time randomWait) {
        super(location, keyword, name, description, steps);
        this.time = time;
        this.tags = Collections.unmodifiableList(tags);
        this.rampDown = rampDown;
        this.rampUp = rampUp;
        this.synchronize = synchronize;
        this.randomWait = randomWait;
    }

    public List<Tag> getTags() {
        return tags;
    }
    
    public Time getTime() {
        return time;
    }
    
    public Time getRampUp() {
 		return rampUp;
 	}

 	public Time getRampDown() {
 		return rampDown;
 	}

	public Count getSynchronize() {
		return synchronize;
	}

	public Time getRandomWait() {
		return randomWait;
	}
}
