package cucumber.formatter;

import cucumber.api.event.ConcurrentEventListener;
import cucumber.api.event.EventPublisher;

public class NullFormatter implements ConcurrentEventListener {
    public NullFormatter() {
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
    }
}