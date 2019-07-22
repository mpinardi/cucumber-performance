package cucumber.perf.api.result;

import java.time.Duration;
import java.time.LocalDateTime;

import cucumber.api.Result;
import cucumber.api.Result.Type;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventListener;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestRunStarted;

public class GroupResultListener implements EventListener {
	private String groupName = "";
	private GroupResult result = null;
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
			result = new GroupResult(groupName,new Result(Type.PASSED, Duration.between(start,LocalDateTime.now()).toNanos(), null),start,LocalDateTime.now());
		}
	};
	
    public GroupResultListener() {
	}

	@Override
	public void setEventPublisher(EventPublisher publisher) {
		publisher.registerHandlerFor(TestRunStarted.class, testRunStartedHandler);
		publisher.registerHandlerFor(TestRunFinished.class, testRunFinishedHandler);
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;    
	}

	public GroupResult getResult() {
		return result;
	}
}