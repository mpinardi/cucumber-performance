package cucumber.perf.api;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cucumber.perf.salad.AstBuilder;
import cucumber.perf.salad.Parser;
import cucumber.perf.salad.ParserException;
import cucumber.perf.salad.TokenMatcher;
import cucumber.perf.salad.ast.SaladDocument;
import cucumber.perf.salad.ast.SimulationDefinition;
import cucumber.runtime.CucumberException;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.util.Encoding;

public class PlanBuilder {
	private static final Charset UTF8 = Charset.forName("UTF-8");
	private final List<PerfPlan> perfPlans;
	private final char fileSeparatorChar;
	private final MessageDigest md5;
	private final Map<String, String> pathsByChecksum = new HashMap<String, String>();

	public PlanBuilder(List<PerfPlan> perfPlans) {
		this(perfPlans, File.separatorChar);
	}

	PlanBuilder(List<PerfPlan> perfPlans, char fileSeparatorChar) {
		this.perfPlans = perfPlans;
		this.fileSeparatorChar = fileSeparatorChar;
		try {
			this.md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new CucumberException(e);
		}
	}

	public void parse(Resource resource) {
		String gherkin = read(resource);

		String checksum = checksum(gherkin);
		String path = pathsByChecksum.get(checksum);
		if (path != null) {
			return;
		}
		pathsByChecksum.put(checksum, resource.getPath().toString());

		Parser<SaladDocument> parser = new Parser<SaladDocument>(new AstBuilder());
		// new Parser<SaladDocument>(new AstBuilder());
		TokenMatcher matcher = new TokenMatcher();
		try {
			SaladDocument saladDocument = parser.parse(gherkin, matcher);
			PerfPlan perfPlan = new PerfPlan(saladDocument, convertFileSeparatorToForwardSlash(resource.getPath().toString()),
					gherkin);
			perfPlans.add(perfPlan);
		} catch (ParserException e) {
			throw new CucumberException(e);
		}
	}

	private String convertFileSeparatorToForwardSlash(String path) {
		return path.replace(fileSeparatorChar, '/');
	}

	private String checksum(String gherkin) {
		return new BigInteger(1, md5.digest(gherkin.getBytes(UTF8))).toString(16);
	}

	public String read(Resource resource) {
		try {
			String source = Encoding.readFile(resource);
			return source;
		} catch (IOException e) {
			throw new CucumberException("Failed to read resource:" + resource.getPath(), e);
		}
	}

	public static List<PerfPlan> LoadPlans(Class<?> clazz, List<String> planPaths) {
		ClassLoader classLoader = clazz.getClassLoader();
		ResourceLoader resourceLoader = new MultiLoader(classLoader);
		return PerfPlan.load(resourceLoader, planPaths,System.out);
	}

	public static List<PerfPlan> FindPlan(String prefix, List<PerfPlan> plans) {
		List<PerfPlan> result = new ArrayList<PerfPlan>();
		for (PerfPlan f : plans) {
			if (f.getSaladPlan().getPlan().getName().toLowerCase().startsWith(prefix)) {
				result.add(f);
			}
		}
		return result;
	}

	public static List<SimulationDefinition> FindSimulations(String prefix, String plan, List<PerfPlan> plans) {
		List<SimulationDefinition> result = new ArrayList<SimulationDefinition>();
		for (PerfPlan p : plans) {
			if (p.getSaladPlan().getPlan().getName().equalsIgnoreCase(plan)) {
				for (SimulationDefinition s : p.getSaladPlan().getPlan().getChildren()) {
					if (s.getName().startsWith(prefix)) {
						result.add(s);
					}
				}
			}
		}
		return result;
	}

	public static List<List<SimulationDefinition>> GetSimulations(List<PerfPlan> plans) {

		List<List<SimulationDefinition>> result = new ArrayList<List<SimulationDefinition>>();
		for (PerfPlan p : plans) {
			List<SimulationDefinition> sc = new ArrayList<SimulationDefinition>();
			for (SimulationDefinition s : p.getSaladPlan().getPlan().getChildren()) {
				sc.add(s);
			}
			result.add(sc);
		}
		return result;
	}
}
