package cucumber.perf.api.event;

public interface EventHandler<T extends Event> {

    void receive(T event);

}