package cucumber.perf.api.formatter;

import java.util.HashMap;
import java.util.List;

import cucumber.perf.api.result.BaseResult;
import cucumber.perf.api.result.GroupResult;

public class Statistics {
	private BaseResult simulation = null;
	private HashMap<String,GroupResult> min = new HashMap<String,GroupResult>();
	private HashMap<String,GroupResult> max = new HashMap<String,GroupResult>();
	private HashMap<String,GroupResult> avg = new HashMap<String,GroupResult>();
	private HashMap<String,GroupResult> cnt = new HashMap<String,GroupResult>();
	private HashMap<String,List<HashMap<String,GroupResult>>> chartPoints = new HashMap<String,List<HashMap<String,GroupResult>>>();
	private HashMap<String,HashMap<String,Throwable>> errors = new HashMap<String,HashMap<String,Throwable>>();
	
	public HashMap<String,GroupResult> getAvg()
	{
		return avg;
	}
	
	public HashMap<String,GroupResult> getCnt()
	{
		return cnt;
	}
	
	public HashMap<String, GroupResult> getMin() {
		return min;
	}

	public HashMap<String, GroupResult> getMax() {
		return max;
	}
	
	public  HashMap<String,List<HashMap<String,GroupResult>>> getChartPoints() {
		return chartPoints;
	}

	public HashMap<String, HashMap<String, Throwable>> getErrors() {
		return errors;
	}

	public BaseResult getSimulation() {
		return simulation;
	}
	
	public void setErrors(HashMap<String, HashMap<String, Throwable>> errors) {
		this.errors = errors;
	}

	public void setMin(HashMap<String, GroupResult> min) {
		this.min = min;
	}

	public void setMax(HashMap<String, GroupResult> max) {
		this.max = max;
	}

	public void setAvg(HashMap<String, GroupResult> avg) {
		this.avg = avg;
	}

	public void setCnt(HashMap<String, GroupResult> cnt) {
		this.cnt = cnt;
	}

	public void setChartPoints(HashMap<String, List<HashMap<String, GroupResult>>> chartPoints) {
		this.chartPoints = chartPoints;
	}
	
	public void setSimulation(BaseResult result) {
		this.simulation = result;
	}
}
