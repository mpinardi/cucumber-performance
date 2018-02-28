package cucumber.api.perf.formatter;

import java.util.List;

import cucumber.api.Plugin;
import cucumber.api.perf.result.FeatureResult;

/**
 * This is the interface you should implement if you want your own custom
 * formatter.
 *
 * @see Plugin
 */
public interface Formatter extends  Plugin {
	void process(List<FeatureResult> result);
}