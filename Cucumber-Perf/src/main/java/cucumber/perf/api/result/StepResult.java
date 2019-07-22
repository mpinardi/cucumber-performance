package cucumber.perf.api.result;

import java.time.LocalDateTime;

import cucumber.api.Result;

public class StepResult extends BaseResult {
	private TestStep testStep;
	
	public StepResult()
	{
	}
			
	public StepResult(String name,TestStep testStep,Result result,LocalDateTime start,LocalDateTime stop)
	{
		super(name,result,start,stop);
		this.setTestStep(testStep);
	}
	
	public StepResult(StepResult result)
	{
		super(result.getName(),new Result(result.getResult().getStatus(), result.getResultDuration(), result.getError()),result.getStart(),result.getStop());
		this.setTestStep(new TestStep(result.getTestStep().getCodeLocation()));
	}

	public TestStep getTestStep() {
		return testStep;
	}

	public void setTestStep(TestStep testStep) {
		this.testStep = testStep;
	}

}
