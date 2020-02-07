package cucumber.perf.runtime.formatter;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map.Entry;

import cucumber.perf.api.event.EventHandler;
import cucumber.perf.api.event.EventListener;
import cucumber.perf.api.event.EventPublisher;
import cucumber.perf.api.event.StatisticsFinished;
import cucumber.perf.api.formatter.Statistics;
import cucumber.perf.api.formatter.SummaryPrinter;
import cucumber.perf.api.result.GroupResult;

public class DefaultSummaryPrinter implements SummaryPrinter, EventListener  {
    private final PrintStream out;
    
    private EventHandler<StatisticsFinished> statsFinishedEventhandler = new EventHandler<StatisticsFinished>() {
        @Override
        public void receive(StatisticsFinished event) {
            print(event.getResult());
        }
    };

    public DefaultSummaryPrinter() {
        this.out = System.out;
    }
    
    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(StatisticsFinished.class, statsFinishedEventhandler);
    }

    private void print(Statistics stats) {
        out.println();
        printStats(stats);
        out.println();
    }
    
	private void printStats(Statistics s){
		if (s.getSimulation() != null)
		{
			out.println("Simulation: " + s.getSimulation().getName() + " Start: "+ s.getSimulation().getStart()+ " Stop: "+ s.getSimulation().getStop()+ " Duration: "+ s.getSimulation().getDuration().toString());
		}
		for (Entry<String, GroupResult> entry : s.getAvg().entrySet()) {
			out.println("Group: " + entry.getKey() + " Count: " + s.getCnt().get(entry.getKey()).getResultDuration().toNanos()
					+ " Avg: " + entry.getValue().getResultDuration().toMillis()
					+ " Min: " + s.getMin().get(entry.getKey()).getResultDuration().toMillis() + " Max: "
					+ s.getMax().get(entry.getKey()).getResultDuration().toMillis());
			for (int sc = 0; sc < entry.getValue().getChildResults().size(); sc++) {
				out.println("	Scenario: " + entry.getValue().getChildResults().get(sc).getName() + " Count: "
						+ s.getCnt().get(entry.getKey()).getChildResults().get(sc).getResultDuration().toNanos() + " Avg: "
						+ entry.getValue().getChildResults().get(sc).getResultDuration().toMillis() + " Min: "
						+ s.getMin().get(entry.getKey()).getChildResults().get(sc).getResultDuration().toMillis()
						+ " Max: "
						+ s.getMax().get(entry.getKey()).getChildResults().get(sc).getResultDuration().toMillis());// 1000000
			
				for (int stp = 0; stp < entry.getValue().getChildResults().get(sc).getChildResults().size(); stp++) {
					if (entry.getValue().getChildResults().get(sc).getChildResults().get(stp).getResultDuration() != null)
					{
					out.println("		Step: "
							+ entry.getValue().getChildResults().get(sc).getChildResults().get(stp).getName() + " Count: "
							+ s.getCnt().get(entry.getKey()).getChildResults().get(sc).getChildResults().get(stp)
									.getResultDuration().toNanos() + " Avg: "
							+ entry.getValue().getChildResults().get(sc).getChildResults().get(stp).getResultDuration().toMillis()
							+ " Min: "
							+ s.getMin().get(entry.getKey()).getChildResults().get(sc).getChildResults().get(stp)
									.getResultDuration().toMillis()
							+ " Max: " + s.getMax().get(entry.getKey()).getChildResults().get(sc).getChildResults()
									.get(stp).getResultDuration().toMillis());
					}
				}
			}
		}
		
		HashMap<String,HashMap<String,Throwable>> errors = s.getErrors();
		if (!errors.isEmpty())
		{
			out.println("Errors:");
			for (Entry<String,HashMap<String,Throwable>> entry : errors.entrySet()) {
				out.println("Feature: " + entry.getKey());
				for (Entry<String,Throwable> sentry: entry.getValue().entrySet()) {
					out.println("Step: " + entry.getKey());
					sentry.getValue().printStackTrace(out);
				}
			}
		}
	}
}