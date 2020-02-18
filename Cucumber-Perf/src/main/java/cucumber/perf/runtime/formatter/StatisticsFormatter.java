package cucumber.perf.runtime.formatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import cucumber.perf.api.event.ConfigStatistics;
import cucumber.perf.api.event.EventBus;
import cucumber.perf.api.event.EventHandler;
import cucumber.perf.api.event.EventListener;
import cucumber.perf.api.event.EventPublisher;
import cucumber.perf.api.event.SimulationFinished;
import cucumber.perf.api.event.StatisticsFinished;
import cucumber.perf.api.event.StatisticsStarted;
import cucumber.perf.api.formatter.EventWriter;
import cucumber.perf.api.formatter.PluginSpawner;
import cucumber.perf.api.formatter.StatisticCreator;
import cucumber.perf.api.result.BaseResult;
import cucumber.perf.api.result.GroupResult;
import cucumber.perf.api.result.SimulationResult;
import cucumber.perf.api.result.StepResult;
import io.cucumber.plugin.StrictAware;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import cucumber.perf.api.result.statistics.DefaultStatistics;

public final class StatisticsFormatter implements EventListener,EventWriter,StrictAware,PluginSpawner {
	public final static String CONFIG_ADDPLUGIN = "addPlugin";
	private HashSet<String> pluginMinions = new HashSet<String>();
	private DefaultStatistics statistics = null;
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
	
	public StatisticsFormatter(String[] options)
	{
		for (String opt : options)
			pluginMinions.add(opt);
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
		if (setting==CONFIG_ADDPLUGIN){
			pluginMinions.add((String) value);
		}
	}
	
	private void process(SimulationResult result)
	{
		if (result != null)
		{
			if (result.getChildResults() != null && !result.getChildResults().isEmpty())
			{
				HashMap<String,List<GroupResult>> results = new HashMap<String,List<GroupResult>>();
				eventBus.send(new StatisticsStarted(eventBus.getTime(),eventBus.getTimeMillis()));
				for (GroupResult o : result.getChildResults()) {
					if (results.containsKey(o.getName())) {
						results.get(o.getName()).add(o);
					} else {
						results.put(o.getName(), new ArrayList<GroupResult>(Arrays.asList(o)));
					}
				}
				statistics = new DefaultStatistics(new BaseResult(result.getName(),result.getResult(),result.getStart(),result.getStop()),results);
				statistics.getStats(isStrict);
				statistics.getErrors();
				runMinions();
				eventBus.send(new StatisticsFinished(eventBus.getTime(),eventBus.getTimeMillis(),statistics.getStatistics()));
			}
		}
	}
	
	private void runMinions()
	{
		PluginFactory pf = new PluginFactory();
		for (String plugin : pluginMinions)
		{
			if(PluginFactory.isStatisticCreatorName(plugin)) {
				StatisticCreator sc = (StatisticCreator) pf.create(plugin);
				this.statistics.getStatistics().addStats(sc.run(statistics.getSortedResults()));
			}
		}
	}
}
