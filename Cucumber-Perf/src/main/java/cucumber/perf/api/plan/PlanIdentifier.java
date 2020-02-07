package cucumber.perf.api.plan;

import java.net.URI;
import java.nio.file.Path;

/**
 * Identifies a single plan.
 * <p>
 * Features are identified by a URI as defined in {@link FeaturePath}.
 * Additionally the scheme specific part must end with {@code .plan}
 *
 * @see FeatureWithLines
 */
public class PlanIdentifier {

    private static final String PLAN_FILE_SUFFIX = ".plan";

    private PlanIdentifier() {

    }

    public static URI parse(String planIdentifier) {
        return parse(PlanPath.parse(planIdentifier));
    }

    public static URI parse(URI planIdentifier) {
        if (!isPlan(planIdentifier)) {
            throw new IllegalArgumentException("planIdentifier does not reference a single plan file: " + planIdentifier);
        }
        return planIdentifier;
    }

    public static boolean isPlan(URI planIdentifier) {
        return planIdentifier.getSchemeSpecificPart().endsWith(PLAN_FILE_SUFFIX);
    }

    public static boolean isPlan(Path path) {
        return path.getFileName().toString().endsWith(PLAN_FILE_SUFFIX);
    }
}
