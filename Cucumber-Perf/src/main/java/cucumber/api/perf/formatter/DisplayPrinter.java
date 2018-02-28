package cucumber.api.perf.formatter;

import java.util.List;

import cucumber.api.Plugin;
import cucumber.api.perf.PerfGroup;

/**
 * This is the interface you should implement if you want your own custom
 * formatter.
 *
 * @see Plugin
 */
public interface DisplayPrinter extends  Plugin {
	void print(List<PerfGroup> groups);
}