package cucumber.perf.runtime;

import cucumber.perf.api.event.Event;
import cucumber.perf.api.event.EventBus;

abstract class AbstractEventBus extends BaseEventPublisher implements EventBus {

    @Override
    public void send(Event event) {
        super.send(event);
    }

    @Override
    public void sendAll(Iterable<Event> queue) {
        super.sendAll(queue);
    }
}