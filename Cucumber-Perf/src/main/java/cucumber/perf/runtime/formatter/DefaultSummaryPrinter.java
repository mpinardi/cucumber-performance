package cucumber.perf.runtime.formatter;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map.Entry;

import cucumber.perf.api.event.EventHandler;
import cucumber.perf.api.event.EventListener;
import cucumber.perf.api.event.EventPublisher;
import cucumber.perf.api.event.StatisticsFinished;
import cucumber.perf.api.formatter.SummaryPrinter;
import cucumber.perf.api.result.GroupResult;
import cucumber.perf.api.result.statistics.StepErrors;
import cucumber.perf.api.result.statistics.Errors;
import cucumber.perf.api.result.statistics.Stat;
import cucumber.perf.api.result.statistics.Stat.StatDataType;
import cucumber.perf.api.result.statistics.Statistics;

public final class DefaultSummaryPrinter implements SummaryPrinter, EventListener  {
    private final PrintStream out;
    private final StatDataType displayType = StatDataType.MILLIS;
    
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

		for (Entry<String, GroupResult> entry : s.getGroups().entrySet()) {
			String gn = entry.getKey();
			String line = "\tGroup: " + gn;
			for (Entry<String, Double> es : s.getStatistics(gn))
			{
				Stat st = s.getStatType(es.getKey());
				line += " "+st.getShortName()+": " + convertStatDataType(st.getDataType(),es.getValue());
			}
			out.println(line);
			for (int sc = 0; sc < entry.getValue().getChildResults().size(); sc++) {
				String scn = entry.getValue().getChildResults().get(sc).getName();
				line = "\t\tScenario: " + scn;
				for (Entry<String, Double> es : s.getStatistics(gn,scn))
				{
					Stat st = s.getStatType(es.getKey());
					if(!st.getKey().equalsIgnoreCase("cncrnt"))
						line += " "+st.getShortName()+": " + convertStatDataType(st.getDataType(),es.getValue());
				}
				out.println(line);
				for (int stp = 0; stp < entry.getValue().getChildResults().get(sc).getChildResults().size(); stp++) {
					if (entry.getValue().getChildResults().get(sc).getChildResults().get(stp).getResultDuration() != null)
					{
						String stpn = entry.getValue().getChildResults().get(sc).getChildResults().get(stp).getName();
						line = "\t\t\tStep: "+ stpn ;
						for (Entry<String, Double> es : s.getStatistics(gn,scn,stpn))
						{
							Stat st = s.getStatType(es.getKey());
							if(!st.getKey().equalsIgnoreCase("cncrnt"))
								line += " "+st.getShortName()+": " + convertStatDataType(st.getDataType(),es.getValue());
						}
						out.println(line);
					}
				}
			}
		}
		
		HashMap<String,HashMap<String,StepErrors>> errors = s.getErrors();
		if (!errors.isEmpty())
		{
			out.println("Errors:");
			for (Entry<String,HashMap<String,StepErrors>> entry : errors.entrySet()) {
				out.println("\tGroup: " + entry.getKey());
				for (Entry<String,StepErrors> scentry: entry.getValue().entrySet()) {
					out.println("\t\tScenario: " + scentry.getKey());
					for (Entry<String, Errors> stpentry: scentry.getValue().getErrors()) {
					out.println("\t\t\tStep: " + stpentry.getValue().getStep());
					out.println("\t\t\t  Count: "+ stpentry.getValue().getCount()+" Timing:"+stpentry.getValue().getFirst().toString()+" - "+stpentry.getValue().getLast().toString());
					stpentry.getValue().getThrowable().printStackTrace(out);
					}
				}
			}
		}
	}

	private String convertStatDataType(StatDataType dataType, Double value) {
		DecimalFormat df = new DecimalFormat("#0.000");
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
}