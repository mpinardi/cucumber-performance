package cucumber.perf.api.plan;

//import cucumber.api.event.TestSourceRead;
import cucumber.perf.salad.ast.SaladDocument;
import io.cucumber.core.eventbus.EventBus;
/*import io.cucumber.core.model.FeatureWithLines;
import io.cucumber.core.model.RerunLoader;
import cucumber.runner.EventBus;
import cucumber.runtime.CucumberException;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import static io.cucumber.core.model.Classpath.CLASSPATH_SCHEME;
import cucumber.util.Encoding;*/
import io.cucumber.plugin.event.TestSourceRead;

//import static java.util.Optional.of;

import java.io.Serializable;
import java.net.URI;
import java.util.Comparator;
import java.util.regex.Pattern;

public class PerfPlan implements Serializable {
    private static final long serialVersionUID = 1L;
    private final URI uri;
    private SaladDocument saladDocument;
    private String saladSource;
    public static final Pattern RERUN_PATH_SPECIFICATION = Pattern.compile("(?m:^| |)(.*?\\.plan(?:(?::\\d+)*))");

    public PerfPlan(SaladDocument  saladDocument, URI uri, String saladSource) {
        this.saladDocument = saladDocument;
        this.uri = uri;
        this.saladSource = saladSource;
    }

    public SaladDocument getSaladPlan() {
        return saladDocument;
    }
    

    public String getSource() {
        return saladSource;
    }

    public URI getUri() {
        return uri;
    }

    public void sendTestSourceRead(EventBus bus) {
        bus.send(new TestSourceRead(bus.getInstant(), uri, saladSource));
    }

    @SuppressWarnings("unused")
	private static class PerfPlanUriComparator implements Comparator<PerfPlan> {
        @Override
        public int compare(PerfPlan a, PerfPlan b) {
            return a.getUri().compareTo(b.getUri());
        }
    }
}
