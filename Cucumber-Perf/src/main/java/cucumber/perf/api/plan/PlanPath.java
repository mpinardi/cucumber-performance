package cucumber.perf.api.plan;

import java.io.File;
import java.net.URI;
import java.util.Locale;

import static io.cucumber.core.resource.ClasspathSupport.CLASSPATH_SCHEME_PREFIX;
import static io.cucumber.core.resource.ClasspathSupport.RESOURCE_SEPARATOR_CHAR;
import static io.cucumber.core.resource.ClasspathSupport.rootPackageUri;
import static java.util.Objects.requireNonNull;

/**
 * A plan path is a URI to a single plan file or directory of plans.
 * <p>
 * This URI can either be absolute:
 * {@code scheme:/absolute/path/to.plan}, or relative to the
 * current working directory: {@code scheme:relative/path/to.plan}. In
 * either form, when the scheme is omitted {@code file} will be assumed.
 * <p>
 * On systems that use a {@code File.separatorChar} other then `{@code /}`
 * {@code File.separatorChar} can be used as a path separator. When
 * doing so when the scheme must be omitted: {@code path\to.plan}.
 * <em>It is recommended to use `{@code /}` as the path separator.</em>
 *
 * @see FeatureIdentifier
 * @see FeatureWithLines
 */
public class PlanPath {

    private PlanPath() {

    }

    public static URI parse(String planIdentifier) {
        requireNonNull(planIdentifier, "planIdentifier may not be null");
        if (planIdentifier.isEmpty()) {
            throw new IllegalArgumentException("planIdentifier may not be empty");
        }

        // Legacy from the Cucumber Eclipse plugin
        // Older versions of Cucumber allowed it.
        if (CLASSPATH_SCHEME_PREFIX.equals(planIdentifier)) {
            return rootPackageUri();
        }

        if (nonStandardPathSeparatorInUse(planIdentifier)) {
            String standardized = replaceNonStandardPathSeparator(planIdentifier);
            return parseAssumeFileScheme(standardized);
        }

        if (isWindowsOS() && pathContainsWindowsDrivePattern(planIdentifier)) {
            return parseAssumeFileScheme(planIdentifier);
        }

        if (probablyURI(planIdentifier)) {
            return parseProbableURI(planIdentifier);
        }

        return parseAssumeFileScheme(planIdentifier);
    }

    private static URI parseProbableURI(String planIdentifier) {
        URI uri = URI.create(planIdentifier);
        if ("file".equals(uri.getScheme())) {
            return parseAssumeFileScheme(uri.getSchemeSpecificPart());
        }
        return uri;
    }

    private static boolean isWindowsOS() {
        String osName = System.getProperty("os.name");
        return normalize(osName).contains("windows");
    }

    private static boolean pathContainsWindowsDrivePattern(String planIdentifier) {
        return planIdentifier.matches("^[a-zA-Z]:.*$");
    }

    private static boolean probablyURI(String planIdentifier) {
        return planIdentifier.matches("^[a-zA-Z+.\\-]+:.*$");
    }

    private static String replaceNonStandardPathSeparator(String planIdentifier) {
        return planIdentifier.replace(File.separatorChar, RESOURCE_SEPARATOR_CHAR);
    }

    private static boolean nonStandardPathSeparatorInUse(String planIdentifier) {
        return File.separatorChar != RESOURCE_SEPARATOR_CHAR
            && planIdentifier.contains(File.separator);
    }

    private static URI parseAssumeFileScheme(String planIdentifier) {
        File planFile = new File(planIdentifier);
        return planFile.toURI();
    }

    private static String normalize(final String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "");
    }

}