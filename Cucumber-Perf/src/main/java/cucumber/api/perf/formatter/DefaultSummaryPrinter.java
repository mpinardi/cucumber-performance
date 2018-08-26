package cucumber.api.perf.formatter;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import cucumber.api.perf.result.FeatureResult;

public class DefaultSummaryPrinter implements SummaryPrinter {
    private final PrintStream out;

    public DefaultSummaryPrinter() {
        this.out = System.out;
    }

    @Override
    public void print(Statistics stats) {
        out.println();
        printStats(stats);
        out.println();
    }
    
	private void printStats(Statistics s){
		
		out.println("Averages:");

		for (Entry<String, FeatureResult> entry : s.getAvg().entrySet()) {
			out.println("Feature: " + entry.getKey() + " Avg: " + entry.getValue().getResultDuration() / 1000000
					+ " Min: " + s.getMin().get(entry.getKey()).getResultDuration() / 1000000 + " Max: "
					+ s.getMax().get(entry.getKey()).getResultDuration() / 1000000);
			for (int sc = 0; sc < entry.getValue().getChildResults().size(); sc++) {
				out.println("	Scenario: " + entry.getValue().getChildResults().get(sc).getName() + " Avg: "
						+ entry.getValue().getChildResults().get(sc).getResultDuration() / 1000000 + " Min: "
						+ s.getMin().get(entry.getKey()).getChildResults().get(sc).getResultDuration() / 1000000
						+ " Max: "
						+ s.getMax().get(entry.getKey()).getChildResults().get(sc).getResultDuration() / 1000000);// 1000000
			
				for (int stp = 0; stp < entry.getValue().getChildResults().get(sc).getChildResults().size(); stp++) {
					if (entry.getValue().getChildResults().get(sc).getChildResults().get(stp).getResultDuration() != null)
					{
					out.println("		Step: "
							+ entry.getValue().getChildResults().get(sc).getChildResults().get(stp).getName() + " Avg: "
							+ entry.getValue().getChildResults().get(sc).getChildResults().get(stp).getResultDuration()
									/ 1000000
							+ " Min: "
							+ s.getMin().get(entry.getKey()).getChildResults().get(sc).getChildResults().get(stp)
									.getResultDuration() / 1000000
							+ " Max: " + s.getMax().get(entry.getKey()).getChildResults().get(sc).getChildResults()
									.get(stp).getResultDuration() / 1000000);
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