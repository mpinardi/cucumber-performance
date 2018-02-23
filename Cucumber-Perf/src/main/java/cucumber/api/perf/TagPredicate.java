package cucumber.api.perf;

import io.cucumber.tagexpressions.Expression;
import io.cucumber.tagexpressions.TagExpressionParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cucumber.api.perf.salad.ast.Plan;
import cucumber.api.perf.salad.ast.Simulation;
import cucumber.api.perf.salad.ast.SimulationPeriod;
import cucumber.runtime.TagExpressionOld;
import gherkin.ast.Node;
import gherkin.ast.Tag;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleTag;

import static java.util.Arrays.asList;


public class TagPredicate implements Predicate {
    private final List<Expression> expressions = new ArrayList<Expression>();
    private final List<TagExpressionOld> oldStyleExpressions = new ArrayList<TagExpressionOld>();

    public TagPredicate(List<String> tagExpressions) {
        if (tagExpressions == null) {
            return;
        }
        TagExpressionParser parser = new TagExpressionParser();
        for (String tagExpression : tagExpressions) {
            if (TagExpressionOld.isOldTagExpression(tagExpression)) {
                oldStyleExpressions.add(new TagExpressionOld(asList(tagExpression)));
            } else {
                expressions.add(parser.parse(tagExpression));
            }
        }
    }

    @Override
    public boolean apply(Node n) {
    	if (n instanceof Simulation)
    	{
    		return apply(((Simulation) n).getTags());
    	}
    	else if (n instanceof SimulationPeriod)
    	{
    		return apply(((SimulationPeriod) n).getTags());
    	}
     	else if (n instanceof Plan)
    	{
    		return apply(((Plan) n).getTags());
    	}
     	else return false; 
    }

    public boolean apply(Collection<Tag> tags) {
        for (TagExpressionOld oldStyleExpression : oldStyleExpressions) {
        	List<PickleTag> ptags = new ArrayList<PickleTag>();
        	for (Tag tag : tags)
        	{
        		PickleTag pt = new PickleTag(new PickleLocation(tag.getLocation().getColumn(), tag.getLocation().getLine()), tag.getName());
        		ptags.add(pt);
        	}
        	
            if (!oldStyleExpression.evaluate(ptags)) {
                return false;
            }
        }
        List<String> ntags = new ArrayList<String>();
        for (Tag tag : tags) {
            ntags.add(tag.getName());
        }
        for (Expression expression : expressions) {
            if (!expression.evaluate(ntags)) {
                return false;
            }
        }
        return true;
    }

}

