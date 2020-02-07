package cucumber.perf.api.result;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;

public class StepResultListener implements EventListener {
    static final String UNDEFINED_MESSAGE = "There are undefined steps";
    private LocalDateTime start = null;
    private List<StepResult> results = new ArrayList<StepResult>();
    private String name;
 
    private final EventHandler<TestStepFinished> testStepFinishedHandler = new EventHandler<TestStepFinished>() {
		@Override
		public void receive(TestStepFinished event) {
			results.add(new StepResult(name,new TestStep(event.getTestStep()),event.getResult(),start,LocalDateTime.now()));
		}
    };
    
   private final EventHandler<TestStepStarted> testStepStartedHandler = new EventHandler<TestStepStarted>() {
		@Override
		public void receive(TestStepStarted event) {
			start = LocalDateTime.now();
			name = ((PickleStepTestStep)event.getTestStep()).getStep().getText();
		}
    };
    
    public StepResultListener() {
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestStepFinished.class, testStepFinishedHandler);
        publisher.registerHandlerFor(TestStepStarted.class, testStepStartedHandler);
    }
    
    public List<StepResult> getResults()
    {
    	return results;
    }
    
    
    public void reset()
    {
    	results = new ArrayList<StepResult>();
    }
}