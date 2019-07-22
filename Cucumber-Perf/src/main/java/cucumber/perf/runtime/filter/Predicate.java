package cucumber.perf.runtime.filter;

import gherkin.ast.Node;

interface Predicate {
    boolean apply(Node n);
}
