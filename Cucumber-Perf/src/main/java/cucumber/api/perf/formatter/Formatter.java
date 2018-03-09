package cucumber.api.perf.formatter;

import cucumber.api.Plugin;

/**
 * This is the interface you should implement if you want your own custom
 * formatter.
 *
 * @see Plugin
 */
public interface Formatter extends  Plugin {
	void process(Statistics stats);
}