package cucumber.perf.api.event;

public interface EventPublisher {

    /**
     * Registers an event handler for a specific event.
     * <p>
     * The available events types are:
     * <ul>
     * <li>{@link Event} - all events.
     * <li>{@link PerfRunStarted} - the first event sent.
     * <li>{@link SimulationStarted} - sent before starting the execution of a Simulation, contains the simulation name
     * <li>{@link GroupStarted} - sent before starting the execution of a Group, contains the PerfGroup
     * <li>{@link GroupFinished} - sent after the execution of a Group, contains the PerfGroup and its Result.
     * <li>{@link SimulationFinished} - sent after the execution of a Simulation, contains its Result.
     * <li>{@link StatisticsStarted} - sent before starting creation of Statistics.
     * <li>{@link StatisticsFinished} - sent after the creation of Statistics, contains its Statistics.
     * <li>{@link PerfRunFinished} - the last event sent.
     * </ul>
     *
     * @param eventType the event type for which the handler is being registered
     * @param handler   the event handler
     * @param <T>       the event type
     * @see Event
     */
    <T extends Event> void registerHandlerFor(Class<T> eventType, EventHandler<T> handler);

    /**
     * Unregister an event handler for a specific event
     *
     * @param eventType the event type for which the handler is being registered
     * @param handler   the event handler
     * @param <T>       the event type
     */
    <T extends Event> void removeHandlerFor(Class<T> eventType, EventHandler<T> handler);

}