package cucumber.perf.api;

import io.cucumber.core.gherkin.Argument;
import io.cucumber.core.gherkin.Step;
import io.cucumber.core.gherkin.StepType;

public class PerfStep implements Step {

    private final Step step;
    private final String text;
    
    PerfStep(Step step,String text){
    	this.text = text;
		this.step = step;
    }

	@Override
	public int getLine() {
		return step.getLine();
	}

	@Override
	public Argument getArgument() {
		return step.getArgument();
	}

	@Override
	public String getKeyWord() {
		return step.getKeyWord();
	}

	@Override
	public StepType getType() {
		return step.getType();
	}

	@Override
	public String getPreviousGivenWhenThenKeyWord() {
		return step.getPreviousGivenWhenThenKeyWord();
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public String getId() {
		return step.getId();
	}
}
