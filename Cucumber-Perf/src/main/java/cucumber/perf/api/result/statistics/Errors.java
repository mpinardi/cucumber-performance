package cucumber.perf.api.result.statistics;

import java.time.LocalDateTime;

public class Errors {
	
	private String step = "";
	private int count = 0;
	private Throwable throwable = null;
	private LocalDateTime first = LocalDateTime.now();
	private LocalDateTime last = LocalDateTime.now();
	
	public Errors(String step, int count, Throwable throwable, LocalDateTime first) {
		this.step = step;
		this.count = count;
		this.throwable = throwable;
		this.first = first;
		this.last = first;
	}
	
	public String getKey()
	{
		return step+":"+throwable.getMessage();
	}
	
	public int getCount() {
		return count;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public Throwable getThrowable() {
		return throwable;
	}
	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
	}

	public LocalDateTime getFirst() {
		return first;
	}

	public void setFirst(LocalDateTime first) {
		this.first = first;
	}

	public LocalDateTime getLast() {
		return last;
	}

	public void setLast(LocalDateTime last) {
		this.last = last;
	}

	public void update(LocalDateTime date) {
		if (date.isBefore(first))
			first = date;
		if (date.isAfter(last))
			last = date;
		incrementCount();
	}

	public void incrementCount() {
		this.count++;
	}

	public String getStep() {
		return step;
	}

	public void setStep(String step) {
		this.step = step;
	}
}
