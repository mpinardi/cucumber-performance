package cucumber.perf.api.plan;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import org.junit.Test;

import cucumber.perf.api.FeatureBuilder;

public class PathPlanSupplierTest {
	
	@Test
	public void testGet() {
		Supplier<ClassLoader> classLoader = FeatureBuilder.class::getClassLoader;
		PlanParser parser = new PlanParser(UUID::randomUUID);
		List<String> list = new ArrayList<String>();
		list.add("./src/test/java/resources");
		List<PerfPlan> plans =  new PathPlanSupplier(classLoader,list , parser).get();
		assertTrue(plans.get(0).getUri().toString().contains("test/java/resources/test.plan"));
	}

}
