package cucumber.perf.api.result;

import java.time.LocalDateTime;

import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;

public class TestCaseResultListener implements EventListener {
    static final String UNDEFINED_MESSAGE = "There are undefined steps";
	private LocalDateTime start = LocalDateTime.now();
	private ScenarioResult result = null;
    private String name;

    private final EventHandler<TestCaseFinished> testCaseFinishedHandler = new EventHandler<TestCaseFinished>() {

        @Override
        public void receive(TestCaseFinished event) {
            result = new ScenarioResult(name,new TestCase(event.getTestCase()),event.getResult(),start,LocalDateTime.now());
        }
    };
    
    private final EventHandler<TestCaseStarted> testCaseStartedHandler = new EventHandler<TestCaseStarted>() {

		@Override
		public void receive(TestCaseStarted event) {
			start = LocalDateTime.now();
			name = event.getTestCase().getName();
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