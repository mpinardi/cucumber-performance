package cucumber.api.perf;

	import java.lang.annotation.ElementType;
	import java.lang.annotation.Retention;
	import java.lang.annotation.RetentionPolicy;
	import java.lang.annotation.Target;

import cucumber.api.SnippetType;

	/**
	 * This annotation provides the same options as the cucumber command line, {@link cucumber.api.cli.Main}.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.TYPE})
	public @interface CucumberPerfOptions {
	    /**
	     * @return true if this is a dry run
	     */
	    boolean dryRun() default false;

	    /**
	     * @return the uris to the feature(s)
	     */
	    String[] plans() default {};

	    /**
	     * @return what tags in the features should be executed
	     */
	    String[] tags() default {};

	    /**
	     * Specify a patternfilter for features or scenarios
	     *
	     * @return a list of patterns
	     */
	    String[] name() default {};

	    /**
	     * @return what format should the snippets use. underscore, camelcase
	     */
	    SnippetType snippets() default SnippetType.UNDERSCORE;
	}

