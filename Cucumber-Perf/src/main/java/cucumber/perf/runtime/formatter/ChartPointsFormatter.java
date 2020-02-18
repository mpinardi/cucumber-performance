package cucumber.perf.runtime.formatter;

import cucumber.perf.api.event.ChartFinished;
import cucumber.perf.api.event.ChartStarted;
import cucumber.perf.api.event.EventBus;
import cucumber.perf.api.event.EventHandler;
import cucumber.perf.api.event.EventListener;
import cucumber.perf.api.event.EventPublisher;
import cucumber.perf.api.event.SimulationFinished;
import cucumber.perf.api.formatter.EventWriter;
import cucumber.perf.api.formatter.NiceAppendable;
import cucumber.perf.api.formatter.PluginSpawner;
import cucumber.perf.api.formatter.StatisticCreator;
import cucumber.perf.api.result.BaseResult;
import cucumber.perf.api.result.GroupResult;
import cucumber.perf.api.result.ScenarioResult;
import cucumber.perf.api.result.SimulationResult;
import cucumber.perf.api.result.StepResult;
import cucumber.perf.api.result.statistics.Chart;
import cucumber.perf.api.result.statistics.DefaultStatistics;
import cucumber.perf.api.result.statistics.Stat;
import cucumber.perf.api.result.statistics.Stats;
import io.cucumber.plugin.StrictAware;
import cucumber.perf.api.result.statistics.Stat.StatDataType;
import cucumber.perf.api.result.statistics.Statistics;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public final class ChartPointsFormatter implements EventListener, EventWriter,StrictAware,PluginSpawner {
	private NiceAppendable out;
	private AppendableBuilder builder;
	private LinkedHashMap<String,List<String>> lineGroups = new LinkedHashMap<String,List<String>>();
	private DefaultStatistics statistics = null;
	private int chartPoints = 20;
	private boolean isStrict = false;
	private HashSet<String> pluginMinionTypes = new HashSet<String>();
	private List<StatisticCreator> pluginMinions = new ArrayList<StatisticCreator>();
	private final StatDataType displayType = StatDataType.NANOS;
	private EventBus eventBus;
	
	public ChartPointsFormatter(AppendableBuilder builder, String[] options) {
	    this.builder = builder;
	    boolean foundCP = false;
		for (String opt : options)
	    {
			if (!opt.isEmpty()&&!foundCP) {
				try {
					this.chartPoints = Integer.parseInt(opt);
					foundCP = true;
				} catch( Exception e) {
					pluginMinionTypes.add(opt);
				}
			} else 
				pluginMinionTypes.add(opt);
	    }
	 }
	   
    public ChartPointsFormatter(AppendableBuilder builder) {
    	this.builder = builder;
    }
    
    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(SimulationFinished.class, simulationFinishedEventHandler);
    }
	
	private EventHandler<SimulationFinished> simulationFinishedEventHandler = new EventHandler<SimulationFinished>() {
	     @Override
	     public void receive(SimulationFinished event) {
	        process(event.getResult());
	     }
	};
    
	
	private void process(SimulationResult result)
	{
		if (result != null)
		{
			eventBus.send(new ChartStarted(eventBus.getTime(),eventBus.getTimeMillis()));
			createMinions();
			Chart chart = null;
			HashMap<String,List<GroupResult>> results = new HashMap<String,List<GroupResult>>();
			if (result.getChildResults() != null && !result.getChildResults().isEmpty())
			{
				for (GroupResult o : result.getChildResults()) {
					if (results.containsKey(o.getName())) {
						results.get(o.getName()).add(o);
					} else {
						results.put(o.getName(), new ArrayList<GroupResult>(Arrays.asList(o)));
					}
				}
				chart = createChart(new BaseResult(result.getName(),result.getResult(),result.getStart(),result.getStop()),
						result.getStart(),result.getStop(),results);
			}
			reset();
			addLines(chart);
			this.finishReport();
    		eventBus.send(new ChartFinished(eventBus.getTime(),eventBus.getTimeMillis(),chart));
		}
	}
	
	private Chart createChart(BaseResult simulation ,LocalDateTime start, LocalDateTime stop, HashMap<String,List<GroupResult>> results) {
		Chart chart = new Chart();
		Long period = getPeriod(Duration.between(start,stop), chartPoints);
		LocalDateTime endPeriod = this.getEnd(start, period);
		LocalDateTime startPeriod = start;
		statistics = new DefaultStatistics(simulation, results);
		while (endPeriod.isBefore(stop)|| endPeriod.isEqual(stop)) {
			runMinions(statistics.getStats(isStrict,startPeriod,endPeriod),statistics.getSortedResults());
			statistics.getErrors();
			chart.putPoint(getMid(startPeriod,endPeriod), statistics.getStatistics());
			startPeriod = endPeriod;
			endPeriod = this.getEnd(endPeriod, period);
		}
		return chart;
	}
	
	private void createMinions() {
		PluginFactory pf = new PluginFactory();
		for (String plugin : pluginMinionTypes)
		{
			if(PluginFactory.isStatisticCreatorName(plugin)) {
				StatisticCreator sc = (StatisticCreator) pf.create(plugin);
				this.pluginMinions.add(sc);
			}
		}
	}
	
	private void runMinions(Stats stats, HashMap<String,List<Long>> results) {
		for (StatisticCreator plugin : pluginMinions)
		{
			stats.addStatistics(plugin.run(results));
		}
	}

	private void reset()
	{
		this.out = new NiceAppendable(builder.build());
		lineGroups= new LinkedHashMap<String,List<String>>();
	}

	private void addLines(Chart chart) {
		//line: feature,scenario,step,type,datetime,value
		for (Entry<Instant, Statistics> l : chart.get().entrySet())
		{
			for (Entry<String, GroupResult> e  : l.getValue().getGroups().entrySet())
			{
				String feature = e.getValue().getName();
				String scenario = "";
				String step = "";
				String datetime = e.getValue().getStop().toString();
				String type = "";
				Stats s = l.getValue().getStats();
				for (Entry<String, Double> es : s.getStatistics(feature).entrySet())
				{
					Stat st = s.getStatisticType(es.getKey());
					type = es.getKey();
					if (lineGroups.containsKey(feature+":"+type))
					{
						lineGroups.get(feature+":"+st.getKey()).add(feature+","+scenario+","+step+","+type+","+datetime+","+convertStatDataType(st.getDataType(),es.getValue()));
					}
					else
					{
						List<String> arr = new ArrayList<String>();
						arr.add(feature+","+scenario+","+step+","+es.getKey()+","+datetime+","+convertStatDataType(st.getDataType(),es.getValue()));
						lineGroups.put(feature+":"+type,arr);
					}
				}
				for (ScenarioResult sr : e.getValue().getChildResults()) {
					scenario = sr.getName();
					datetime = sr.getStop().toString();
					for (Entry<String, Double> es : s.getStatistics(feature+"."+scenario).entrySet())
					{
						Stat st = s.getStatisticType(es.getKey());
						type = es.getKey();
						if(!type.equalsIgnoreCase("cncrnt")) {
							if (lineGroups.containsKey(feature+"."+scenario+":"+type))
							{
								lineGroups.get(feature+"."+scenario+":"+type).add(feature+","+scenario+","+step+","+type+","+datetime+","+convertStatDataType(st.getDataType(),es.getValue()));
							}
							else
							{
								List<String> arr = new ArrayList<String>();
								arr.add(feature+","+scenario+","+step+","+type+","+datetime+","+convertStatDataType(st.getDataType(),es.getValue()));
								lineGroups.put(feature+"."+scenario+":"+type,arr);
							}
						}
					}
					for (StepResult stpr : sr.getChildResults()) {
						step = stpr.getName();
						datetime = sr.getStop().toString();
						for (Entry<String, Double> es : s.getStatistics(feature+"."+scenario+"."+step).entrySet())
						{
							Stat st = s.getStatisticType(es.getKey());
							type = es.getKey();
							if(!type.equalsIgnoreCase("cncrnt")) {
								if (lineGroups.containsKey(feature+"."+scenario+"."+step+":"+type))
								{
									lineGroups.get(feature+"."+scenario+"."+step+":"+type).add(feature+","+scenario+","+step+","+type+","+datetime+","+convertStatDataType(st.getDataType(),es.getValue()));
								}
								else
								{
									List<String> arr = new ArrayList<String>();
									arr.add(feature+","+scenario+","+step+","+type+","+datetime+","+convertStatDataType(st.getDataType(),es.getValue()));
									lineGroups.put(feature+"."+scenario+"."+step+":"+type,arr);
								}
							}
						}
					}
				}
			}
		}
	}

	private void finishReport() {
			for (Entry<String,List<String>> group : lineGroups.entrySet())
			{
					for (String line : group.getValue())
					{
						out.append(line+"\n");
					}
			}
			out.close();
	}
	
	private String convertStatDataType(StatDataType dataType, Double value) {
		DecimalFormat df = new DecimalFormat("#0.00000");
		String output = "";
		if (dataType==StatDataType.COUNT){
			output = output+value.longValue();
		} else if (dataType==StatDataType.OTHER){
			output = output+df.format(value);
		} else if (this.displayType!=dataType){
			if (dataType == StatDataType.NANOS && this.displayType==StatDataType.MILLIS) {
				output = output+ df.format((value/ 1000000));
			} else if (dataType == StatDataType.MILLIS && this.displayType==StatDataType.NANOS) {
				output = output+ df.format((value* 1000000));
			} else if (dataType == StatDataType.MILLIS && this.displayType==StatDataType.SECONDS) {
				output = output+ df.format((value/ 1000));
			} else if (dataType == StatDataType.NANOS && this.displayType==StatDataType.SECONDS) {
				output = output+ df.format((value/ 1000000000));
			} else if (dataType == StatDataType.SECONDS && this.displayType== StatDataType.MILLIS) {
				output = output+ df.format((value* 1000));
			} else if (dataType == StatDataType.SECONDS && this.displayType==StatDataType.NANOS) {
				output = output+ df.format((value* 1000000000));
			}
		} else {
			output = output+value;
		}
		return output;
	}
	
	
	private long getPeriod(Duration time, int times) { 
		return (time.getSeconds()*1000) / times; 
	}
	  
	private LocalDateTime getEnd(LocalDateTime start, long timeMillis) {
		LocalDateTime endt = LocalDateTime.from(start).plus(timeMillis,ChronoUnit.MILLIS); 
		return endt;
	}
	  
	private Instant getMid(LocalDateTime start, LocalDateTime end) {
		long millis = ChronoUnit.MILLIS.between(start,end)/2;
		return LocalDateTime.from(start).plus(millis,ChronoUnit.MILLIS).toInstant(ZoneOffset.UTC);
	}
	 
	@Override
	public void setStrict(boolean strict) {
		this.isStrict = strict;
	}

	@Override
	public void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}
}
