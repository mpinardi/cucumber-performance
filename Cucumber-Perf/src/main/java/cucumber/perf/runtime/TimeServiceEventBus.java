package cucumber.perf.runtime;

import cucumber.runner.TimeService;

public final class TimeServiceEventBus extends AbstractEventBus {
    private final TimeService stopWatch;

    public TimeServiceEventBus(TimeService stopWatch) {
        this.stopWatch = stopWatch;
    }

    @Override
    public Long getTime() {
        return stopWatch.time();
    }

    @Override
    public Long getTimeMillis() {
        return stopWatch.timeMillis();
    }
}