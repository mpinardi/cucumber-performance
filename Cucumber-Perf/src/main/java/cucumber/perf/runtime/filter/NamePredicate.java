package cucumber.perf.runtime.filter;

import java.util.List;
import java.util.regex.Pattern;

import cucumber.perf.salad.ast.Plan;
import cucumber.perf.salad.ast.Simulation;
import cucumber.perf.salad.ast.SimulationPeriod;
import io.cucumber.core.internal.gherkin.ast.Node;

public class NamePredicate implements Predicate {
    private List<Pattern> patterns;

    public NamePredicate(List<Pattern> patterns) {
        this.patterns = patterns;
    }

    @Override
    public boolean apply(Node n) {
    	String name = "";
    	if (n instanceof Simulation)
    	{
    		name = ((Simulation) n).getName();
    	}
    	else if (n instanceof SimulationPeriod)
    	{
    		name = ((SimulationPeriod) n).getName();
    	}
     	else if (n instanceof Plan)
    	{
    		name = ((Plan) n).getName();
    	}
        for (Pattern pattern : patterns) {
            if (pattern.matcher(name).find()) {
                return true;
            }
        }
        return false;
    }

}
