package cucumber.api.perf;

import gherkin.ast.Node;

interface Predicate {
    boolean apply(Node n);
}
