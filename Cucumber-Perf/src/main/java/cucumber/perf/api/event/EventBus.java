package cucumber.perf.api.event;

public interface EventBus extends EventPublisher {

    Long getTime();

    Long getTimeMillis();

    void send(Event event);

    void sendAll(Iterable<Event> queue);

}