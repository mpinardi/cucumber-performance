package cucumber.perf.runtime.formatter;

import cucumber.api.formatter.NiceAppendable;
import cucumber.perf.api.event.ConfigStatistics;
import cucumber.perf.api.event.EventBus;
import cucumber.perf.api.event.EventHandler;
import cucumber.perf.api.event.EventListener;
import cucumber.perf.api.event.EventPublisher;
import cucumber.perf.api.event.PerfRunStarted;
import cucumber.perf.api.event.StatisticsFinished;
import cucumber.perf.api.formatter.EventWriter;
import cucumber.perf.api.result.GroupResult;
import cucumber.perf.api.result.ScenarioResult;
import cucumber.perf.api.result.StepResult;
import cucumber.perf.api.result.statistics.Stats;
import cucumber.perf.api.result.statistics.Stat.StatDataType;
import cucumber.perf.api.result.statistics.Stat;
import cucumber.perf.api.result.statistics.Statistics;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public final class TaurusFormatter implements EventListener, EventWriter {
	private NiceAppendable out;
	private AppendableBuilder builder;
	private List<String> lines = new ArrayList<String>();
	private final StatDataType displayType = StatDataType.MILLIS;
	private EventBus eventBus;
	private final static String HEADER = "label,avg_ct,avg_lt,avg_rt,bytes,concurrency,fail,stdev_rt,succ,throughput,perc_0.0,perc_50.0,perc_90.0,perc_95.0,perc_99.0,perc_99.9,perc_100.0,rc_200";

	   
    public TaurusFormatter(AppendableBuilder builder) {
    	this.builder = builder;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(StatisticsFinished.class, statisticsFinishedHandler);
        publisher.registerHandlerFor(PerfRunStarted.class, perfRunStartedEventHandler);
    }
	
	private EventHandler<StatisticsFinished> statisticsFinishedHandler = new EventHandler<StatisticsFinished>() {
		@Override
		public void receive(StatisticsFinished event) {
			process(event.getResult());
		}
	};
	
	private EventHandler<PerfRunStarted> perfRunStartedEventHandler = new EventHandler<PerfRunStarted>() {
        @Override
        public void receive(PerfRunStarted event) {
        	eventBus.send(new ConfigStatistics(eventBus.getTime(),eventBus.getTimeMillis(),StatisticsFormatter.CONFIG_ADDPLUGIN,"prcntl:50"));
        	eventBus.send(new ConfigStatistics(eventBus.getTime(),eventBus.getTimeMillis(),StatisticsFormatter.CONFIG_ADDPLUGIN,"prcntl:90"));
        	eventBus.send(new ConfigStatistics(eventBus.getTime(),eventBus.getTimeMillis(),StatisticsFormatter.CONFIG_ADDPLUGIN,"prcntl:95"));
        	eventBus.send(new ConfigStatistics(eventBus.getTime(),eventBus.getTimeMillis(),StatisticsFormatter.CONFIG_ADDPLUGIN,"prcntl:99"));
        	eventBus.send(new ConfigStatistics(eventBus.getTime(),eventBus.getTimeMillis(),StatisticsFormatter.CONFIG_ADDPLUGIN,"prcntl:99.5"));
        }
    };   
	
	private void process(Statistics result)
	{
		reset();
		addLines(result);
		this.finishReport();
	}

	private void reset()
	{
		this.out = new NiceAppendable(builder.build());
		lines= new ArrayList<String>();
	}

	private void addLines(Statistics result) {
		for (Entry<String, GroupResult> e  : result.getGroups().entrySet())
		{
			String feature = e.getValue().getName();
			String scenario = "";
			String step = "";
			Stats s = result.getStats();
			lines.add(createLine(feature,s.getStatistics(feature)));
			
			for (ScenarioResult sr : e.getValue().getChildResults()) {
				scenario = sr.getName();
				lines.add(createLine(feature+"."+scenario,s.getStatistics(feature+"."+scenario)));
				for (StepResult stpr : sr.getChildResults()) {
					step = stpr.getName();
					lines.add(createLine(feature+"."+scenario+"."+step,s.getStatistics(feature+"."+scenario+"."+step)));
				}
			}
		}
	}
	
	private String createLine(String label, LinkedHashMap<String,Double> statistics) {
		/*
		 * label - is the sample group for which this CSV line presents the stats. Empty
		 * label means total of all labels 
		 * concurrency - average number of Virtual Users - null
		 * throughput - total count of all samples - count
		 * succ - total count of not-failed samples - pass
		 * fail - total count of saved samples  - fail
		 * avg_rt - average response time - avg
		 * stdev_rt - standard deviation of response time 
		 * avg_ct - average connect time if present - null
		 * avg_lt - average latency if present - null
		 * rc_200 - counts for specific response codes - null
		 * perc_0.0 .. perc_100.0 - percentile levels for response time,0 is also minimum response time, 100 is maximum 
		 * perc_0.0 - min
		 * perc_100 - max
		 * bytes - total download size - null
		 */
		String sep = ",";
		//label,avg_ct,avg_lt,avg_rt,bytes,concurrency,fail,stdev_rt,succ,
		//throughput,perc_0.0,perc_50.0,perc_90.0,perc_95.0,perc_99.0,perc_99.9,perc_100.0,rc_200
		String result = label+sep+"0.00000"+sep+"0.00000"
		+sep+convertStatDataType(Stats.StatType.AVERAGE.type.getDataType(),statistics.get(Stats.StatType.AVERAGE.key))
		+sep+0
		+sep+getStat(Stats.StatType.CONCURRENCY.type,statistics,"0.00000")
		+sep+getStat(Stats.StatType.FAILED.type,statistics,"0")
		+sep+getStat(Stats.StatType.STD_DEVIATION.type,statistics,"0.00000")
		+sep+getStat(Stats.StatType.PASSED.type,statistics,"0.00000")
		+sep+convertStatDataType(Stats.StatType.COUNT.type.getDataType(),statistics.get(Stats.StatType.COUNT.key))
		+sep+convertStatDataType(Stats.StatType.MINIMUM.type.getDataType(),statistics.get(Stats.StatType.MINIMUM.key))
		+sep+getStat(Stats.StatType.PERCENTILE.type,"50",statistics,"0.00000")
		+sep+getStat(Stats.StatType.PERCENTILE.type,"90",statistics,"0.00000")
		+sep+getStat(Stats.StatType.PERCENTILE.type,"95",statistics,"0.00000")
		+sep+getStat(Stats.StatType.PERCENTILE.type,"99",statistics,"0.00000")
		+sep+getStat(Stats.StatType.PERCENTILE.type,"99.5",statistics,"0.00000")
		+sep+convertStatDataType(Stats.StatType.MAXIMUM.type.getDataType(),statistics.get(Stats.StatType.MAXIMUM.key))
		+sep+0;
		return result;
	}

	private void finishReport() {
		out.append(HEADER+"\n");
		for (String line : lines)
		{
			out.append(line+"\n");
		}
		out.close();
	}
	
	private String getStat(Stat stat, String postfix,LinkedHashMap<String,Double> statistics, String defaultValue) {
		Stat st = new Stat(stat,postfix);
		return getStat(st,statistics,defaultValue);
	}
	private String getStat(Stat statType, LinkedHashMap<String,Double> statistics, String defaultValue) {
		return statistics.containsKey(statType.getKey())?convertStatDataType(statType.getDataType(),statistics.get(statType.getKey())):defaultValue;
	}
	
	private String convertStatDataType(StatDataType dataType, Double value) {
		String output = "";
		if (dataType==StatDataType.COUNT){
			output = output+value.longValue();
		} else if (dataType==StatDataType.OTHER){
			output = output+value;
		} else if (this.displayType!=dataType){
			if (dataType == StatDataType.NANOS && this.displayType==StatDataType.MILLIS) {
				output = output+ (value/ 1000000);
			} else if (dataType == StatDataType.MILLIS && this.displayType==StatDataType.NANOS) {
				output = output+ (value* 1000000);
			} else if (dataType == StatDataType.MILLIS && this.displayType==StatDataType.SECONDS) {
				output = output+ (value/ 1000);
			} else if (dataType == StatDataType.NANOS && this.displayType==StatDataType.SECONDS) {
				output = output+ (value/ 1000000000);
			} else if (dataType == StatDataType.SECONDS && this.displayType== StatDataType.MILLIS) {
				output = output+ (value* 1000);
			} else if (dataType == StatDataType.SECONDS && this.displayType==StatDataType.NANOS) {
				output = output+ (value* 1000000000);
			} else {
				output = output+value;
			}
		}
		return output;
	}

	@Override
	public void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}
}
