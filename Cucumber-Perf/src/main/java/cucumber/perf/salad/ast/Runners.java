package cucumber.perf.salad.ast;

import gherkin.ast.Location;
import gherkin.ast.Node;

public class Runners extends Node {
    private final String contentType;
    private final String content;

    public Runners(Location location, String contentType, String content) {
        super(location);
        this.contentType = contentType;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public String getContentType() {
        return contentType;
    }
}
