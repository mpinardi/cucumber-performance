package cucumber.api.perf.formatter;

import java.io.PrintStream;
import java.util.List;

import cucumber.api.perf.PerfGroup;

public class DetailDisplayPrinter implements DisplayPrinter {
	private final PrintStream out;
	private final String osname = System.getProperty("os.name");

	public DetailDisplayPrinter() {
		this.out = System.out;
	}

	@Override
	public void print(List<PerfGroup> groups) {
		// osname.contains("Windows")
		String line = "";
		int i = 0;
		for (PerfGroup pg : groups) {
			if (i > 0) {
				line = line + " | ";
			}
			line += trimFeature(pg.getText()) + ":" + pg.getRunning() + "-" + pg.getMaxThreads() + ">" + pg.getRan();
			i++;
		}
		out.print(line + "\r");

		/* looking at better options
		 * else { int i = 0; for(PerfGroup pg : groups) {
		 * out.println(trimFeature(pg.getText())+":"+pg.getRunning()+"-"+pg.
		 * getMaxThreads()+">"+pg.getRan()); i++; }
		 * out.print(String.format("\033[%dA",i)); // Move up
		 * System.out.print("\033[2K"); }
		 */
	}

	private String trimFeature(String name) {
		if (name.endsWith(".feature")) {
			return name.substring(0, name.length() - 8);
		}
		return name;
	}

}
