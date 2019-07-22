package cucumber.perf.salad.ast;

import gherkin.ast.Location;
import gherkin.ast.Node;

public class Time extends Node {
    private final String keyword;
    private final String text;

    public Time(Location location, String keyword, String text) {
        super(location);
        this.keyword = keyword;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getKeyword() {
        return keyword;
    }

}
