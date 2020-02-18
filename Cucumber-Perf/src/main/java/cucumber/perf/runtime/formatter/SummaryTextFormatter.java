package cucumber.perf.runtime.formatter;

import cucumber.perf.api.event.EventHandler;
import cucumber.perf.api.event.EventListener;
import cucumber.perf.api.event.EventPublisher;
import cucumber.perf.api.event.StatisticsFinished;
import cucumber.perf.api.result.GroupResult;
import cucumber.perf.api.result.statistics.StepErrors;
import cucumber.perf.api.result.statistics.Errors;
import cucumber.perf.api.result.statistics.Stat;
import cucumber.perf.api.result.statistics.Statistics;
import cucumber.perf.api.result.statistics.Stat.StatDataType;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public final class SummaryTextFormatter implements EventListener {
	private Writer out;
	private final AppendableBuilder builder;
	private final StatDataType displayType = StatDataType.MILLIS;
	private List<String> lines = new ArrayList<String>();
	
	private EventHandler<StatisticsFinished> statsFinishedEventhandler = new EventHandler<StatisticsFinished>() {
        @Override
        public void receive(StatisticsFinished event) {
            process(event.getResult());
        }
    };
	
	public SummaryTextFormatter(AppendableBuilder builder){
		this.builder= builder;
	}

	@Override
	public void setEventPublisher(EventPublisher publisher) {
		publisher.registerHandlerFor(StatisticsFinished.class, statsFinishedEventhandler);
	}
	
	private void process(Statistics stats) {
		reset();
		createLines(stats);
		this.finishReport();
	}
	
	private void reset()
	{
		this.out = builder.build();
		lines = new ArrayList<String>();
	}

	private void createLines(Statistics s){
		if (s.getSimulation() != null)
		{
			lines.add("Simulation: " + s.getSimulation().getName() + " Start: "+ s.getSimulation().getStart()+ " Stop: "+ s.getSimulation().getStop()+ " Duration: "+ s.getSimulation().getDuration().toString());
		}
		for (Entry<String, GroupResult> entry : s.getGroups().entrySet()) {
			String gn = entry.getKey();
			String gline = "\tGroup: " + gn;
			for (Entry<String, Double> es : s.getStatistics(gn))
			{
				Stat st = s.getStatType(es.getKey());
				gline += " "+st.getShortName()+": " + convertStatDataType(st.getDataType(),es.getValue());
			}
			lines.add(gline);
			for (int sc = 0; sc < entry.getValue().getChildResults().size(); sc++) {
				String scn = entry.getValue().getChildResults().get(sc).getName();
				String scline = "\t\tScenario: " + scn;
				for (Entry<String, Double> es : s.getStatistics(gn,scn))
				{
					Stat st = s.getStatType(es.getKey());
					if(!st.getKey().equalsIgnoreCase("cncrnt"))
						scline += " "+st.getShortName()+": " + convertStatDataType(st.getDataType(),es.getValue());
				}
				lines.add(scline);
				for (int stp = 0; stp < entry.getValue().getChildResults().get(sc).getChildResults().size(); stp++) {
					if (entry.getValue().getChildResults().get(sc).getChildResults().get(stp).getResultDuration() != null)
					{
						String stpn = entry.getValue().getChildResults().get(sc).getChildResults().get(stp).getName();
						String line = "\t\t\tStep: "+ stpn ;
						for (Entry<String, Double> es : s.getStatistics(gn,scn,stpn))
						{
							Stat st = s.getStatType(es.getKey());
							if(!st.getKey().equalsIgnoreCase("cncrnt"))
								line += " "+st.getShortName()+": " + convertStatDataType(st.getDataType(),es.getValue());
						}
						lines.add(line);
					}
				}
			}
		}
		
		HashMap<String,HashMap<String,StepErrors>> errors = s.getErrors();
		if (!errors.isEmpty())
		{
			lines.add("Errors:");
			for (Entry<String,HashMap<String,StepErrors>> entry : errors.entrySet()) {
				lines.add("\tGroup: " + entry.getKey());
				for (Entry<String,StepErrors> scentry: entry.getValue().entrySet()) {
					lines.add("\t\tScenario: " + scentry.getKey());
					for (Entry<String, Errors> stpentry: scentry.getValue().getErrors()) {
						StackTraceElement[] stes = stpentry.getValue().getThrowable().getStackTrace();
						lines.add("\t\t\tStep: " + stpentry.getValue().getStep());
						lines.add("\t\t\t  Count: "+stpentry.getValue().getCount()+" Timing: "+stpentry.getValue().getFirst().toString()+" - "+stpentry.getValue().getLast().toString());
						lines.add("\t\t\t  Message: "+stpentry.getValue().getThrowable().getMessage());
						for (StackTraceElement ste : stes)
						{
							lines.add("\t\t\t  "+ste.getClassName()+"."+ste.getMethodName()+"("+ste.getFileName()+":"+ste.getLineNumber()+")");
						}
					}
				}
			}
		}
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
			}
		} else {
			output = output+value;
		}
		return output;
	}
	
	private void finishReport() {
			try (BufferedWriter bw = new BufferedWriter(out)) {
				for (String line : lines)
				{
					bw.append(line+"\r\n");
				}

			} catch (IOException e) {

			}
			closeQuietly(out);
	}

	private static void closeQuietly(Closeable out) {
		try {
			out.close();
		} catch (IOException ignored) {
			// go gentle into that good night
		}
	}
}
