package cucumber.perf.salad;

import java.util.List;

import io.cucumber.core.internal.gherkin.ast.Location;

public interface ISaladDialectProvider {
    SaladDialect getDefaultDialect();

    SaladDialect getDialect(String language, Location location);

    List<String> getLanguages();
}
