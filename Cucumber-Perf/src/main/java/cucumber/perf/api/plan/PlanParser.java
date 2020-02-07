package cucumber.perf.api.plan;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.resource.Resource;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.function.Supplier;

import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;

public final class PlanParser {

    private final Supplier<UUID> idGenerator;

    public PlanParser(Supplier<UUID> idGenerator) {
        this.idGenerator = idGenerator;
    }


    public PerfPlan parseResource(Resource resource) {
        requireNonNull(resource);
        URI uri = resource.getUri();
        String source = read(resource);
        ServiceLoader<cucumber.perf.salad.PlanParser> services =ServiceLoader.load(cucumber.perf.salad.PlanParser.class);
        Iterator<cucumber.perf.salad.PlanParser> iterator = services.iterator();
        List<cucumber.perf.salad.PlanParser> parser = new ArrayList<>();
        while (iterator.hasNext()) {
            parser.add(iterator.next());
        }
        Comparator<cucumber.perf.salad.PlanParser> version =
            comparing(cucumber.perf.salad.PlanParser::version);
        return Collections.max(parser, version).parse(uri, source, idGenerator);
    }

    private static String read(Resource resource) {
        try {
            return Encoding.readFile(resource);
        } catch (IOException e) {
            throw new CucumberException("Failed to read resource:" + resource.getUri(), e);
        }
    }


}
