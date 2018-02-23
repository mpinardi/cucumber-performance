package cucumber.api.perf.result;

import java.time.LocalDateTime;

import cucumber.api.event.EventHandler;
import cucumber.api.event.EventListener;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestCaseFinished;
import cucumber.api.event.TestCaseStarted;

public class TestCaseResultListener implements EventListener {
    static final String UNDEFINED_MESSAGE = "There are undefined steps";
	private LocalDateTime start = LocalDateTime.now();
	private ScenarioResult result = null;
    private String name;
    private String uri;
    private final EventHandler<TestCaseFinished> testCaseFinishedHandler = new EventHandler<TestCaseFinished>() {

        @Override
        public void receive(TestCaseFinished event) {
            result = new ScenarioResult(name,uri,event.result,start,LocalDateTime.now());
        }
    };
    
    private final EventHandler<TestCaseStarted> testCaseStartedHandler = new EventHandler<TestCaseStarted>() {

		@Override
		public void receive(TestCaseStarted event) {
			start = LocalDateTime.now();
			name = event.testCase.getName();
			uri = event.testCase.getUri();
		}
    };
    
    public TestCaseResultListener() {
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestCaseFinished.class, testCaseFinishedHandler);
        publisher.registerHandlerFor(TestCaseStarted.class, testCaseStartedHandler);
    }
    
    public ScenarioResult getResult()
    {
    	return result;
    }
}