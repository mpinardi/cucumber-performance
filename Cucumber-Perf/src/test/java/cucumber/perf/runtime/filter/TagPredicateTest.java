package cucumber.perf.runtime.filter;

import java.util.Collections;

import org.junit.Test;

import cucumber.perf.salad.ast.Simulation;
import io.cucumber.core.internal.gherkin.ast.Location;
import io.cucumber.core.internal.gherkin.ast.Tag;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TagPredicateTest {
    private static final String FOO_TAG_VALUE = "@FOO";
    private static final Location MOCK_LOCATION = mock(Location.class);
    private static final Tag FOO_TAG = new Tag(MOCK_LOCATION, FOO_TAG_VALUE);
    private static final String BAR_TAG_VALUE = "@BAR";
    private static final Tag BAR_TAG = new Tag(MOCK_LOCATION, BAR_TAG_VALUE);
    private static final String NOT_FOO_TAG_VALUE = "not @FOO";
    private static final String FOO_OR_BAR_TAG_VALUE = "@FOO or @BAR";
    private static final String FOO_AND_BAR_TAG_VALUE = "@FOO and @BAR";
    private static final String OLD_STYLE_NOT_FOO_TAG_VALUE = "~@FOO";
    private static final String OLD_STYLE_FOO_OR_BAR_TAG_VALUE = "@FOO,@BAR";

    @Test
    public void empty_tag_predicate_matches_pickle_with_any_tags() {
        TagPredicate predicate = new TagPredicate(null);
        Simulation n = mock(Simulation.class);
        when(n.getTags()).thenReturn(asList(FOO_TAG));
        assertTrue(predicate.apply(n));
    }

    @Test
    public void single_tag_predicate_does_not_match_pickle_with_no_tags() {
        TagPredicate predicate = new TagPredicate(asList(FOO_TAG_VALUE));
        Simulation n = mock(Simulation.class);
        when(n.getTags()).thenReturn(Collections.<Tag>emptyList());
        assertFalse(predicate.apply(n));
    }

    @Test
    public void single_tag_predicate_matches_pickle_with_same_single_tag() {
        TagPredicate predicate = new TagPredicate(asList(FOO_TAG_VALUE));
        Simulation n = mock(Simulation.class);
        when(n.getTags()).thenReturn(asList(FOO_TAG));
        assertTrue(predicate.apply(n));
    }

    @Test
    public void single_tag_predicate_matches_pickle_with_more_tags() {
        TagPredicate predicate = new TagPredicate(asList(FOO_TAG_VALUE));
        Simulation n = mock(Simulation.class);
        when(n.getTags()).thenReturn(asList(FOO_TAG, BAR_TAG));
        assertTrue(predicate.apply(n));
    }

    @Test
    public void single_tag_predicate_does_not_match_pickle_with_different_single_tag() {
        TagPredicate predicate = new TagPredicate(asList(FOO_TAG_VALUE));
        Simulation n = mock(Simulation.class);
        when(n.getTags()).thenReturn(asList(BAR_TAG));
        assertFalse(predicate.apply(n));
    }

    @Test
    public void not_tag_predicate_matches_pickle_with_no_tags() {
        TagPredicate predicate = new TagPredicate(asList(NOT_FOO_TAG_VALUE));
        Simulation n = mock(Simulation.class);
        when(n.getTags()).thenReturn(Collections.<Tag>emptyList());
        assertTrue(predicate.apply(n));
    }

    @Test
    public void not_tag_predicate_does_not_match_pickle_with_same_single_tag() {
        TagPredicate predicate = new TagPredicate(asList(NOT_FOO_TAG_VALUE));
        Simulation n = mock(Simulation.class);
        when(n.getTags()).thenReturn(asList(FOO_TAG));
        assertFalse(predicate.apply(n));
    }

    @Test
    public void not_tag_predicate_matches_pickle_with_different_single_tag() {
        TagPredicate predicate = new TagPredicate(asList(NOT_FOO_TAG_VALUE));
        Simulation n = mock(Simulation.class);
        when(n.getTags()).thenReturn(asList(BAR_TAG));
        assertTrue(predicate.apply(n));
    }

    @Test
    public void and_tag_predicate_matches_pickle_with_all_tags() {
        TagPredicate predicate = new TagPredicate(asList(FOO_AND_BAR_TAG_VALUE));
        Simulation n = mock(Simulation.class);
        when(n.getTags()).thenReturn(asList(FOO_TAG, BAR_TAG));
        assertTrue(predicate.apply(n));
    }

    @Test
    public void and_tag_predicate_does_not_match_pickle_with_one_of_the_tags() {
        TagPredicate predicate = new TagPredicate(asList(FOO_AND_BAR_TAG_VALUE));
        Simulation n = mock(Simulation.class);
        when(n.getTags()).thenReturn(asList(FOO_TAG));
        assertFalse(predicate.apply(n));
    }

    @Test
    public void or_tag_predicate_matches_pickle_with_one_of_the_tags() {
        TagPredicate predicate = new TagPredicate(asList(FOO_OR_BAR_TAG_VALUE));
        Simulation n = mock(Simulation.class);
        when(n.getTags()).thenReturn(asList(FOO_TAG));
        assertTrue(predicate.apply(n));
    }

    @Test
    public void or_tag_predicate_does_not_match_pickle_none_of_the_tags() {
        TagPredicate predicate = new TagPredicate(asList(FOO_OR_BAR_TAG_VALUE));
        Simulation n = mock(Simulation.class);
        when(n.getTags()).thenReturn(Collections.<Tag>emptyList());
        assertFalse(predicate.apply(n));
    }

    @Test
    public void old_style_not_tag_predicate_is_handled() {
        TagPredicate predicate = new TagPredicate(asList(OLD_STYLE_NOT_FOO_TAG_VALUE));
        Simulation n = mock(Simulation.class);
        when(n.getTags()).thenReturn(asList(BAR_TAG));
        assertTrue(predicate.apply(n));
    }

    @Test
    public void old_style_or_tag_predicate_is_handled() {
        TagPredicate predicate = new TagPredicate(asList(OLD_STYLE_FOO_OR_BAR_TAG_VALUE));
        Simulation n = mock(Simulation.class);
        when(n.getTags()).thenReturn(asList(FOO_TAG));
        assertTrue(predicate.apply(n));
    }

    @Test
    public void multiple_tag_expressions_are_combined_with_and() {
        TagPredicate predicate = new TagPredicate(asList(FOO_TAG_VALUE, BAR_TAG_VALUE));
        Simulation n = mock(Simulation.class);
        when(n.getTags()).thenReturn(asList(FOO_TAG, BAR_TAG));
        assertTrue(predicate.apply(n));
    }

    @Test
    public void old_and_new_style_tag_expressions_can_be_combined() {
        TagPredicate predicate = new TagPredicate(asList(BAR_TAG_VALUE, OLD_STYLE_NOT_FOO_TAG_VALUE));
        Simulation n = mock(Simulation.class);
        when(n.getTags()).thenReturn(asList(BAR_TAG));
        assertTrue(predicate.apply(n));
    }

}