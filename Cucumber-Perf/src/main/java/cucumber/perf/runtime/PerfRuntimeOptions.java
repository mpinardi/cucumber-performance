package cucumber.perf.runtime;

import cucumber.perf.api.FixJava;
import cucumber.perf.api.Mapper;
import cucumber.perf.runtime.formatter.PluginFactory;
import cucumber.perf.salad.ISaladDialectProvider;
import cucumber.perf.salad.SaladDialect;
import cucumber.perf.salad.SaladDialectProvider;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.options.PluginOption;
/*import cucumber.runtime.CucumberException;
import cucumber.runtime.Shellwords;
import cucumber.util.FixJava;
import cucumber.util.Mapper;*/
import io.cucumber.datatable.DataTable;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.SummaryPrinter;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/*import static cucumber.util.FixJava.join;
import static cucumber.util.FixJava.map;*/
import static java.util.Arrays.asList;
import static cucumber.perf.api.FixJava.map;
import static cucumber.perf.api.FixJava.join;
// IMPORTANT! Make sure USAGE.txt is always up to date if this class changes.

public class PerfRuntimeOptions {
	public static final String VERSION = ResourceBundle.getBundle("version").getString("cucumber-perf");
	public static final String USAGE_RESOURCE = "/USAGE.txt";

	static String usageText;

	private static final Mapper<String, String> QUOTE_MAPPER = new Mapper<String, String>() {
		@Override
		public String map(String o) {
			return '"' + o + '"';
		}
	};

	private final List<String> tagFilters = new ArrayList<String>();
	private final List<Pattern> nameFilters = new ArrayList<Pattern>();
	private final List<String> planPaths = new ArrayList<String>();
	private final List<String> cucumberOptions = new ArrayList<String>();
	private final List<String> pluginFormatterNames = new ArrayList<String>();
	private final List<String> pluginDisplayNames = new ArrayList<String>();
    private final List<String> pluginSummaryPrinterNames = new ArrayList<String>();
	private boolean dryRun;
	private boolean strict = true;
	private boolean monochrome;
	private boolean failFast;

	public PerfRuntimeOptions() {
		this(new ArrayList<String>());
	}
	
	/**
	 * Create a new instance from a list of options, for example:
	 * <pre>{@code Arrays.asList("name=the fox", "plugin=detail_display);}</pre>
	 * @param argv
	 *            the arguments
	 */
	public PerfRuntimeOptions(List<String> argv) {
		argv = new ArrayList<String>(argv); // in case the one passed in is unmodifiable.
		List<String> ls = parse(argv);
		addCucumberOptions(ls);
   
		if (!pluginFormatterNames.isEmpty()) {
			if (this.findFormatterPlugin("statistics")==-1)
				pluginFormatterNames.add("statistics");
	    } else {
	    	pluginFormatterNames.add("statistics");
	    }
		 
        if (pluginSummaryPrinterNames.isEmpty()) {
            pluginSummaryPrinterNames.add("default_summary");
        }
	}

	private List<String> parse(List<String> args) {
		List<String> list = new ArrayList<String>();
		ParsedPluginData parsedPluginData = new ParsedPluginData();
		for (String arg : args) {
			if (arg.startsWith("tags=") || arg.startsWith("t=")) {
				tagFilters.add(arg.split("=")[1]);
			} else if (arg.startsWith("name=") || arg.startsWith("n=")) {
				String nextArg = arg.split("=")[1];
				Pattern patternFilter = Pattern.compile(nextArg);
				nameFilters.add(patternFilter);
			} else if (arg.startsWith("plans=") || arg.startsWith("p=")) {
				planPaths.add(arg.split("=")[1]);
			} else if (arg.startsWith("plugin=") || arg.startsWith("pg=") || arg.startsWith("add-plugin=")) {
                parsedPluginData.addPluginName(arg.split("=")[1],arg.startsWith("add-plugin="));
			} else if (arg.startsWith("dryrun")) {
				this.dryRun = true;
			} else if (arg.startsWith("monochrome")) {
				this.monochrome = true;
			} else if (arg.startsWith("failfast")) {
				this.failFast = true;
			} else if (arg.equals("help") || arg.equals("h")) {
				printUsage();
				System.exit(0);
			} else if (arg.startsWith("i18n=")) {
				System.exit(printI18n(arg.split("=")[1]));
			} else if (arg.equals("version") || arg.equals("v")) {
				System.out.println(VERSION);
				System.exit(0);
			} else if (arg.equals("no-strict")) {
				this.strict = false;
			} else {
				list.add(arg);
			}
		}
		
	    parsedPluginData.updatePluginFormatterNames(pluginFormatterNames);
	    parsedPluginData.updatePluginDisplayNames(pluginDisplayNames);
	    parsedPluginData.updatePluginSummaryPrinterNames(pluginSummaryPrinterNames);
		return list;
	}

	private void printUsage() {
		loadUsageTextIfNeeded();
		System.out.println(usageText);
	}

	static void loadUsageTextIfNeeded() {
		if (usageText == null) {
			try {
				Reader reader = new InputStreamReader(FixJava.class.getResourceAsStream(USAGE_RESOURCE), "UTF-8");
				usageText = FixJava.readReader(reader);
			} catch (Exception e) {
				usageText = "Could not load usage text: " + e.toString();
			}
		}
	}

	private int printI18n(String language) {
		ISaladDialectProvider dialectProvider = new SaladDialectProvider();
		List<String> languages = dialectProvider.getLanguages();

		if (language.equalsIgnoreCase("help")) {
			for (String code : languages) {
				System.out.println(code);
			}
			return 0;
		}
		if (languages.contains(language)) {
			return printKeywordsFor(dialectProvider.getDialect(language, null));
		}

		System.err.println("Unrecognised ISO language code");
		return 1;
	}

	private int printKeywordsFor(SaladDialect dialect) {
		StringBuilder builder = new StringBuilder();
		//TablePrinter printer = new TablePrinter();
		List<List<String>> table = new ArrayList<List<String>>();
		addKeywordRow(table, "plan", dialect.getPlanKeywords());
		addKeywordRow(table, "simulation", dialect.getSimulationKeywords());
		addKeywordRow(table, "simulation period", dialect.getSimulationPeriodKeywords());
		addKeywordRow(table, "group", dialect.getGroupKeywords());
		addKeywordRow(table, "count", dialect.getCountKeywords());
		addKeywordRow(table, "runners", dialect.getRunnersKeywords());
		addKeywordRow(table, "rampup", dialect.getRampUpKeywords());
		addKeywordRow(table, "rampdown", dialect.getRampDownKeywords());
		addKeywordRow(table, "time", dialect.getTimeKeywords());
		addKeywordRow(table, "synchronized", dialect.getSynchronizedKeywords());
		addKeywordRow(table, "randomwait", dialect.getRandomWaitKeywords());
		//printer.printTable(table, builder);
		DataTable.create(table).print(builder);
		System.out.println(builder.toString());
		return 0;
	}

	private void addKeywordRow(List<List<String>> table, String key, List<String> keywords) {
		List<String> cells = asList(key, join(map(keywords, QUOTE_MAPPER), ", "));
		table.add(cells);
	}
	
	/**
	 * @return All plugin names;
	 */
	public List<String> getPluginsNames(){
		List<String> plugins = new ArrayList<String>();
		plugins.addAll(pluginFormatterNames);
		plugins.addAll(pluginSummaryPrinterNames);
		plugins.addAll(pluginDisplayNames);
		return plugins;
	}
    
	private boolean isCucumberPlugin(String name) throws Exception {
	  PluginOption plugin = PluginOption.parse(name);
		if (SummaryPrinter.class.isAssignableFrom(plugin.pluginClass())) {
			return true;
		}
		if (EventListener.class.isAssignableFrom(plugin.pluginClass()) || ConcurrentEventListener.class.isAssignableFrom(plugin.pluginClass())) {
			return true;
		}
		return false;
	}
	
	private int findFormatterPlugin(String name) {
		for (int i = 0; i < pluginFormatterNames.size(); i++) {
			if (pluginFormatterNames.get(i).toLowerCase().startsWith(name))
				return i;
		}
		return -1;
	}

	/**
	 * @return True if dry run option was passed.
	 */
	public boolean isDryRun() {
		return dryRun;
	}
	
	/**
	 * @return True if monochrome option was passed.
	 */
	public boolean isMonochrome() {
		return monochrome;
	}
	
	/**
	 * @return True else False if no-strict option was passed.
	 */
	public boolean isStrict()
	{ 
		return strict;
	}
	
	/**
	 * @return True if fail fast option was passed.
	 */
	public boolean isFailFast() {
		return failFast;
	}
	
	/**
	 * @return Plan paths
	 */
	public List<String> getPlanPaths() {
		return planPaths;
	}

	/**
	 * @return Name filters
	 */
	public List<Pattern> getNameFilters() {
		return nameFilters;
	}

	/**
	 * @return Tag filters
	 */
	public List<String> getTagFilters() {
		return tagFilters;
	}
	
	/**
	 * @return Cucumber options
	 */
	public List<String> getCucumberOptions() {
		return cucumberOptions;
	}
	
	/**
	 * Add to the plan paths.
	 * @param planPaths A list of path strings to add.
	 * @return self
	 */
	public PerfRuntimeOptions addPlanPaths(List<String> planPaths) {
		this.planPaths.addAll(planPaths);
		return this;
	}

	/**
	 * Add to name filters.
	 * @param nameFilters A list of filter strings to add.
	 * @return self
	 */
	public PerfRuntimeOptions addNameFilters(List<String> nameFilters) {
		for (String nf : nameFilters) {
			Pattern patternFilter = Pattern.compile(nf);
			this.nameFilters.add(patternFilter);
		}
		return this;
	}

	/**
	 * Add to tag filters.
	 * @param tagFilters A list of tag filter strings to add.
	 * @return self
	 */
	public PerfRuntimeOptions addTagFilters(List<String> tagFilters) {
		this.tagFilters.addAll(tagFilters);
		return this;
	}
	
	/**
	 * Add to plugin's.
	 * @param plugins A list of plugin strings to add.
	 * @return self
	 */
	public PerfRuntimeOptions addPlugins(List<String> plugins) {
		for (String plugin : plugins)
		{
			if (PluginFactory.isDisplayName(plugin))
			{
				this.pluginDisplayNames.add(plugin);
			}
			else if (PluginFactory.isFormatterName(plugin))
			{
				if (plugin.toLowerCase().startsWith("statistics")) {
					int i = this.findFormatterPlugin("statistics");
					if (i==-1)
						this.pluginFormatterNames.add(plugin);
					else
						this.pluginFormatterNames.set(i, plugin);
				} else {
					this.pluginFormatterNames.add(plugin);
				}
			}
			else if (PluginFactory.isSummaryPrinterName(plugin))
			{
				this.pluginSummaryPrinterNames.add(plugin);
			}
		}
		return this;
	}
	
	/**
	 * Disable the display plugin's.
	 */
	public void disableDisplay()
	{
		this.pluginDisplayNames.clear();
	}
	
	/**
	 * Sets the local copy of cucumber arguments.
	 * The pretty option is automatically removed.
	 * @param args The list of arguments to use.
	 * @return Self
	 */
	public PerfRuntimeOptions addCucumberOptions(List<String> args) {
		for (String arg : args) {
			List<String> ca = Shellwords.parse(arg);
			int p = 0;
			boolean ispretty = false;
			for (int i = 0; i < ca.size(); i++) {
				if (ca.get(i).contentEquals("-p")) {
					p = i;
				}
				if (ca.get(i).contains("pretty")) {
					ca.remove(i);
					i--;
					ispretty = true;
				}
				try {
					if (ispretty && (ca.get(i).contentEquals("-p") || !isCucumberPlugin(ca.get(i)))) {
						ispretty = false;
						ca.remove(p);
					}
				} catch (Exception e) {
				}

			}
			cucumberOptions.addAll(ca);
		}
		return this;
	}
	
	class ParsedPluginData {
        ParsedOptionNames formatterNames = new ParsedOptionNames();
        ParsedOptionNames displayNames = new ParsedOptionNames();
        ParsedOptionNames summaryPrinterNames = new ParsedOptionNames();

        public void addPluginName(String name, boolean isAddPlugin) {
        	
            if (PluginFactory.isFormatterName(name)) {
                formatterNames.addName(name, isAddPlugin);
            } else if (PluginFactory.isDisplayName(name)) {
                displayNames.addName(name, isAddPlugin);
            } else if (PluginFactory.isSummaryPrinterName(name)) {
                summaryPrinterNames.addName(name, isAddPlugin);
            } else {
                throw new CucumberException("Unrecognized plugin: " + name);
            }
        }

        public void updatePluginFormatterNames(List<String> pluginFormatterNames) {
            formatterNames.updateNameList(pluginFormatterNames);
        }

        public void updatePluginDisplayNames(List<String> pluginDisplayNames) {
            displayNames.updateNameList(pluginDisplayNames);
        }

        public void updatePluginSummaryPrinterNames(List<String> pluginSummaryPrinterNames) {
            summaryPrinterNames.updateNameList(pluginSummaryPrinterNames);
        }
    }

	class ParsedOptionNames {
		private List<String> names = new ArrayList<String>();
		private boolean clobber = false;

		public void addName(String name, boolean isAddOption) {
			names.add(name);
			if (!isAddOption) {
				clobber = true;
			}
		}

		public void updateNameList(List<String> nameList) {
			if (!names.isEmpty()) {
				if (clobber) {
					nameList.clear();
				}
				nameList.addAll(names);
			}
		}
	}

	public void setDryRun(boolean dryRun) {
		this.dryRun = dryRun;
	}

	public void setStrict(boolean strict) {
		this.strict = strict;
	}

	public void setMonochrome(boolean monochrome) {
		this.monochrome = monochrome;
	}

	public void setFailFast(boolean failFast) {
		this.failFast = failFast;
	}
}