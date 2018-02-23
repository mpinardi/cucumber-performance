package cucumber.api.perf.salad.ast;

import java.util.List;

import gherkin.ast.Location;
import gherkin.ast.Node;

public class Group extends Node {
    private final String keyword;
    private final String text;
    private final List<Node> arguments;

    public Group(Location location, String keyword, String text, List<Node> arguments) {
        super(location);
        this.keyword = keyword;
        this.text = text;
        this.arguments = arguments;
    }

    public String getText() {
        return text;
    }

    public String getKeyword() {
        return keyword;
    }

    public List<Node> getArguments() {
        return arguments;
    }

}
