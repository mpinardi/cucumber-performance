package cucumber.perf.runtime;

import java.time.Clock;

public final class TimeServiceEventBus extends AbstractEventBus {
    private final Clock clock;

    public TimeServiceEventBus(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Long getTime() {
        return clock.instant().getEpochSecond();
    }

    @Override
    public Long getTimeMillis() {
        return clock.instant().toEpochMilli();
    }
}