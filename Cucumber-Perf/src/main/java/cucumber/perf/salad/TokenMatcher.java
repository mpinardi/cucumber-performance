package cucumber.perf.salad;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cucumber.perf.salad.Parser.ITokenMatcher;
import cucumber.perf.salad.Parser.TokenType;
import io.cucumber.core.internal.gherkin.GherkinLanguageConstants;
import io.cucumber.core.internal.gherkin.GherkinLineSpan;
import io.cucumber.core.internal.gherkin.ast.Location;


public class TokenMatcher implements ITokenMatcher {
    private static final Pattern LANGUAGE_PATTERN = Pattern.compile("^\\s*#\\s*language\\s*:\\s*([a-zA-Z\\-_]+)\\s*$");
    private final ISaladDialectProvider dialectProvider;
    private SaladDialect currentDialect;
    private String activeDocStringSeparator = null;
    private int indentToRemove = 0;

    public TokenMatcher(ISaladDialectProvider dialectProvider) {
        this.dialectProvider = dialectProvider;
        reset();
    }

    public TokenMatcher() {
        this(new SaladDialectProvider());
    }

    public TokenMatcher(String defaultDialectName) {
        this(new SaladDialectProvider(defaultDialectName));
    }

    @Override
    public void reset() {
        activeDocStringSeparator = null;
        indentToRemove = 0;
        currentDialect = dialectProvider.getDefaultDialect();
    }

    public SaladDialect getCurrentDialect() {
        return currentDialect;
    }

    protected void setTokenMatched(Token token, TokenType matchedType, String text, String keyword, Integer indent, List<GherkinLineSpan> items) {
        token.matchedType = matchedType;
        token.matchedKeyword = keyword;
        token.matchedText = text;
        token.mathcedItems = items;
        token.matchedSaladDialect = getCurrentDialect();
        token.matchedIndent = indent != null ? indent : (token.line == null ? 0 : token.line.indent());
        token.location = new Location(token.location.getLine(), token.matchedIndent + 1);
    }

    @Override
    public boolean match_EOF(Token token) {
        if (token.isEOF()) {
            setTokenMatched(token, TokenType.EOF, null, null, null, null);
            return true;
        }
        return false;
    }

    @Override
    public boolean match_Other(Token token) {
        String text = token.line.getLineText(indentToRemove); //take the entire line, except removing DocString indents
        setTokenMatched(token, TokenType.Other, unescapeDocString(text), null, 0, null);
        return true;
    }

    @Override
    public boolean match_Empty(Token token) {
        if (token.line.isEmpty()) {
            setTokenMatched(token, TokenType.Empty, null, null, null, null);
            return true;
        }
        return false;
    }

    @Override
    public boolean match_Comment(Token token) {
        if (token.line.startsWith(GherkinLanguageConstants.COMMENT_PREFIX)) {
            String text = token.line.getLineText(0); //take the entire line
            setTokenMatched(token, TokenType.Comment, text, null, 0, null);
            return true;
        }
        return false;
    }

    @Override
    public boolean match_Language(Token token) {
        Matcher matcher = LANGUAGE_PATTERN.matcher(token.line.getLineText(0));
        if (matcher.matches()) {
            String language = matcher.group(1);
            setTokenMatched(token, TokenType.Language, language, null, null, null);

            currentDialect = dialectProvider.getDialect(language, token.location);
            return true;
        }
        return false;
    }

    @Override
    public boolean match_TagLine(Token token) {
        if (token.line.startsWith(GherkinLanguageConstants.TAG_PREFIX)) {
            setTokenMatched(token, TokenType.TagLine, null, null, null, token.line.getTags());
            return true;
        }
        return false;
    }

    private boolean matchTitleLine(Token token, TokenType tokenType, List<String> keywords) {
        for (String keyword : keywords) {
            if (token.line.startsWithTitleKeyword(keyword)) {
                String title = token.line.getRestTrimmed(keyword.length() + GherkinLanguageConstants.TITLE_KEYWORD_SEPARATOR.length());
                setTokenMatched(token, tokenType, title, keyword, null, null);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean match_DocStringSeparator(Token token) {
        return activeDocStringSeparator == null
                // open
                ? match_DocStringSeparator(token, GherkinLanguageConstants.DOCSTRING_SEPARATOR, true) ||
                match_DocStringSeparator(token, GherkinLanguageConstants.DOCSTRING_ALTERNATIVE_SEPARATOR, true)
                // close
                : match_DocStringSeparator(token, activeDocStringSeparator, false);
    }

    private boolean match_DocStringSeparator(Token token, String separator, boolean isOpen) {
        if (token.line.startsWith(separator)) {
            String contentType = null;
            if (isOpen) {
                contentType = token.line.getRestTrimmed(separator.length());
                activeDocStringSeparator = separator;
                indentToRemove = token.line.indent();
            } else {
                activeDocStringSeparator = null;
                indentToRemove = 0;
            }

            setTokenMatched(token, TokenType.DocStringSeparator, contentType, null, null, null);
            return true;
        }
        return false;
    }

   /* @Override
    public boolean match_StepLine(Token token) {
        List<String> keywords = currentDialect.getStepKeywords();
        for (String keyword : keywords) {
            if (token.line.startsWith(keyword)) {
                String stepText = token.line.getRestTrimmed(keyword.length());
                setTokenMatched(token, TokenType.StepLine, stepText, keyword, null, null);
                return true;
            }
        }
        return false;
    }*/

    @Override
    public boolean match_TableRow(Token token) {
        if (token.line.startsWith(GherkinLanguageConstants.TABLE_CELL_SEPARATOR)) {
            setTokenMatched(token, TokenType.TableRow, null, null, null, token.line.getTableCells());
            return true;
        }
        return false;
    }

    private String unescapeDocString(String text) {
        return activeDocStringSeparator != null ? text.replace("\\\"\\\"\\\"", "\"\"\"") : text;
    }

	@Override
	public boolean match_PlanLine(Token token) {
		 return matchTitleLine(token, TokenType.PlanLine, currentDialect.getPlanKeywords());
	}

	@Override
	public boolean match_SimulationLine(Token token) {
		 return matchTitleLine(token, TokenType.SimulationLine, currentDialect.getSimulationKeywords());
	}

	@Override
	public boolean match_GroupLine(Token token) {
		 List<String> keywords = currentDialect.getGroupKeywords();
	        for (String keyword : keywords) {
	            if (token.line.startsWith(keyword)) {
	                String stepText = token.line.getRestTrimmed(keyword.length());
	                setTokenMatched(token, TokenType.GroupLine, stepText, keyword, null, null);
	                return true;
	            }
	        }
	        return false;
	}

	@Override
	public boolean match_SimulationPeriodLine(Token token) {
		 return matchTitleLine(token, TokenType.SimulationPeriodLine, currentDialect.getSimulationPeriodKeywords());
	}

	@Override
	public boolean match_TimeLine(Token token) {
		 return matchTitleLine(token, TokenType.TimeLine, currentDialect.getTimeKeywords());
	}

	@Override
	public boolean match_CountLine(Token token) {
		 return matchTitleLine(token, TokenType.CountLine, currentDialect.getCountKeywords());
	}

	@Override
	public boolean match_RunnersLine(Token token) {
		 return matchTitleLine(token, TokenType.RunnersLine, currentDialect.getRunnersKeywords());
	}

	@Override
	public boolean match_RampUpLine(Token token) {
		return matchTitleLine(token, TokenType.RampUpLine, currentDialect.getRampUpKeywords());
	}

	@Override
	public boolean match_RampDownLine(Token token) {
		return matchTitleLine(token, TokenType.RampDownLine, currentDialect.getRampDownKeywords());
	}

	@Override
	public boolean match_SynchronizedLine(Token token) {
		return matchTitleLine(token, TokenType.SynchronizedLine, currentDialect.getSynchronizedKeywords());
	}

	@Override
	public boolean match_RandomWaitLine(Token token) {
		return matchTitleLine(token, TokenType.RandomWaitLine, currentDialect.getRandomWaitKeywords());
	}
	
	/* @Override
    public boolean match_FeatureLine(Token token) {
        return matchTitleLine(token, TokenType.FeatureLine, currentDialect.getFeatureKeywords());
    }

    @Override
    public boolean match_ScenarioLine(Token token) {
        return matchTitleLine(token, TokenType.ScenarioLine, currentDialect.getScenarioKeywords());
    }

    @Override
    public boolean match_ScenarioOutlineLine(Token token) {
        return matchTitleLine(token, TokenType.ScenarioOutlineLine, currentDialect.getScenarioOutlineKeywords());
    }

    @Override
    public boolean match_ExamplesLine(Token token) {
        return matchTitleLine(token, TokenType.ExamplesLine, currentDialect.getExamplesKeywords());
    }
*/
}
