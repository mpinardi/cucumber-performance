package cucumber.api.perf.formatter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import cucumber.api.Result;
import cucumber.api.perf.result.FeatureResult;
import cucumber.api.perf.result.ScenarioResult;
import cucumber.api.perf.result.StepResult;

public class Statistics {
	private int maxPoints;
	private HashMap<String,List<FeatureResult>> results = new HashMap<String,List<FeatureResult>>();
	//HashMap<FeatureName,FeatureResult>()
	private HashMap<String,FeatureResult> min = new HashMap<String,FeatureResult>();
	private HashMap<String,FeatureResult> max = new HashMap<String,FeatureResult>();
	private HashMap<String,FeatureResult> avg = new HashMap<String,FeatureResult>();
	//HashMap<FeatureName,List<HashMap<StatType,FeatureResult>>>
	private HashMap<String,List<HashMap<String,FeatureResult>>> chartPoints = new HashMap<String,List<HashMap<String,FeatureResult>>>();
	
	public Statistics(List<FeatureResult> results,boolean isStrict)
	{
		this(results,20,isStrict);
	}
	
	public Statistics(List<FeatureResult> results,int maxPoints,boolean isStrict)
	{
		this.maxPoints = maxPoints;
		if (!results.isEmpty())
		{
		for (FeatureResult o : results) {
			if (this.results.containsKey(o.getName())) {
				this.results.get(o.getName()).add(o);
			} else {
				this.results.put(o.getName(), new ArrayList<FeatureResult>(Arrays.asList(o)));
			}
		}
		calculate(isStrict);
		}
	}
	
	private void calculate(boolean isStrict)
	{
		for (Entry<String,List<FeatureResult>> entry: results.entrySet())
		{
			List<HashMap<String,FeatureResult>> l = new ArrayList<HashMap<String,FeatureResult>>();
			l.add(new HashMap<String,FeatureResult>());
			chartPoints.put(entry.getKey(), l);
			LocalDateTime startPeriod = entry.getValue().get(0).getStart();
			Long period = getPeriod(Duration.between(entry.getValue().get(0).getStart(), entry.getValue().get(entry.getValue().size()-1).getStart()), maxPoints);
			LocalDateTime nextPeriod = this.getEnd(startPeriod, period);
			FeatureResult pointMin = new FeatureResult(entry.getValue().get(0));
			FeatureResult pointMax = new FeatureResult(entry.getValue().get(0));
			FeatureResult pointSum = new FeatureResult(entry.getValue().get(0));
			FeatureResult sum = new FeatureResult(entry.getValue().get(0));
			FeatureResult min = new FeatureResult(entry.getValue().get(0));
			FeatureResult max = new FeatureResult(entry.getValue().get(0));
			boolean first = true;
			int count = 0;
			for (FeatureResult f : entry.getValue())
			{
				if (!first)
				{
					if (f.getStop().isAfter(nextPeriod))
					{
						count = 0;
						FeatureResult pointAvg = new FeatureResult(pointSum);
						pointAvg.setResult(new Result(pointAvg.getResult().getStatus(),count > 0 ?pointAvg.getResultDuration()/count :pointAvg.getResultDuration() ,pointAvg.getResult().getError()));
						for (int sci = 0; sci < sum.getChildResults().size(); sci++)
						{
							pointAvg.getChildResults().get(sci).setResult(new Result(pointSum.getChildResults().get(sci).getResult().getStatus(),count>0?pointSum.getChildResults().get(sci).getResultDuration()/count:pointSum.getChildResults().get(sci).getResultDuration(),pointAvg.getChildResults().get(sci).getResult().getError()));
							for (int sti = 0; sti < sum.getChildResults().get(sci).getChildResults().size(); sti++)
							{
								if (pointAvg.getChildResults().get(sci).getChildResults().get(sti).getResult().getDuration()!=null)
								{
									pointAvg.getChildResults().get(sci).getChildResults().get(sti).setResult(new Result(pointAvg.getChildResults().get(sci).getChildResults().get(sti).getResult().getStatus(),count>0?pointAvg.getChildResults().get(sci).getChildResults().get(sti).getResultDuration()/count:pointAvg.getChildResults().get(sci).getChildResults().get(sti).getResultDuration(),pointAvg.getChildResults().get(sci).getChildResults().get(sti).getResult().getError()));
								}
							}
						}
						chartPoints.get(entry.getKey()).get(chartPoints.get(entry.getKey()).size()-1).put("avg", pointAvg);
						chartPoints.get(entry.getKey()).get(chartPoints.get(entry.getKey()).size()-1).put("min", pointMin);
						chartPoints.get(entry.getKey()).get(chartPoints.get(entry.getKey()).size()-1).put("max", pointMax);
						nextPeriod = this.getEnd(startPeriod, period);
						pointMin = new FeatureResult(f);
						pointMax = new FeatureResult(f);
						pointSum = new FeatureResult(f);
						chartPoints.get(entry.getKey()).add(new HashMap<String,FeatureResult>());
					}
					count++;
					Result fSum = sum.getResult();
					if ((isStrict && f.getResult().isOk(isStrict)) || !isStrict) {
						sum.setResult(new Result(f.getResult().getStatus(),fSum.getDuration()+f.getResultDuration(),f.getResult().getError()));
						pointSum.setResult(new Result(f.getResult().getStatus(),fSum.getDuration()+f.getResultDuration(),f.getResult().getError()));
						if (f.getResultDuration()>max.getResultDuration())
						{
							max.setResult(new Result(f.getResult().getStatus(),f.getResultDuration(),f.getResult().getError()));
							pointMax.setResult(new Result(f.getResult().getStatus(),f.getResultDuration(),f.getResult().getError()));

						}
						else if (f.getResultDuration()<min.getResultDuration())
						{
							min.setResult(new Result(f.getResult().getStatus(),f.getResultDuration(),f.getResult().getError()));
							pointMin.setResult(new Result(f.getResult().getStatus(),f.getResultDuration(),f.getResult().getError()));
						}
					}
					
					for (int sci = 0; sci < f.getChildResults().size(); sci++)
					{
						ScenarioResult sc = f.getChildResults().get(sci);
						if ((isStrict && sc.getResult().isOk(isStrict)) || !isStrict) {
							sum.getChildResults().get(sci).setResult(new Result(sc.getResult().getStatus(),sum.getChildResults().get(sci).getResultDuration()+sc.getResultDuration(),sc.getResult().getError()));
							pointSum.getChildResults().get(sci).setResult(new Result(sc.getResult().getStatus(),sum.getChildResults().get(sci).getResultDuration()+sc.getResultDuration(),sc.getResult().getError()));
							if (sc.getResultDuration()>max.getChildResults().get(sci).getResultDuration())
							{
								max.getChildResults().get(sci).setResult(new Result(sc.getResult().getStatus(),sc.getResultDuration(),sc.getResult().getError()));
								pointMax.getChildResults().get(sci).setResult(new Result(sc.getResult().getStatus(),sc.getResultDuration(),sc.getResult().getError()));
							}
							else if (sc.getResultDuration()<min.getChildResults().get(sci).getResultDuration())
							{
								min.getChildResults().get(sci).setResult(new Result(sc.getResult().getStatus(),sc.getResultDuration(),sc.getResult().getError()));
								pointMin.getChildResults().get(sci).setResult(new Result(sc.getResult().getStatus(),sc.getResultDuration(),sc.getResult().getError()));
							}
						}
						for (int sti = 0; sti < sc.getChildResults().size(); sti++)
						{
							StepResult stp = sc.getChildResults().get(sti);
							if (sum.getChildResults().get(sci).getChildResults().get(sti).getResultDuration()!=null&&((isStrict && stp.getResult().isOk(isStrict)) || !isStrict)){
								sum.getChildResults().get(sci).getChildResults().get(sti).setResult(new Result(stp.getResult().getStatus(),sum.getChildResults().get(sci).getChildResults().get(sti).getResultDuration()+stp.getResultDuration(),stp.getResult().getError()));
								pointSum.getChildResults().get(sci).getChildResults().get(sti).setResult(new Result(stp.getResult().getStatus(),sum.getChildResults().get(sci).getChildResults().get(sti).getResultDuration()+stp.getResultDuration(),stp.getResult().getError()));
							}
							if (stp.getResultDuration()!=null && stp.getResultDuration()>max.getChildResults().get(sci).getChildResults().get(sti).getResultDuration())
							{
								max.getChildResults().get(sci).getChildResults().get(sti).setResult(new Result(stp.getResult().getStatus(),stp.getResultDuration(),stp.getResult().getError()));
								pointMax.getChildResults().get(sci).getChildResults().get(sti).setResult(new Result(stp.getResult().getStatus(),stp.getResultDuration(),stp.getResult().getError()));
							}
							else if (stp.getResultDuration()!=null && stp.getResultDuration()<min.getChildResults().get(sci).getChildResults().get(sti).getResultDuration())
							{
								min.getChildResults().get(sci).getChildResults().get(sti).setResult(new Result(stp.getResult().getStatus(),stp.getResultDuration(),stp.getResult().getError()));							
								pointMin.getChildResults().get(sci).getChildResults().get(sti).setResult(new Result(stp.getResult().getStatus(),stp.getResultDuration(),stp.getResult().getError()));							
							}
						}
					}
				}
				else
				{
					first = false;
				}
			}
			FeatureResult avg = new FeatureResult(sum);
			avg.setResult(new Result(avg.getResult().getStatus(),avg.getResultDuration()/entry.getValue().size(),avg.getResult().getError()));
			for (int sci = 0; sci < sum.getChildResults().size(); sci++)
			{
				avg.getChildResults().get(sci).setResult(new Result(sum.getChildResults().get(sci).getResult().getStatus(),sum.getChildResults().get(sci).getResultDuration()/entry.getValue().size(),avg.getChildResults().get(sci).getResult().getError()));
				for (int sti = 0; sti < sum.getChildResults().get(sci).getChildResults().size(); sti++)
				{
					if (avg.getChildResults().get(sci).getChildResults().get(sti).getResult().getDuration()!=null)
					{
					avg.getChildResults().get(sci).getChildResults().get(sti).setResult(new Result(avg.getChildResults().get(sci).getChildResults().get(sti).getResult().getStatus(),avg.getChildResults().get(sci).getChildResults().get(sti).getResultDuration()/entry.getValue().size(),avg.getChildResults().get(sci).getChildResults().get(sti).getResult().getError()));
				
					}
				}
			
			}
			this.avg.put(entry.getKey(), avg);
			this.min.put(entry.getKey(), min);
			this.max.put(entry.getKey(), max);
		}
	}
	
	private long getPeriod(Duration time, int times) {
		return (time.getSeconds()*1000) / times;
	}
	
	private LocalDateTime getEnd(LocalDateTime start, long timeMillis) {
		LocalDateTime endt = LocalDateTime.from(start).plus(timeMillis, ChronoUnit.MILLIS);
		return endt;
	}
	
	public HashMap<String,FeatureResult> getAvg()
	{
		return avg;
	}
	public HashMap<String, FeatureResult> getMin() {
		return min;
	}

	public HashMap<String, FeatureResult> getMax() {
		return max;
	}
	
	public  HashMap<String, List<FeatureResult>> getResults() {
		return results;
	}
	
	public  HashMap<String,List<HashMap<String,FeatureResult>>> getChartPoints() {
		return chartPoints;
	}
}
