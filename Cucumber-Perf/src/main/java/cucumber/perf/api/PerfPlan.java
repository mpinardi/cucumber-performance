package cucumber.perf.api;

import cucumber.api.event.TestSourceRead;
import cucumber.perf.salad.ast.SaladDocument;
import io.cucumber.core.model.FeatureWithLines;
import io.cucumber.core.model.RerunLoader;
import cucumber.runner.EventBus;
import cucumber.runtime.CucumberException;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import static io.cucumber.core.model.Classpath.CLASSPATH_SCHEME;
import cucumber.util.Encoding;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class PerfPlan implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String uri;
    private SaladDocument saladDocument;
    private String saladSource;
    public static final Pattern RERUN_PATH_SPECIFICATION = Pattern.compile("(?m:^| |)(.*?\\.feature(?:(?::\\d+)*))");

    public static List<PerfPlan> load(ResourceLoader resourceLoader, List<String> planPaths, PrintStream out) {
        final List<PerfPlan> perfPlans = load(resourceLoader, planPaths);
        if (perfPlans.isEmpty()) {
            if (planPaths.isEmpty()) {
                out.println("Got no path to plan directory or plan file");
            } else {
                out.println(String.format("No plans found at %s", planPaths));
            }
        }
        return perfPlans;
    }
    
    public static List<PerfPlan> load(ResourceLoader resourceLoader, List<String> planPaths) {
        final List<PerfPlan> perfPlan = new ArrayList<PerfPlan>();
        final PlanBuilder builder = new PlanBuilder(perfPlan);
        for (String planPath : planPaths) {
            if (planPath.startsWith("@")) {
                loadFromRerunFile(builder, resourceLoader, planPath.substring(1));
            } else {
                loadFromFeaturePath(builder, resourceLoader, URIPath.parse(planPath), false);
            }
        }
        Collections.sort(perfPlan, new PerfPlanUriComparator());
        return perfPlan;
    }

    private static void loadFromRerunFile(PlanBuilder builder, ResourceLoader resourceLoader, String rerunPath) {
    	for(FeatureWithLines pathWithLines : new RerunLoader(resourceLoader).load(URIPath.parse(rerunPath))){
            loadFromFileSystemOrClasspath(builder, resourceLoader, pathWithLines.uri());
        }
    }

    @SuppressWarnings("unused")
	private static String read(Resource resource) {
        try {
            return Encoding.readFile(resource);
        } catch (IOException e) {
            throw new CucumberException("Failed to read resource:" + resource.getPath(), e);
        }
    }

    private static void loadFromFileSystemOrClasspath(PlanBuilder builder, ResourceLoader resourceLoader, URI featurePath) {
        try {
            loadFromFeaturePath(builder, resourceLoader, featurePath, false);
        } catch (IllegalArgumentException originalException) {
            if (!featurePath.getScheme().startsWith(CLASSPATH_SCHEME) &&
                    originalException.getMessage().contains("Not a file or directory")) {
                try {
                    loadFromFeaturePath(builder, resourceLoader, URIPath.parse(CLASSPATH_SCHEME + featurePath.toString()), true);
                } catch (IllegalArgumentException secondException) {
                    if (secondException.getMessage().contains("No resource found for")) {
                        throw new IllegalArgumentException("Neither found on file system or on classpath: " +
                                originalException.getMessage() + ", " + secondException.getMessage());
                    } else {
                        throw secondException;
                    }
                }
            } else {
                throw originalException;
            }
        }
    }

    private static void loadFromFeaturePath(PlanBuilder builder, ResourceLoader resourceLoader, URI planPath, boolean failOnNoResource) {
        Iterable<Resource> resources = resourceLoader.resources(planPath, ".plan");
        if (failOnNoResource && !resources.iterator().hasNext()) {
            throw new IllegalArgumentException("No resource found for: " + planPath);
        }
        for (Resource resource : resources) {
            builder.parse(resource);
        }
    }

    public PerfPlan(SaladDocument  saladDocument, String uri, String saladSource) {
        this.saladDocument = saladDocument;
        this.uri = uri;
        this.saladSource = saladSource;
    }

    public SaladDocument getSaladPlan() {
        return saladDocument;
    }

    public String getUri() {
        return uri;
    }

    public void sendTestSourceRead(EventBus bus) {
        bus.send(new TestSourceRead(bus.getTime(),bus.getTimeMillis(), uri, saladSource));
    }

    private static class PerfPlanUriComparator implements Comparator<PerfPlan> {
        @Override
        public int compare(PerfPlan a, PerfPlan b) {
            return a.getUri().compareTo(b.getUri());
        }
    }
}
