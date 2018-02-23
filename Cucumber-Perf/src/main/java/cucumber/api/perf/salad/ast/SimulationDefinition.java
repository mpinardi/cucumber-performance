package cucumber.api.perf.salad.ast;

import java.util.Collections;
import java.util.List;

import gherkin.ast.Location;
import gherkin.ast.Node;

public abstract class SimulationDefinition extends Node {
    private final String keyword;
    private final String name;
    private final String description;
    private final List<Group> groups;

	public SimulationDefinition(Location location, String keyword, String name, String description, List<Group> groups) {
        super(location);
        this.keyword = keyword;
        this.name = name;
        this.description = description;
        this.groups = Collections.unmodifiableList(groups);
    }

    public String getName() {
        return name;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getDescription() {
        return description;
    }

    public List<Group> getGroups() {
        return groups;
    } 
}
