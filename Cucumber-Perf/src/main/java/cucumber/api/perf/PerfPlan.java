package cucumber.api.perf;

import cucumber.api.event.TestSourceRead;
import cucumber.api.perf.salad.ast.SaladDocument;
import cucumber.runner.EventBus;
import cucumber.runtime.CucumberException;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.PathWithLines;
import cucumber.util.Encoding;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
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
                loadFromFeaturePath(builder, resourceLoader, planPath, false);
            }
        }
        Collections.sort(perfPlan, new PerfPlanUriComparator());
        return perfPlan;
    }

    private static void loadFromRerunFile(PlanBuilder builder, ResourceLoader resourceLoader, String rerunPath) {
        for(PathWithLines pathWithLines : loadRerunFile(resourceLoader, rerunPath)){
            loadFromFileSystemOrClasspath(builder, resourceLoader, pathWithLines.path);
        }
    }

    public static List<PathWithLines> loadRerunFile(ResourceLoader resourceLoader, String rerunPath) {
        List<PathWithLines> featurePaths = new ArrayList<PathWithLines>();
        Iterable<Resource> resources = resourceLoader.resources(rerunPath, null);
        for (Resource resource : resources) {
            String source = read(resource);
            if (!source.isEmpty()) {
                Matcher matcher = RERUN_PATH_SPECIFICATION.matcher(source);
                while(matcher.find()){
                    featurePaths.add(new PathWithLines(matcher.group(1)));
                }
            }
        }
        return featurePaths;
    }

    private static String read(Resource resource) {
        try {
            return Encoding.readFile(resource);
        } catch (IOException e) {
            throw new CucumberException("Failed to read resource:" + resource.getPath(), e);
        }
    }

    private static void loadFromFileSystemOrClasspath(PlanBuilder builder, ResourceLoader resourceLoader, String featurePath) {
        try {
            loadFromFeaturePath(builder, resourceLoader, featurePath, false);
        } catch (IllegalArgumentException originalException) {
            if (!featurePath.startsWith(MultiLoader.CLASSPATH_SCHEME) &&
                    originalException.getMessage().contains("Not a file or directory")) {
                try {
                    loadFromFeaturePath(builder, resourceLoader, MultiLoader.CLASSPATH_SCHEME + featurePath, true);
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

    private static void loadFromFeaturePath(PlanBuilder builder, ResourceLoader resourceLoader, String planPath, boolean failOnNoResource) {
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
        bus.send(new TestSourceRead(bus.getTime(), uri, saladSource));
    }

    private static class PerfPlanUriComparator implements Comparator<PerfPlan> {
        @Override
        public int compare(PerfPlan a, PerfPlan b) {
            return a.getUri().compareTo(b.getUri());
        }
    }
}
