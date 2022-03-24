package cucumber.perf.api.result.statistics;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import cucumber.perf.api.result.BaseResult;
import cucumber.perf.api.result.GroupResult;
import cucumber.perf.api.result.ScenarioResult;
import cucumber.perf.api.result.StepResult;
import io.cucumber.plugin.event.Status;

public class DefaultStatistics {
	
	private Statistics statistics = new Statistics();
	private BaseResult simulation = new BaseResult();
	private HashMap<String,List<Long>> sortedResults = new HashMap<String,List<Long>>();
	private HashMap<String,List<GroupResult>> results = new HashMap<String,List<GroupResult>>();
	private LocalDateTime startPeriod = null;
	private LocalDateTime stopPeriod = null;
	
	public DefaultStatistics(BaseResult simulation,HashMap<String,List<GroupResult>> results)
	{
		this.results = results;
		this.simulation = simulation;
	}
	
	public Statistics getStatistics()
	{
		return statistics;
	}
	
	public HashMap<String,List<Long>> getSortedResults()
	{
		return this.sortedResults;
	}
	
	public Stats getStats(boolean isStrict)
	{
		return getStats(isStrict,null,null);
	}
	
	public Stats getStats(boolean isStrict,LocalDateTime startPeriod,LocalDateTime stopPeriod)
	{
		this.startPeriod=startPeriod;
		this.stopPeriod=stopPeriod;
		statistics = new Statistics();
		statistics.setSimulation(new BaseResult(simulation));
		if (startPeriod != null) {
			statistics.getSimulation().setStart(startPeriod);
		}
		if (stopPeriod != null) {
			statistics.getSimulation().setStop(stopPeriod);
		}
		Stats stats = getDefaultStats(isStrict);
		sortedResults = new HashMap<String,List<Long>>();
		for (Entry<String,List<GroupResult>> entry: results.entrySet()){
			LocalDateTime nextConcurrentPeriod = null;
			boolean groupAdded = false;
			sortedResults = new HashMap<String,List<Long>>();
			List<LocalDateTime> concurrency = new ArrayList<LocalDateTime>();
			for (GroupResult g : entry.getValue()){
				if (stopPeriod!=null && (g.getStop().isAfter(stopPeriod))) {
					break;
				}
				if (startPeriod==null || g.getStop().isAfter(startPeriod)) {
					if (!groupAdded) {
						statistics.putGroup(entry.getKey(), g);
						groupAdded=true;
					}
					if (!sortedResults.containsKey(g.getName()))
						sortedResults.put(g.getName(), new ArrayList<Long>());
					stats.putKey(g.getName());
					if( nextConcurrentPeriod==null) {
						nextConcurrentPeriod = LocalDateTime.from(g.getStart()).plus(1,ChronoUnit.SECONDS); 
					} else if(g.getStart().isAfter(nextConcurrentPeriod)) {
						while(g.getStart().isAfter(nextConcurrentPeriod) && !stats.isEmpty()) {
							nextConcurrentPeriod = LocalDateTime.from(nextConcurrentPeriod).plus(1,ChronoUnit.SECONDS);
							stats.putStatistic("cncrnt",stats.getStatistic("cncrnt", g.getName())+getConcurrent(nextConcurrentPeriod,concurrency),g.getName());
						}
					}
					if ((isStrict && g.getResult().getStatus().isOk(isStrict)) || !isStrict) {
						concurrency.add(g.getStop());
						sortedResults.get(g.getName()).add(g.getResultDuration().toNanos());
						LinkedHashMap<String,Double> gs = stats.getStatistics(g.getName());
						if(!isStrict && g.getResult().getStatus().isOk(true))
							stats.putStatistic("pass",gs.get("pass")+1,g.getName());
						else if (!isStrict)
							stats.putStatistic("fail",gs.get("fail")+1,g.getName());
						stats.putStatistic("avg",gs.get("avg")+(double)g.getResultDuration().toNanos(),g.getName());
						stats.putStatistic("cnt",gs.get("cnt")+1,g.getName());
						if (((double)g.getResultDuration().toNanos())>gs.get("max"))
							stats.putStatistic("max",(double)g.getResultDuration().toNanos(),g.getName());
						if (gs.get("min")==0.0||((double)g.getResultDuration().toNanos())<gs.get("min"))
							stats.putStatistic("min",(double)g.getResultDuration().toNanos(),g.getName());
					}
					for (int sci = 0; sci < g.getChildResults().size(); sci++)
					{
						ScenarioResult sc = g.getChildResults().get(sci);
						stats.putKey(g.getName(),sc.getName());
						if (!sortedResults.containsKey(g.getName()+"."+sc.getName()))
							sortedResults.put(g.getName()+"."+sc.getName(), new ArrayList<Long>());
						if ((isStrict && sc.getResult().getStatus().isOk(isStrict)) || !isStrict) {
							try {
								sortedResults.get(g.getName()+"."+sc.getName()).add(sc.getResultDuration().toNanos());
								LinkedHashMap<String,Double> ss= stats.getStatistics(g.getName(),sc.getName());
								if(!isStrict && sc.getResult().getStatus().isOk(true))
									stats.putStatistic("pass",ss.get("pass")+1,g.getName(),sc.getName());
								else if (!isStrict)
									stats.putStatistic("fail",ss.get("fail")+1,g.getName(),sc.getName());
								stats.putStatistic("avg",ss.get("avg")+sc.getResultDuration().toNanos(),g.getName(),sc.getName());
								stats.putStatistic("cnt",ss.get("cnt")+1,g.getName(),sc.getName());
								if (((double)sc.getResultDuration().toNanos())>ss.get("max"))
									stats.putStatistic("max",(double)sc.getResultDuration().toNanos(),g.getName(),sc.getName());
								if (ss.get("min")==0.0||((double)sc.getResultDuration().toNanos())<ss.get("min"))
									stats.putStatistic("min",(double)sc.getResultDuration().toNanos(),g.getName(),sc.getName());
							}catch (Exception e){}
						}
						for (int sti = 0; sti < sc.getChildResults().size(); sti++)
						{
							StepResult stp = sc.getChildResults().get(sti);
							if (!sortedResults.containsKey(g.getName()+"."+sc.getName()+"."+stp.getName()))
								sortedResults.put(g.getName()+"."+sc.getName()+"."+stp.getName(), new ArrayList<Long>());
							stats.putKey(g.getName(),sc.getName(),stp.getName());
							if ((isStrict && stp.getResult().getStatus().isOk(isStrict)) || !isStrict) {
								try {
									sortedResults.get(g.getName()+"."+sc.getName()+"."+stp.getName()).add(stp.getResultDuration().toNanos());
									LinkedHashMap<String,Double> sts= stats.getStatistics(g.getName(),sc.getName(),stp.getName());
									if(!isStrict && stp.getResult().getStatus().isOk(true))
										stats.putStatistic("pass",sts.get("pass")+1,g.getName(),sc.getName(),stp.getName());
									else if (!isStrict)
										stats.putStatistic("fail",sts.get("fail")+1,g.getName(),sc.getName(),stp.getName());
									stats.putStatistic("cnt",sts.get("cnt")+1,g.getName(),sc.getName(),stp.getName());
									if (stp.getResultDuration()!=null)
										stats.putStatistic("avg",sts.get("avg")+stp.getResultDuration().toNanos(),g.getName(),sc.getName(),stp.getName());
									if (stp.getResultDuration()!=null && ((double)stp.getResultDuration().toNanos())>sts.get("max"))
										stats.putStatistic("max",((double)stp.getResultDuration().toNanos()),g.getName(),sc.getName(),stp.getName());
									if (stp.getResultDuration()!=null && (sts.get("min")==0.0||((double)stp.getResultDuration().toNanos())<sts.get("min")))
										stats.putStatistic("min",((double)stp.getResultDuration().toNanos()),g.getName(),sc.getName(),stp.getName());
								}catch (Exception e){}
							}
						}
					}
				}
			}
			if (!stats.isEmpty()) {
				GroupResult last = entry.getValue().get(entry.getValue().size()-1);
				LocalDateTime stop = last.getStop();
				LocalDateTime start = entry.getValue().get(0).getStart();
				if (startPeriod != null) {
					stop = stopPeriod;
					start = startPeriod;
			    }
				//If this is null than the group was not run during the time period
				if (nextConcurrentPeriod!=null) {
					while(last.getStop().isAfter(nextConcurrentPeriod)) {
						nextConcurrentPeriod = LocalDateTime.from(nextConcurrentPeriod).plus(1,ChronoUnit.SECONDS);
						stats.putStatistic("cncrnt",stats.getStatistic("cncrnt", last.getName())+getConcurrent(nextConcurrentPeriod,concurrency),last.getName());
					}
					long totalSeconds =  Duration.between(start,stop).getSeconds();
					if (totalSeconds>0) {
						stats.putStatistic("cncrnt",stats.getStatistic("cncrnt",last.getName())/totalSeconds, last.getName());
					}
				}
			}
		}
		if (!stats.isEmpty()) {
			for (String key :stats.getStatisticKeys())
			{
				if (stats.getStatistic("avg", key)>0 && stats.getStatistic("cnt", key)>0)
					stats.putStatistic("avg",stats.getStatistic("avg", key)/stats.getStatistic("cnt", key),key);
			}
		}
		this.statistics.setStats(stats);
		this.sortResults();
		return stats;
	}
	
	public HashMap<String,HashMap<String,StepErrors>> getErrors()
	{
		HashMap<String,HashMap<String,StepErrors>> map = new HashMap<String,HashMap<String,StepErrors>>();
		for (Entry<String,List<GroupResult>> entry: results.entrySet())
		{
			int c = 0;
			for (GroupResult f : entry.getValue())
			{
				c++;
				if ((startPeriod==null || f.getStop().isAfter(startPeriod))&&(stopPeriod==null || (f.getStop().isBefore(stopPeriod) ||c ==entry.getValue().size()))) {
					if (f.getResult().getStatus().is(Status.FAILED))
					{
						HashMap<String,StepErrors> sErrs = new HashMap<String,StepErrors>();
						if (map.containsKey(f.getName()))
						{
							sErrs = map.get(f.getName());
						}
						for (ScenarioResult sr : f.getChildResults())
						{
							if (sr.getResult().getStatus().is(Status.FAILED))
							{
								for (StepResult str : sr.getChildResults())
								{
									if (str.getResult().getStatus().is(Status.FAILED))
									{
										if (sErrs.containsKey(sr.getName()))
											sErrs.get(sr.getName()).putError(str.getName(),str.getStop(), str.getError());
										else
											sErrs.put(sr.getName(),new StepErrors(str.getName(),str.getStop(), str.getError()));
									}
								}
							}
						}
						map.put(f.getName(), sErrs);
					}
				}
			}
		}
		this.statistics.setErrors(map);
		return map;
	}
	
	public static Stats getDefaultStats(boolean isStrict)
	{
		Stats stats = new Stats();
		stats.putStatisticType(Stats.StatType.COUNT.type);
		if(!isStrict)
		{
			stats.putStatisticType(Stats.StatType.PASSED.type);
			stats.putStatisticType(Stats.StatType.FAILED.type);
		}
		stats.putStatisticType(Stats.StatType.AVERAGE.type);
		stats.putStatisticType(Stats.StatType.MINIMUM.type);
		stats.putStatisticType(Stats.StatType.MAXIMUM.type);
		stats.putStatisticType(Stats.StatType.CONCURRENCY.type);
		return stats;
	}
	
	
	private void sortResults() {
		for (String key : sortedResults.keySet())
		{
			Collections.sort(sortedResults.get(key));
		}
	}
	
	private Double getConcurrent(LocalDateTime now, List<LocalDateTime> concurrency) {
		for (int i = 0; i < concurrency.size();i++){
			if (concurrency.get(i).isBefore(now))
				concurrency.remove(i);
		}
		return (double) concurrency.size();
	}
}
