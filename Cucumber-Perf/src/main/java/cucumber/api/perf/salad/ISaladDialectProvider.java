package cucumber.api.perf.salad;
import gherkin.ast.Location;

import java.util.List;

public interface ISaladDialectProvider {
    SaladDialect getDefaultDialect();

    SaladDialect getDialect(String language, Location location);

    List<String> getLanguages();
}
