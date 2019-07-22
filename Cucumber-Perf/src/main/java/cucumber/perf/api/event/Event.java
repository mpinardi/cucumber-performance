package cucumber.perf.api.event;

import java.util.Comparator;

public interface Event {

    /**
     * When pickles are executed in parallel events can be
     * produced with a partial ordering.
     * <p>
     * The canonical order is the order in which these events
     * would have been generated had cucumber executed these
     * pickles in a serial fashion.
     * <p>
     * In canonical order events are first ordered by type:
     * <ol>
     * <li>SimulationStarted
     * <li>GroupEvent
     * <li>SimulationFinished
     * <li>StatisticsStarted
     * <li>StatisticsFinished
     * </ol>
     * <p>
     */
    Comparator<Event> CANONICAL_ORDER = new CanonicalEventOrder();

    /**
     * Returns timestamp in nano seconds since an arbitrary start time.
     *
     * @return timestamp in nano seconds
     * @see System#nanoTime()
     * @deprecated prefer {@link TimeStampedEvent#getTimeStampMillis()}
     */
    @Deprecated
    Long getTimeStamp();
}