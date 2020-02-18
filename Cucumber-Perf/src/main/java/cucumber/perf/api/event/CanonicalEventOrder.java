package cucumber.perf.api.event;

import java.util.Comparator;
import java.util.List;

import static java.util.Arrays.asList;

final class CanonicalEventOrder implements Comparator<Event> {

    private static final FixedEventOrderComparator fixedOrder = new FixedEventOrderComparator();

    @Override
    public int compare(Event a, Event b) {
        int fixedOrder = CanonicalEventOrder.fixedOrder.compare(a, b);
         return fixedOrder;
    }

    private static final class FixedEventOrderComparator implements Comparator<Event> {

        private final List<Class<? extends Event>> fixedOrder = asList(
            (Class<? extends Event>)
            PerfRunStarted.class,
            TestSourceRead.class,
            ConfigStatistics.class,
            SimulationStarted.class,
            GroupEvent.class,
            SimulationFinished.class,
            StatisticsEvent.class,
            PerfRunFinished.class
        );

        @Override
        public int compare(final Event a, final Event b) {
            return Integer.compare(requireInFixOrder(a.getClass()), requireInFixOrder(b.getClass()));
        }

        private int requireInFixOrder(Class<? extends Event> o) {
            int index = findInFixedOrder(o);
            if (index < 0) {
                throw new IllegalStateException(o + "was not in " + fixedOrder);
            }
            return index;
        }

        private int findInFixedOrder(Class<? extends Event> o) {
            for (int i = 0; i < fixedOrder.size(); i++) {
                if (fixedOrder.get(i).isAssignableFrom(o)) {
                    return i;
                }
            }
            return -1;
        }
    }
}
