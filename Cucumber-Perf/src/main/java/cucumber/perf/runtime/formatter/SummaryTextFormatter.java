package cucumber.perf.runtime.formatter;
import cucumber.perf.api.event.EventHandler;
import cucumber.perf.api.event.EventListener;
import cucumber.perf.api.event.EventPublisher;
import cucumber.perf.api.event.StatisticsFinished;
import cucumber.perf.api.formatter.Statistics;
import cucumber.perf.api.result.GroupResult;

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
	private List<String> lines = new ArrayList<String>();
	
	private EventHandler<StatisticsFinished> statsFinishedEventhandler = new EventHandler<StatisticsFinished>() {
        @Override
        public void receive(StatisticsFinished event) {
            process(event.getResult());
        }
    };
	
	public SummaryTextFormatter(AppendableBuilder builder) throws IOException {
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
		for (Entry<String, GroupResult> entry : s.getAvg().entrySet()) {
			lines.add("\tGroup: " + entry.getKey() + " Count: " + s.getCnt().get(entry.getKey()).getResultDuration().toNanos()
					+ " Avg: " + entry.getValue().getResultDuration().toMillis()
					+ " Min: " + s.getMin().get(entry.getKey()).getResultDuration().toMillis() + " Max: "
					+ s.getMax().get(entry.getKey()).getResultDuration().toMillis());
			for (int sc = 0; sc < entry.getValue().getChildResults().size(); sc++) {
				lines.add("\t\tScenario: " + entry.getValue().getChildResults().get(sc).getName() + " Count: "
						+ s.getCnt().get(entry.getKey()).getChildResults().get(sc).getResultDuration().toNanos() + " Avg: "
						+ entry.getValue().getChildResults().get(sc).getResultDuration().toMillis() + " Min: "
						+ s.getMin().get(entry.getKey()).getChildResults().get(sc).getResultDuration().toMillis()
						+ " Max: "
						+ s.getMax().get(entry.getKey()).getChildResults().get(sc).getResultDuration().toMillis());// 1000000
			
				for (int stp = 0; stp < entry.getValue().getChildResults().get(sc).getChildResults().size(); stp++) {
					if (entry.getValue().getChildResults().get(sc).getChildResults().get(stp).getResultDuration() != null)
					{
						lines.add("\t\t\tStep: "
							+ entry.getValue().getChildResults().get(sc).getChildResults().get(stp).getName() + " Count: "
							+ s.getCnt().get(entry.getKey()).getChildResults().get(sc).getChildResults().get(stp)
									.getResultDuration().toNanos()+ " Avg: "
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
			lines.add("Errors:");
			for (Entry<String,HashMap<String,Throwable>> entry : errors.entrySet()) {
				lines.add("\tScenario: " + entry.getKey());
				for (Entry<String,Throwable> sentry: entry.getValue().entrySet()) {
					lines.add("\t\tStep: " + entry.getKey());
					StackTraceElement[] stes = sentry.getValue().getStackTrace();
					lines.add("\t\t"+sentry.getValue().getMessage());
					for (StackTraceElement ste : stes)
					{
						lines.add("\t\t"+ste.getClassName()+"."+ste.getMethodName()+"("+ste.getFileName()+":"+ste.getLineNumber()+")");
					}
				}
			}
		}
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
