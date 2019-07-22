package cucumber.perf.api;

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
	     * @return true if only successes should be reported in statistics
	     */
	    boolean strict() default true;

	    /**
	     * @return true if monochorme formatters.
	     */
	    boolean monochrome() default false;
	    
	    /**
	     * @return true if scenario failures should fail a group
	     */
	    boolean failfast() default false;
	    
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
	     * @return what plugins(s) to use
	     */
	    String[] plugin() default {};
	    
	    /**
	     * @return what format should the snippets use. underscore, camelcase
	     */
	    SnippetType snippets() default SnippetType.UNDERSCORE;
	}

