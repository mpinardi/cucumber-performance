package cucumber.perf.api.plan;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.junit.Test;

import cucumber.perf.api.plan.PerfPlan;
import io.cucumber.core.resource.Resource;


public class PlanParserTest {
	@Test
	public void testParseResource() {
		PlanParser p = new PlanParser(UUID::randomUUID);
		URI uri = URI.create("src/test/java/resources/test.plan");
		Resource r = new UriResource(Paths.get(uri.getPath()));
		PerfPlan pp = p.parseResource(r);
		assertEquals("simulation 1", pp.getSaladPlan().getPlan().getChildren().get(0).getName());
	}

	private static class UriResource implements Resource {
        private final Path resource;

        UriResource(Path resource) {
            this.resource = resource;
        }

        @Override
        public URI getUri() {
            return resource.toUri();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return Files.newInputStream(resource);
        }
    }
}