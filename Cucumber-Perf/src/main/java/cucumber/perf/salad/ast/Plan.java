package cucumber.perf.salad.ast;

import java.util.Collections;
import java.util.List;

import io.cucumber.core.internal.gherkin.ast.Location;
import io.cucumber.core.internal.gherkin.ast.Node;
import io.cucumber.core.internal.gherkin.ast.Tag;


public class Plan extends Node {
    private final List<Tag> tags;
    private final String language;
    private final String keyword;
    private final String name;
    private final String description;
    private final List<SimulationDefinition> children;

    public Plan(
            List<Tag> tags,
            Location location,
            String language,
            String keyword,
            String name,
            String description,
            List<SimulationDefinition> children) {
        super(location);
        this.tags = Collections.unmodifiableList(tags);
        this.language = language;
        this.keyword = keyword;
        this.name = name;
        this.description = description;
        this.children = Collections.unmodifiableList(children);
    }

    public List<SimulationDefinition> getChildren() {
        return children;
    }

    public String getLanguage() {
        return language;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<Tag> getTags() {
        return tags;
    }
}
