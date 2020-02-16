package cucumber.perf.api.result.statistics;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public class StepErrors {
	
	HashMap<String,Errors> map = new HashMap<String,Errors>();
	
	public StepErrors() {
		
	}
	
	public StepErrors(String step, LocalDateTime localDateTime, Throwable throwable) {
		map.put(step+":"+throwable.getMessage(), new Errors(step, 1, throwable, localDateTime));
	}
	
	public void putError(Errors error) {
		if (map.containsKey(error.getKey()))
			map.get(error.getKey()).update(error.getLast());
		else
			map.put(error.getKey(), error);
	}
	
	public void putError(String step,LocalDateTime when,Throwable error) {
		if (map.containsKey(step+":"+error.getMessage()))
			map.get(step+":"+error.getMessage()).update(when);
		else
			map.put(step+":"+error.getMessage(), new Errors(step, 1, error, when));
	}
	
	public Errors getError(String step, String throwableMessage) {
		return map.get(step+":"+throwableMessage);
	}
	
	public Set<Entry<String,Errors>> getErrors()
	{
		return map.entrySet();
	}
}
