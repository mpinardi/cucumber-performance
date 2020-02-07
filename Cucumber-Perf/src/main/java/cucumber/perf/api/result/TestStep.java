package cucumber.perf.api.result;

public class TestStep implements io.cucumber.plugin.event.TestStep{
	
	private String codeLocation;
	
	public TestStep() {
	}
	
	public TestStep(String codeLocation) {
		this.codeLocation = codeLocation;
	}
	
	public TestStep(io.cucumber.plugin.event.TestStep testStep) {
		this.codeLocation = testStep.getCodeLocation();
	}
	
	public TestStep(TestStep testStep) {
		this.codeLocation = testStep.getCodeLocation();
	}
	
	@Override
	public String getCodeLocation() {
		return codeLocation;
	}
}
