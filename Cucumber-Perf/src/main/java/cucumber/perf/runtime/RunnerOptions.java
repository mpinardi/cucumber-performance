package cucumber.perf.runtime;

import java.util.ArrayList;
import java.util.List;

import cucumber.perf.api.PerfGroup;
import cucumber.perf.salad.ast.Slice;
import cucumber.runtime.model.CucumberFeature;

public class RunnerOptions {
	private String groupText;
	private Slice slice;
	private List<CucumberFeature> features = new ArrayList<CucumberFeature>();
	
	public RunnerOptions(PerfGroup pg)
	{
		this.groupText = pg.getText();
		this.slice = pg.getSlice();
		this.features = pg.getFeatures();
	}
	
	public RunnerOptions(String groupText,List<CucumberFeature> features,Slice slice)
	{
		this.groupText = groupText;
		this.slice = slice;
		this.features = features;
	}
	
	public String getGroupText() {
		return groupText;
	}

	public void setGroupText(String groupText) {
		this.groupText = groupText;
	}

	public Slice getSlice() {
		return slice;
	}

	public void setSlice(Slice slice) {
		this.slice = slice;
	}

	public List<CucumberFeature> getFeatures() {
		return features;
	}

	public void setFeatures(List<CucumberFeature> features) {
		this.features = features;
	}
	
	
}