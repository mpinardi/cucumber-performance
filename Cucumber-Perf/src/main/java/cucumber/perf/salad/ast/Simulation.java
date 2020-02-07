package cucumber.perf.salad.ast;

import java.util.Collections;
import java.util.List;

import io.cucumber.core.internal.gherkin.ast.Location;
import io.cucumber.core.internal.gherkin.ast.Tag;


public class Simulation extends SimulationDefinition {
    private final List<Tag> tags;
    private final Time rampUp;
    private final Time rampDown;
    private final Count synchronize;
    private final Time randomWait;

    public Simulation(List<Tag> tags, Location location, String keyword, String name, String description, List<Group> steps, Time rampUp, Time rampDown,Count synchronize, Time randomWait) {
        super(location, keyword, name, description, steps);
        this.tags = Collections.unmodifiableList(tags);
        this.rampDown = rampDown;
        this.rampUp = rampUp;
        this.synchronize = synchronize;
        this.randomWait = randomWait;
    }
    
    public List<Tag> getTags() {
        return tags;
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
