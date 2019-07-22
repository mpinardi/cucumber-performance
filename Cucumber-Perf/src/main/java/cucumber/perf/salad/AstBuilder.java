package cucumber.perf.salad;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import cucumber.perf.salad.Parser.Builder;
import cucumber.perf.salad.Parser.RuleType;
import cucumber.perf.salad.Parser.TokenType;
import cucumber.perf.salad.ast.Count;
import cucumber.perf.salad.ast.Group;
import cucumber.perf.salad.ast.Plan;
import cucumber.perf.salad.ast.Runners;
import cucumber.perf.salad.ast.SaladDocument;
import cucumber.perf.salad.ast.Simulation;
import cucumber.perf.salad.ast.SimulationDefinition;
import cucumber.perf.salad.ast.SimulationPeriod;
import cucumber.perf.salad.ast.Time;
import gherkin.GherkinLineSpan;
import gherkin.StringUtils;
import gherkin.ast.Comment;
import gherkin.ast.DataTable;
import gherkin.ast.DocString;
import gherkin.ast.Location;
import gherkin.ast.Node;
import gherkin.ast.TableCell;
import gherkin.ast.TableRow;
import gherkin.ast.Tag;

import static gherkin.StringUtils.join;

public class AstBuilder implements Builder<SaladDocument> {
    private Deque<AstNode> stack;
    private List<Comment> comments;

    public AstBuilder() {
        reset();
    }

    @Override
    public void reset() {
        stack = new ArrayDeque<>();
        stack.push(new AstNode(RuleType.None));

        comments = new ArrayList<>();
    }

    private AstNode currentNode() {
        return stack.peek();
    }

    @Override
    public void build(Token token) {
        RuleType ruleType = RuleType.cast(token.matchedType);
        if (token.matchedType == TokenType.Comment) {
            comments.add(new Comment(getLocation(token, 0), token.matchedText));
        } else {
            currentNode().add(ruleType, token);
        }
    }

    @Override
    public void startRule(RuleType ruleType) {
        stack.push(new AstNode(ruleType));
    }

    @Override
    public void endRule(RuleType ruleType) {
        AstNode node = stack.pop();
        Object transformedNode = getTransformedNode(node);
        currentNode().add(node.ruleType, transformedNode);
    }

    private Object getTransformedNode(AstNode node) {
        switch (node.ruleType) {
            case Group: {
                Token stepLine = node.getToken(TokenType.GroupLine);
                List<Node> stepArgs = new ArrayList<Node>();
                
                Node stepArg = node.getSingle(RuleType.DataTable, null);
                if (stepArg != null)
                {
                	stepArgs.add(stepArg);
                }
                
                stepArg = node.getSingle(RuleType.Runners, null);
                if (stepArg != null)
                {
                	stepArgs.add(stepArg);
                }
                
                stepArg = node.getSingle(RuleType.Count, null);
                if (stepArg != null)
                {
                	stepArgs.add(stepArg);
                }
                return new Group(getLocation(stepLine, 0), stepLine.matchedKeyword, stepLine.matchedText, stepArgs);
            }
    		case Count:
    		{
                 Token countLine = node.getToken(TokenType.CountLine);
    			return new Count(getLocation(countLine, 0),countLine.matchedKeyword,countLine.matchedText);
    		}
    		case Runners:
    		{
                Token runnerLine = node.getToken(TokenType.RunnersLine);
                return new Runners(getLocation(runnerLine, 0),runnerLine.matchedKeyword,runnerLine.matchedText);
    		}
            case DocString: {
                Token separatorToken = node.getTokens(TokenType.DocStringSeparator).get(0);
                String contentType = separatorToken.matchedText.length() > 0 ? separatorToken.matchedText : null;
                List<Token> lineTokens = node.getTokens(TokenType.Other);
                StringBuilder content = new StringBuilder();
                boolean newLine = false;
                for (Token lineToken : lineTokens) {
                    if (newLine) content.append("\n");
                    newLine = true;
                    content.append(lineToken.matchedText);
                }
                return new DocString(getLocation(separatorToken, 0), contentType, content.toString());
            }
            case DataTable: {
                List<TableRow> rows = getTableRows(node);
                return new DataTable(rows);
            }
            case Simulation_Definition: {
                List<Tag> tags = getTags(node);
                AstNode simNode = node.getSingle(RuleType.Simulation, null);
                
                if (simNode != null) {
                    Token simLine = simNode.getToken(TokenType.SimulationLine);
                    String description = getDescription(simNode);
                    List<Group> steps = getGroups(simNode);
                    List<Time> rampUp= simNode.getItems(RuleType.RampUp);
                    List<Time> rampDown= simNode.getItems(RuleType.RampDown);
                    List<Count> synchronize= simNode.getItems(RuleType.Synchronized);
                    List<Time> randomWait= simNode.getItems(RuleType.RandomWait);
                    return new Simulation(tags, getLocation(simLine, 0), simLine.matchedKeyword, simLine.matchedText, description, steps,(rampUp != null&& rampUp.size()>0) ? rampUp.get(0) : null,(rampDown != null&& rampDown.size()>0) ? rampDown.get(0) : null,(synchronize != null&& synchronize.size()>0) ? synchronize.get(0) : null,(randomWait != null&& randomWait.size()>0) ? randomWait.get(0) : null);
                } else {
                    AstNode simPeriodNode = node.getSingle(RuleType.SimulationPeriod, null);
                    if (simPeriodNode == null) {
                        throw new RuntimeException("Internal grammar error");
                    }
                    Token simPeriodLine = simPeriodNode.getToken(TokenType.SimulationPeriodLine);
                    String description = getDescription(simPeriodNode);
                    List<Group> steps = getGroups(simPeriodNode);
                    List<Time> times= simPeriodNode.getItems(RuleType.Time);
                    List<Time> rampUp= simPeriodNode.getItems(RuleType.RampUp);
                    List<Time> rampDown= simPeriodNode.getItems(RuleType.RampDown);
                    List<Count> synchronize= simPeriodNode.getItems(RuleType.Synchronized);
                    List<Time> randomWait= simPeriodNode.getItems(RuleType.RandomWait);
                    return new SimulationPeriod(tags, getLocation(simPeriodLine, 0), simPeriodLine.matchedKeyword, simPeriodLine.matchedText, description, steps, times.get(0),(rampUp != null&& rampUp.size()>0) ? rampUp.get(0) : null,(rampDown != null&& rampDown.size()>0) ? rampDown.get(0) : null,(synchronize != null&& synchronize.size()>0) ? synchronize.get(0) : null,(randomWait != null&& randomWait.size()>0) ? randomWait.get(0) : null);
                }

            }
            case Description: {
                List<Token> lineTokens = node.getTokens(TokenType.Other);
                // Trim trailing empty lines
                int end = lineTokens.size();
                while (end > 0 && lineTokens.get(end - 1).matchedText.matches("\\s*")) {
                    end--;
                }
                lineTokens = lineTokens.subList(0, end);

                return join(new StringUtils.ToString<Token>() {
                    @Override
                    public String toString(Token t) {
                        return t.matchedText;
                    }
                }, "\n", lineTokens);
            }
            case Plan: {
                AstNode header = node.getSingle(RuleType.Plan_Header, new AstNode(RuleType.Plan_Header));
                if (header == null) return null;
                List<Tag> tags = getTags(header);
                Token planLine = header.getToken(TokenType.PlanLine);
                if (planLine == null) return null;
                List<SimulationDefinition> scenarioDefinitions = new ArrayList<>();
 
                scenarioDefinitions.addAll(node.<SimulationDefinition>getItems(RuleType.Simulation_Definition));
                String description = getDescription(header);
                if (planLine.matchedSaladDialect == null) return null;
                String language = planLine.matchedSaladDialect.getLanguage();

                return new Plan(tags, getLocation(planLine, 0), language, planLine.matchedKeyword, planLine.matchedText, description, scenarioDefinitions);
            }
            case SaladDocument: {
                Plan feature = node.getSingle(RuleType.Plan, null);

                return new SaladDocument(feature, comments);
            }
			case Time: {
				//AstNode timeNode = node.getSingle(RuleType.Time, null);
	            Token timeLine = node.getToken(TokenType.TimeLine);
	
				return new Time(getLocation(timeLine, 0),timeLine.matchedKeyword,timeLine.matchedText);
	        }
			case RampUp: {
                Token rupLine = node.getToken(TokenType.RampUpLine);

				return new Time(getLocation(rupLine, 0),rupLine.matchedKeyword,rupLine.matchedText);
		    }
			case RampDown: {
                Token rdwnLine = node.getToken(TokenType.RampDownLine);
			
				return new Time(getLocation(rdwnLine, 0),rdwnLine.matchedKeyword,rdwnLine.matchedText);
        	}
			case Synchronized: {
                Token syncLine = node.getToken(TokenType.SynchronizedLine);

				return new Count(getLocation(syncLine, 0),syncLine.matchedKeyword,syncLine.matchedText);
		    }
			case RandomWait: {
                Token rwLine = node.getToken(TokenType.RandomWaitLine);
			
				return new Time(getLocation(rwLine, 0),rwLine.matchedKeyword,rwLine.matchedText);
    		}
		default:
			break;
			}
        return node;
    }

    private List<TableRow> getTableRows(AstNode node) {
        List<TableRow> rows = new ArrayList<>();
        for (Token token : node.getTokens(TokenType.TableRow)) {
            rows.add(new TableRow(getLocation(token, 0), getCells(token)));
        }
        ensureCellCount(rows);
        return rows;
    }

    private void ensureCellCount(List<TableRow> rows) {
        if (rows.isEmpty()) return;

        int cellCount = rows.get(0).getCells().size();
        for (TableRow row : rows) {
            if (row.getCells().size() != cellCount) {
                throw new ParserException.AstBuilderException("inconsistent cell count within the table", row.getLocation());
            }
        }
    }

    private List<TableCell> getCells(Token token) {
        List<TableCell> cells = new ArrayList<>();
        for (GherkinLineSpan cellItem : token.mathcedItems) {
            cells.add(new TableCell(getLocation(token, cellItem.column), cellItem.text));
        }
        return cells;
    }

    private List<Group> getGroups(AstNode node) {
        return node.getItems(RuleType.Group);
    }

    private Location getLocation(Token token, int column) {
        return column == 0 ? token.location : new Location(token.location.getLine(), column);
    }

    private String getDescription(AstNode node) {
        return node.getSingle(RuleType.Description, null);
    }

    private List<Tag> getTags(AstNode node) {
        AstNode tagsNode = node.getSingle(RuleType.Tags, new AstNode(RuleType.None));
        if (tagsNode == null)
            return new ArrayList<>();

        List<Token> tokens = tagsNode.getTokens(TokenType.TagLine);
        List<Tag> tags = new ArrayList<>();
        for (Token token : tokens) {
            for (GherkinLineSpan tagItem : token.mathcedItems) {
                tags.add(new Tag(getLocation(token, tagItem.column), tagItem.text));
            }
        }
        return tags;
    }

    @Override
    public SaladDocument getResult() {
        return currentNode().getSingle(RuleType.SaladDocument, null);
    }
}

