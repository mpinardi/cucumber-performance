package cucumber.perf.api.result;

public class TestStep implements cucumber.api.TestStep{
	
	private String codeLocation;
	
	public TestStep() {
	}
	
	public TestStep(String codeLocation) {
		this.codeLocation = codeLocation;
	}
	
	public TestStep(cucumber.api.TestStep testStep) {
		this.codeLocation = testStep.getCodeLocation();
	}
	
	@Override
	public String getCodeLocation() {
		return codeLocation;
	}
}
