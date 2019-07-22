package cucumber.perf.runtime.filter;
import org.junit.Test;

import cucumber.perf.salad.ast.Simulation;

import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
public class NamePredicateTest {
 
    @Test
    public void anchored_name_pattern_matches_exact_name() {
    	Simulation n = mock(Simulation.class);
        NamePredicate predicate = new NamePredicate(asList(Pattern.compile("^a name$")));
        when(n.getName()).thenReturn("a name");
        assertTrue(predicate.apply(n));
    }

    @Test
    public void anchored_name_pattern_does_not_match_part_of_name() {
    	Simulation n = mock(Simulation.class);
        //PickleEvent pickleEvent = createSimulationWithName("a name with suffix");
        NamePredicate predicate = new NamePredicate(asList(Pattern.compile("^a name$")));
        when(n.getName()).thenReturn("a name with suffix");
        assertFalse(predicate.apply(n));
    }

    @Test
    public void non_anchored_name_pattern_matches_part_of_name() {
    	Simulation n = mock(Simulation.class);
        //PickleEvent pickleEvent = createPickleWithName("a pickle name with suffix");
        NamePredicate predicate = new NamePredicate(asList(Pattern.compile("a name")));
        when(n.getName()).thenReturn("a name with suffix");
        assertTrue(predicate.apply(n));
    }

    @Test
    public void wildcard_name_pattern_matches_part_of_name() {
    	Simulation n = mock(Simulation.class);
        //PickleEvent pickleEvent = createPickleWithName("a pickleEvent name");
        NamePredicate predicate = new NamePredicate(asList(Pattern.compile("a .* name")));
        when(n.getName()).thenReturn("a pickleEvent name");
        assertTrue(predicate.apply(n));
    }

}