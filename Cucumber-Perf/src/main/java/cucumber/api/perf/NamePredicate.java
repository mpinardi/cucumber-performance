package cucumber.api.perf;

import gherkin.ast.Node;

import java.util.List;
import java.util.regex.Pattern;

import cucumber.api.perf.salad.ast.Plan;
import cucumber.api.perf.salad.ast.Simulation;
import cucumber.api.perf.salad.ast.SimulationPeriod;

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
