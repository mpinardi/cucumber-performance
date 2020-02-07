package cucumber.perf.api.plan;

import io.cucumber.core.gherkin.FeatureParserException;

import java.net.URI;
import java.util.UUID;
import java.util.function.Supplier;

import cucumber.perf.salad.AstBuilder;
import cucumber.perf.salad.Parser;
import cucumber.perf.salad.ParserException;
import cucumber.perf.salad.TokenMatcher;
import cucumber.perf.salad.ast.SaladDocument;

public final class PerfPlanParser implements cucumber.perf.salad.PlanParser {
    
	private static PerfPlan parseSalad(URI path, String source) {
        try {
            Parser<SaladDocument> parser = new Parser<>(new AstBuilder());
            TokenMatcher matcher = new TokenMatcher();
            SaladDocument saladDocument = parser.parse(source, matcher);
            if(saladDocument.getPlan() == null){
                //return Optional.empty();
            	return null;
            }
            PerfPlan plan = new PerfPlan(saladDocument, path, source);
            //return Optional.of(plan);
            return plan;
        } catch (ParserException e) {
            throw new FeatureParserException("Failed to parse resource at: " + path.getPath().toString(), e);
        }
    }

    @Override
    public PerfPlan parse(URI path, String source, Supplier<UUID> idGenerator) {
        return parseSalad(path, source);
    }

    @Override
    public String version() {
        return "5";
    }
}
