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
import cucumber.perf.api.formatter.Statistics;
import cucumber.perf.api.result.GroupResult;
import cucumber.perf.api.result.ScenarioResult;
import cucumber.perf.api.result.StepResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public final class ChartPointsFormatter implements EventListener, EventWriter {
	private NiceAppendable out;
	private AppendableBuilder builder;
	private LinkedHashMap<String,List<String>> lineGroups = new LinkedHashMap<String,List<String>>();
	private EventBus eventBus = null;
	private int chartPoints = 0;
	
	public ChartPointsFormatter(AppendableBuilder builder, String[] options) {
	    this.builder = builder;
	    if (options.length>0 && options[0].length()>0)
	    {
	    	this.chartPoints = Integer.parseInt(options[0]);
	    }
	 }
	   
    public ChartPointsFormatter(AppendableBuilder builder) {
    	this.builder = builder;
    }
    
    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(StatisticsFinished.class, statsFinishedEventHandler);
        publisher.registerHandlerFor(PerfRunStarted.class, perfRunStartedEventHandler);
    }
    
	@Override
	public void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}
	
    private EventHandler<StatisticsFinished> statsFinishedEventHandler = new EventHandler<StatisticsFinished>() {
        @Override
        public void receive(StatisticsFinished event) {
            process(event.getResult());
        }
    };
    
    private EventHandler<PerfRunStarted> perfRunStartedEventHandler = new EventHandler<PerfRunStarted>() {
        @Override
        public void receive(PerfRunStarted event) {
        	if (chartPoints>0)
        	{
        		eventBus.send(new ConfigStatistics(eventBus.getTime(),eventBus.getTimeMillis(),StatisticsFormatter.CONFIG_MAXPOINTS,chartPoints));
        	}
        }
    };   
    
	private void process(Statistics stats) {
		reset();
		for (Entry<String, List<HashMap<String, GroupResult>>> e : stats.getChartPoints().entrySet())
		{
			for (HashMap<String, GroupResult> l : e.getValue())
			{
				addLines(l);
			}
		}
		this.finishReport();
	}
	
	private void reset()
	{
		this.out = new NiceAppendable(builder.build());
		lineGroups= new LinkedHashMap<String,List<String>>();
	}

	private void addLines(HashMap<String, GroupResult> features) {
		//line: feature,scenario,step,type,datetime,value
		for (Entry<String,GroupResult> e : features.entrySet())
		{
			String feature = e.getValue().getName();
			String scenario = "";
			String step = "";
			String type = e.getKey();
			String datetime = e.getValue().getStop().toString();
			long value = e.getValue().getResultDuration();
			if (lineGroups.containsKey(feature+":"+type))
			{
				lineGroups.get(feature+":"+type).add(feature+","+scenario+","+step+","+type+","+datetime+","+value);
			}
			else
			{
				List<String> arr = new ArrayList<String>();
				arr.add(feature+","+scenario+","+step+","+type+","+datetime+","+value);
				lineGroups.put(feature+":"+type,arr);
			}
			
			for (ScenarioResult sr : e.getValue().getChildResults()) {
				scenario = sr.getName();
				datetime = sr.getStop().toString();
				value = sr.getResultDuration();
				if (lineGroups.containsKey(feature+"."+scenario+":"+type))
				{
					lineGroups.get(feature+"."+scenario+":"+type).add(feature+","+scenario+","+step+","+type+","+datetime+","+value);
				}
				else
				{
					List<String> arr = new ArrayList<String>();
					arr.add(feature+","+scenario+","+step+","+type+","+datetime+","+value);
					lineGroups.put(feature+"."+scenario+":"+type,arr);
				}
				for (StepResult stpr : sr.getChildResults()) {
					step = stpr.getName();
					datetime = sr.getStop().toString();
					value = sr.getResultDuration();
					if (lineGroups.containsKey(feature+"."+scenario+"."+step+":"+type))
					{
						lineGroups.get(feature+"."+scenario+"."+step+":"+type).add(feature+","+scenario+","+step+","+type+","+datetime+","+value);
					}
					else
					{
						List<String> arr = new ArrayList<String>();
						arr.add(feature+","+scenario+","+step+","+type+","+datetime+","+value);
						lineGroups.put(feature+"."+scenario+"."+step+":"+type,arr);
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
}
