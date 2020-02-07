package cucumber.perf.api.plan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.resource.ResourceScanner;

import static java.util.Comparator.comparing;
import static java.util.Optional.of;
import static java.util.stream.Collectors.joining;

import java.net.URI;

/**
 * Supplies a list of planss found on the the plans path provided to RuntimeOptions.
 */
public final class PathPlanSupplier implements PlanSupplier {
	
	private static final io.cucumber.core.logging.Logger log = LoggerFactory.getLogger(PathPlanSupplier.class);

    private final ResourceScanner<PerfPlan> planScanner;
    private final List<URI> paths;

    public PathPlanSupplier(Supplier<ClassLoader> classLoader, List<String> planPaths,  PlanParser parser) {
    	List<URI> uris = new ArrayList<URI>();
    	for (String s : planPaths)
    			uris.add(PlanPath.parse(s));
    	paths = uris;
        this.planScanner = new ResourceScanner<>(
            classLoader,
            PlanIdentifier::isPlan,
            resource -> of(parser.parseResource(resource))
        );
    }

    @Override
    public List<PerfPlan> get() {
        List<PerfPlan> planPaths = loadPlans(paths);
        if (planPaths.isEmpty()) {
            if (paths.isEmpty()) {
                log.warn(() -> "Got no path to plans directory or plans file");
            } else {
               log.warn(() -> "No planss found at " + paths.stream().map(URI::toString).collect(joining(", ")));
            }
        }
        return planPaths;
    }

    private List<PerfPlan> loadPlans(List<URI> planPaths) {
        log.debug(() -> "Loading planPaths from " + planPaths.stream().map(URI::toString).collect(joining(", ")));
        final PlanBuilder builder = new PlanBuilder();

        for (URI planPath : planPaths) {
            List<PerfPlan> found = planScanner.scanForResourcesUri(planPath);
            if (found.isEmpty() && PlanIdentifier.isPlan(planPath)) {
                throw new IllegalArgumentException("Plan not found: " + planPath);
            }
            found.forEach(builder::addUnique);
        }

        return builder.build();
    }

    static final class PlanBuilder {

        private final Map<String, Map<String, PerfPlan>> sourceToPlan = new HashMap<>();
        private final List<PerfPlan> planss = new ArrayList<>();

        List<PerfPlan> build() {
            List<PerfPlan> planss = new ArrayList<>(this.planss);
            planss.sort(comparing(PerfPlan::getUri));
            return planss;
        }

        void addUnique(PerfPlan parsedPlan) {
            String parsedFileName = getFileName(parsedPlan);

            Map<String, PerfPlan> existingPlans = sourceToPlan.get(parsedPlan.getSource());
            if (existingPlans != null) {
                // Same contents but different file names was probably intentional
                PerfPlan existingPlan = existingPlans.get(parsedFileName);
                if (existingPlan != null) {
                    log.error(() -> "" +
                        "Duplicate plans found: " +
                        parsedPlan.getUri() + " was identical to " + existingPlan.getUri() + "\n" +
                        "\n" +
                        "This typically happens when you configure cucumber to look " +
                        "for planss in the root of your project.\nYour build tool " +
                        "creates a copy of these planss in a 'target' or 'build'" +
                        "directory.\n" +
                        "\n" +
                        "If your planss are on the class path consider using a class path URI.\n" +
                        "For example: 'classpath:com/example/app.plans'\n" +
                        "Otherwise you'll have to provide a more specific location"
                    );
                    return;
                }
            }
            sourceToPlan.putIfAbsent(parsedPlan.getSource(), new HashMap<>());
            sourceToPlan.get(parsedPlan.getSource()).put(parsedFileName, parsedPlan);
            planss.add(parsedPlan);
        }

        private String getFileName(PerfPlan plans) {
            String uri = plans.getUri().getSchemeSpecificPart();
            int i = uri.lastIndexOf("/");
            return i > 0 ? uri.substring(i) : uri;
        }
    }

}
