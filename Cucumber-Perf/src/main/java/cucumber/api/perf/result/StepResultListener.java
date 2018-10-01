package cucumber.api.perf.result;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import cucumber.api.PickleStepTestStep;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventListener;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestStepStarted;
import cucumber.api.event.TestStepFinished;

public class StepResultListener implements EventListener {
    static final String UNDEFINED_MESSAGE = "There are undefined steps";
    private LocalDateTime start = null;
    private List<StepResult> results = new ArrayList<StepResult>();
    private String name;
 
    private final EventHandler<TestStepFinished> testStepFinishedHandler = new EventHandler<TestStepFinished>() {
		@Override
		public void receive(TestStepFinished event) {
			results.add(new StepResult(name,event.result,start,LocalDateTime.now()));
		}
    };
    
   private final EventHandler<TestStepStarted> testStepStartedHandler = new EventHandler<TestStepStarted>() {
		@Override
		public void receive(TestStepStarted event) {
			start = LocalDateTime.now();
			name = ((PickleStepTestStep)event.testStep).getStepText();
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