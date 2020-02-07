package cucumber.perf.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import cucumber.perf.salad.ast.Slice;
import io.cucumber.core.gherkin.Location;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.gherkin.Step;

public class PerfPickle implements Pickle {

	private final Pickle pickle;
    private final List<Step> steps;

    public PerfPickle(Pickle pickle, Slice slice) {
    	this.pickle = pickle;
    	this.steps = this.createSteps(slice);  
    }
   
    private List<Step> createSteps(Slice slice)
    {
    	List<Step> steps = new ArrayList<Step>();
    	for (Step s: pickle.getSteps()) {
    		steps.add(new PerfStep(s,slice.replaceParameter(""+s.getText())));
    	}
    	return steps;
    }

    @Override
    public String getKeyword() {
        return pickle.getKeyword();
    }

    @Override
    public String getLanguage() {
        return pickle.getLanguage();
    }

    @Override
    public String getName() {
        return pickle.getName();
    }

    @Override
    public Location getLocation() {
        return pickle.getLocation();
    }

    @Override
    public Location getScenarioLocation() {
    	 return pickle.getScenarioLocation();
    }

    @Override
    public List<Step> getSteps() {
        return steps;
    }

    @Override
    public List<String> getTags() {
    	return pickle.getTags();
    }

    @Override
    public URI getUri() {
        return pickle.getUri();
    }

    @Override
    public String getId() {
        return pickle.getId();
    }

    @Override
    public boolean equals(Object o) {
        return pickle.equals(o);
    }

    @Override
    public int hashCode() {
        return pickle.hashCode();
    }
}
