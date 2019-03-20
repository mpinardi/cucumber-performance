package cucumber.api.perf.result;

import java.time.Duration;
import java.time.LocalDateTime;

import cucumber.api.Result;
import cucumber.api.Result.Type;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventListener;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestRunStarted;

public class FeatureResultListener implements EventListener {
	private String featureName = "";
	private FeatureResult result = null;
	private LocalDateTime start = LocalDateTime.now();
	
	private final EventHandler<TestRunStarted> testRunStartedHandler = new EventHandler<TestRunStarted>() {
		@Override
		public void receive(TestRunStarted event) {
			start = LocalDateTime.now();
		}
	};
	
	private final EventHandler<TestRunFinished> testRunFinishedHandler = new EventHandler<TestRunFinished>() {
		@Override
		public void receive(TestRunFinished event) {
			result = new FeatureResult(featureName,new Result(Type.PASSED, Duration.between(start,LocalDateTime.now()).toNanos(), null),start,LocalDateTime.now());
		}
	};
	
    public FeatureResultListener(String featureName) {
		this.featureName = featureName;
	}

	@Override
	public void setEventPublisher(EventPublisher publisher) {
		publisher.registerHandlerFor(TestRunStarted.class, testRunStartedHandler);
		publisher.registerHandlerFor(TestRunFinished.class, testRunFinishedHandler);
	}
	
	public String getFeatureName()
	{
		return featureName;
	}

	public void startFeature() {
		start = LocalDateTime.now();     
	}

	public FeatureResult getResult() {
		return result;
	}

	public void setResult(FeatureResult result) {
		this.result = result;
	}
}