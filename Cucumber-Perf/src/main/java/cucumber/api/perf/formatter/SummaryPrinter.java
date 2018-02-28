package cucumber.api.perf.formatter;


import cucumber.api.Plugin;

/**
 * Interface for plugins that print a summary after test execution.
 *
 * @see Plugin
 */
public interface SummaryPrinter extends Plugin {
    void print(Statistics stats);
}