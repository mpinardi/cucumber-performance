package cucumber.perf.runtime.formatter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import cucumber.api.Result;
import cucumber.api.formatter.StrictAware;
import cucumber.perf.api.event.ConfigStatistics;
import cucumber.perf.api.event.EventBus;
import cucumber.perf.api.event.EventHandler;
import cucumber.perf.api.event.EventListener;
import cucumber.perf.api.event.EventPublisher;
import cucumber.perf.api.event.SimulationFinished;
import cucumber.perf.api.event.StatisticsFinished;
import cucumber.perf.api.event.StatisticsStarted;
import cucumber.perf.api.formatter.EventWriter;
import cucumber.perf.api.formatter.Statistics;
import cucumber.perf.api.result.BaseResult;
import cucumber.perf.api.result.GroupResult;
import cucumber.perf.api.result.ScenarioResult;
import cucumber.perf.api.result.SimulationResult;
import cucumber.perf.api.result.StepResult;

public class StatisticsFormatter implements EventListener,EventWriter,StrictAware {
	private int maxPoints = 20;
	public final static String CONFIG_MAXPOINTS = "maxPoints";
	private Statistics stats;
	private HashMap<String,List<GroupResult>> results = new HashMap<String,List<GroupResult>>();
	private boolean isStrict = true;
	private EventBus eventBus = null;
	@SuppressWarnings("unused")
	private EventPublisher publisher = null;
	
	private EventHandler<SimulationFinished> simulationFinishedHandler = new EventHandler<SimulationFinished>() {
	        @Override
	        public void receive(SimulationFinished event) {
	            process(event.getResult());
	        }
	};
	
	private EventHandler<ConfigStatistics> configStatisticsHandler = new EventHandler<ConfigStatistics>() {
        @Override
        public void receive(ConfigStatistics event) {
            config(event.setting,event.value);
        }
	};
	    
	public StatisticsFormatter()
	{
	}

	@Override
	public void setEventPublisher(EventPublisher publisher) {
		this.publisher = publisher;
		publisher.registerHandlerFor(SimulationFinished.class, simulationFinishedHandler);
		publisher.registerHandlerFor(ConfigStatistics.class, configStatisticsHandler);
	}

	@Override
	public void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}
	
	@Override
	public void setStrict(boolean strict) {
		this.isStrict =strict;
	}
	
	private void config(String setting, Object value) {
		if (setting==CONFIG_MAXPOINTS)
		{
			this.maxPoints = (int)value;
		}
	}
	
	private void process(SimulationResult result)
	{
		stats = new Statistics();
		if (result != null)
		{
			stats.setSimulation(new BaseResult(result.getName(),result.getResult(),result.getStart(),result.getStop()));
			if (result.getChildResults() != null && !result.getChildResults().isEmpty())
			{
				eventBus.send(new StatisticsStarted(eventBus.getTime(),eventBus.getTimeMillis()));
				for (GroupResult o : result.getChildResults()) {
					if (this.results.containsKey(o.getName())) {
						this.results.get(o.getName()).add(o);
					} else {
						this.results.put(o.getName(), new ArrayList<GroupResult>(Arrays.asList(o)));
					}
				}
				calculate(isStrict);
				stats.setErrors(this.getErrors());
				eventBus.send(new StatisticsFinished(eventBus.getTime(),eventBus.getTimeMillis(),stats));
			}
		}
	}
	
	private void calculate(boolean isStrict)
	{
		for (Entry<String,List<GroupResult>> entry: results.entrySet())
		{
			List<HashMap<String,GroupResult>> l = new ArrayList<HashMap<String,GroupResult>>();
			stats.getChartPoints().put(entry.getKey(), l);
			LocalDateTime startPeriod = entry.getValue().get(0).getStart();
			Long period = getPeriod(Duration.between(entry.getValue().get(0).getStart(), entry.getValue().get(entry.getValue().size()-1).getStart()), maxPoints);
			LocalDateTime nextPeriod = this.getEnd(startPeriod, period);
			GroupResult pointCnt = null;
			GroupResult pointMin = new GroupResult(entry.getValue().get(0));
			GroupResult pointMax = new GroupResult(entry.getValue().get(0));
			GroupResult pointSum = new GroupResult(entry.getValue().get(0));
			GroupResult sum = new GroupResult(entry.getValue().get(0));
			GroupResult min = new GroupResult(entry.getValue().get(0));
			GroupResult max = new GroupResult(entry.getValue().get(0));
			GroupResult cnt = new GroupResult(entry.getValue().get(0));

			boolean first = true;
			int count = 0;
			for (GroupResult f : entry.getValue())
			{
				if (!first)
				{
					if (f.getStop().isAfter(nextPeriod))
					{
						GroupResult pointAvg = new GroupResult(pointSum);
						pointAvg.setResult(new Result(pointAvg.getResult().getStatus(),count > 0 ?pointAvg.getResultDuration()/pointCnt.getResultDuration() :pointAvg.getResultDuration() ,pointAvg.getResult().getError()));
						for (int sci = 0; sci < pointAvg.getChildResults().size(); sci++)
						{
								pointAvg.getChildResults().get(sci).setResult(new Result(pointSum.getChildResults().get(sci).getResult().getStatus(),count>0?pointSum.getChildResults().get(sci).getResultDuration()/pointCnt.getChildResults().get(sci).getResultDuration():pointSum.getChildResults().get(sci).getResultDuration(),pointAvg.getChildResults().get(sci).getResult().getError()));
								for (int sti = 0; sti < pointAvg.getChildResults().get(sci).getChildResults().size(); sti++)
								{
									if (pointAvg.getChildResults().get(sci).getChildResults().get(sti).getResult().getDuration()!=null)
									{
										pointAvg.getChildResults().get(sci).getChildResults().get(sti).setResult(new Result(pointAvg.getChildResults().get(sci).getChildResults().get(sti).getResult().getStatus(),count>0?pointAvg.getChildResults().get(sci).getChildResults().get(sti).getResultDuration()/pointCnt.getChildResults().get(sci).getChildResults().get(sti).getResultDuration():pointAvg.getChildResults().get(sci).getChildResults().get(sti).getResultDuration(),pointAvg.getChildResults().get(sci).getChildResults().get(sti).getResult().getError()));
									}
								}
						}
						stats.getChartPoints().get(entry.getKey()).add(new HashMap<String,GroupResult>());
						stats.getChartPoints().get(entry.getKey()).get(stats.getChartPoints().get(entry.getKey()).size()-1).put("cnt", pointCnt);
						stats.getChartPoints().get(entry.getKey()).get(stats.getChartPoints().get(entry.getKey()).size()-1).put("avg", pointAvg);
						stats.getChartPoints().get(entry.getKey()).get(stats.getChartPoints().get(entry.getKey()).size()-1).put("min", pointMin);
						stats.getChartPoints().get(entry.getKey()).get(stats.getChartPoints().get(entry.getKey()).size()-1).put("max", pointMax);
						nextPeriod = this.getEnd(nextPeriod, period);
						pointMin = new GroupResult(f);
						pointMax = new GroupResult(f);
						pointSum = new GroupResult(f);
						pointCnt = new GroupResult(f);
						clearResultDuration(pointCnt,(long)1);
						count = 0;
					}
					count++;
					if ((isStrict && f.getResult().isOk(isStrict)) || !isStrict) {
						sum.setResult(new Result(f.getResult().getStatus(),sum.getResultDuration()+f.getResultDuration(),f.getResult().getError()));
						cnt.setResult(new Result(f.getResult().getStatus(),cnt.getResultDuration()+1,f.getResult().getError()));
						pointSum.setResult(new Result(f.getResult().getStatus(),count>1 ? pointSum.getResultDuration()+f.getResultDuration():f.getResultDuration(),f.getResult().getError()));
						pointCnt.setResult(new Result(f.getResult().getStatus(),pointCnt.getResultDuration()+1,f.getResult().getError()));
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
							try {
								sum.getChildResults().get(sci).setResult(new Result(sc.getResult().getStatus(),sum.getChildResults().get(sci).getResultDuration()+sc.getResultDuration(),sc.getResult().getError()));
								cnt.getChildResults().get(sci).setResult(new Result(sc.getResult().getStatus(),cnt.getChildResults().get(sci).getResultDuration()+1,sc.getResult().getError()));
								pointSum.getChildResults().get(sci).setResult(new Result(sc.getResult().getStatus(),count>1?pointSum.getChildResults().get(sci).getResultDuration()+sc.getResultDuration():sc.getResultDuration(),sc.getResult().getError()));
								pointCnt.getChildResults().get(sci).setResult(new Result(sc.getResult().getStatus(),pointCnt.getChildResults().get(sci).getResultDuration()+1,sc.getResult().getError()));
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
							catch (Exception e)
							{
								//e.printStackTrace();
								//Range exception
							}
						}
						for (int sti = 0; sti < sc.getChildResults().size(); sti++)
						{
							try {
								StepResult stp = sc.getChildResults().get(sti);
								if (sum.getChildResults().get(sci).getChildResults().get(sti).getResultDuration()!=null&&((isStrict && stp.getResult().isOk(isStrict)) || !isStrict)){
									sum.getChildResults().get(sci).getChildResults().get(sti).setResult(new Result(stp.getResult().getStatus(),sum.getChildResults().get(sci).getChildResults().get(sti).getResultDuration()+stp.getResultDuration(),stp.getResult().getError()));
									cnt.getChildResults().get(sci).getChildResults().get(sti).setResult(new Result(stp.getResult().getStatus(),cnt.getChildResults().get(sci).getChildResults().get(sti).getResultDuration()+1,stp.getResult().getError()));
									pointSum.getChildResults().get(sci).getChildResults().get(sti).setResult(new Result(stp.getResult().getStatus(),count>1 ?pointSum.getChildResults().get(sci).getChildResults().get(sti).getResultDuration()+stp.getResultDuration():stp.getResultDuration(),stp.getResult().getError()));
									pointCnt.getChildResults().get(sci).getChildResults().get(sti).setResult(new Result(stp.getResult().getStatus(),pointCnt.getChildResults().get(sci).getChildResults().get(sti).getResultDuration()+1,stp.getResult().getError()));
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
							catch (Exception e)
							{
								//e.printStackTrace();
								//Range exception
							}
						}
					}
				}
				else if (first)
				{
					first = false;
					count++;
					clearResultDuration(cnt,(long)1);
					pointCnt = new GroupResult(cnt);
				}
				else
				{		
				}
			}
			GroupResult avg = new GroupResult(sum);
			avg.setResult(new Result(avg.getResult().getStatus(),avg.getResultDuration()/cnt.getResultDuration(),avg.getResult().getError()));
			for (int sci = 0; sci < sum.getChildResults().size(); sci++)
			{
				avg.getChildResults().get(sci).setResult(new Result(sum.getChildResults().get(sci).getResult().getStatus(),sum.getChildResults().get(sci).getResultDuration()/cnt.getChildResults().get(sci).getResultDuration(),avg.getChildResults().get(sci).getResult().getError()));
				for (int sti = 0; sti < sum.getChildResults().get(sci).getChildResults().size(); sti++)
				{
					if (avg.getChildResults().get(sci).getChildResults().get(sti).getResult().getDuration()!=null)
					{
					avg.getChildResults().get(sci).getChildResults().get(sti).setResult(new Result(avg.getChildResults().get(sci).getChildResults().get(sti).getResult().getStatus(),avg.getChildResults().get(sci).getChildResults().get(sti).getResultDuration()/cnt.getChildResults().get(sci).getChildResults().get(sti).getResultDuration(),avg.getChildResults().get(sci).getChildResults().get(sti).getResult().getError()));
					}
				}
			
			}
			this.stats.getAvg().put(entry.getKey(), avg);
			this.stats.getCnt().put(entry.getKey(), cnt);
			this.stats.getMin().put(entry.getKey(), min);
			this.stats.getMax().put(entry.getKey(), max);
		}
	}
	
	private void clearResultDuration(GroupResult group,long value) {
		group.setResult(new Result(group.getResult().getStatus(),value,group.getResult().getError()));
		for (ScenarioResult sc : group.getChildResults()){
			sc.setResult(new Result(sc.getResult().getStatus(),value,sc.getResult().getError()));
			for (StepResult st : sc.getChildResults())
				st.setResult(new Result(st.getResult().getStatus(),value,st.getResult().getError()));
		}
	}

	private long getPeriod(Duration time, int times) {
		return (time.getSeconds()*1000) / times;
	}
	
	private LocalDateTime getEnd(LocalDateTime start, long timeMillis) {
		LocalDateTime endt = LocalDateTime.from(start).plus(timeMillis, ChronoUnit.MILLIS);
		return endt;
	}
	
	private HashMap<String,HashMap<String,Throwable>> getErrors()
	{
		HashMap<String,HashMap<String,Throwable>> map = new HashMap<String,HashMap<String,Throwable>>();
		for (Entry<String,List<GroupResult>> entry: results.entrySet())
		{
			for (GroupResult f : entry.getValue())
			{
				if (f.getResult().is(Result.Type.FAILED))
				{
					HashMap<String,Throwable> sErrs = new HashMap<String,Throwable>();
					if (map.containsKey(f.getName()))
					{
						sErrs = map.get(f.getName());
					}
					for (ScenarioResult sr : f.getChildResults())
					{
						if (sr.getResult().is(Result.Type.FAILED))
						{
							sErrs.put(sr.getName(), sr.getError());
						}
					}
					map.put(f.getName(), sErrs);
				}
			}
		}
		return map;
	}
}
