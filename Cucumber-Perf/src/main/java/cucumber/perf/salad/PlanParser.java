package cucumber.perf.salad;

import java.net.URI;
import java.util.UUID;
import java.util.function.Supplier;

import cucumber.perf.api.plan.PerfPlan;

public interface PlanParser {

    PerfPlan parse(URI path, String source, Supplier<UUID> idGenerator);

    String version();

}
