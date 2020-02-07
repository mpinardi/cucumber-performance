package cucumber.perf.runtime.filter;

import io.cucumber.core.internal.gherkin.ast.Node;

interface Predicate {
    boolean apply(Node n);
}
