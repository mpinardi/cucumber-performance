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


public class PerfPlanParserTest {


	@Test
	public void testParse() {
		URI uri = URI.create("src/test/java/resources/test.plan");
		Resource r = new UriResource(Paths.get(uri.getPath()));
        String source = null;
		try {
			source = Encoding.readFile(r);
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PerfPlan p = new PerfPlanParser().parse(URI.create("src/test/java/resources/test.plan"), source, UUID::randomUUID);
		assertEquals("simulation 1", p.getSaladPlan().getPlan().getChildren().get(0).getName());
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