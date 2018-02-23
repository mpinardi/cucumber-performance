package cucumber.api.perf.salad.ast;

import java.util.Collections;
import java.util.List;

import gherkin.ast.Comment;
import gherkin.ast.Node;

public class SaladDocument extends Node {

	private final Plan plan;
	private final List<Comment> comments;

	public SaladDocument(
			Plan plan,
			List<Comment> comments) {
		super(null);
		this.plan = plan;
		this.comments = Collections.unmodifiableList(comments);
	}

	public Plan getPlan() {
		return plan;
	}

	public List<Comment> getComments() {
		return comments;
	}
}