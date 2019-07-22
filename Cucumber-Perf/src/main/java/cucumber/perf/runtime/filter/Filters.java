package cucumber.perf.runtime.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import cucumber.perf.runtime.PerfRuntimeOptions;
import cucumber.perf.runtime.filter.Predicate;
import gherkin.ast.Node;

public class Filters {
	private final List<Predicate> filters;

	public Filters(PerfRuntimeOptions options) {
		filters = new ArrayList<>();
		List<String> tagFilters = options.getTagFilters();
		if (!tagFilters.isEmpty()) {
			this.filters.add(new TagPredicate(tagFilters));
		}
		List<Pattern> nameFilters = options.getNameFilters();
		if (!nameFilters.isEmpty()) {
			this.filters.add(new NamePredicate(nameFilters));
		}
	}

	public boolean matchesFilters(Node n) {
		for (Predicate filter : filters) {
			if (!filter.apply(n)) {
				return false;
			}
		}
		return true;
	}

}
