package cucumber.formatter;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;

public class NullFormatter implements ConcurrentEventListener {
    public NullFormatter() {
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
    }
}