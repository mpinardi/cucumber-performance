package cucumber.perf.api.formatter;

import io.cucumber.plugin.Plugin;

/**
 * Interface for plugins that only run when requested.
 *
 * @see Plugin
 */
public interface Minion extends Plugin {
    /**
     * Run the minion plugin.
     * @param obj The input to perform the action on.
     * @return object The result of the run.
     */
    Object run(Object obj);
}