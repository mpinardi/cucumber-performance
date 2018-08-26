package cucumber.api.perf;

import cucumber.api.Plugin;
import cucumber.api.perf.formatter.DisplayPrinter;
import cucumber.api.perf.formatter.Formatter;
import cucumber.api.perf.formatter.SummaryPrinter;
import cucumber.api.perf.salad.ISaladDialectProvider;
import cucumber.api.perf.salad.SaladDialect;
import cucumber.api.perf.salad.SaladDialectProvider;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Shellwords;
import cucumber.runtime.Utils;
//import cucumber.runtime.table.TablePrinter;
import cucumber.util.FixJava;
import cucumber.util.Mapper;
import io.cucumber.datatable.DataTable;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import static cucumber.util.FixJava.join;
import static cucumber.util.FixJava.map;
import static java.util.Arrays.asList;

// IMPORTANT! Make sure USAGE.txt is always up to date if this class changes.

public class PerfRuntimeOptions {
	public static final String VERSION = ResourceBundle.getBundle("cucumber.version").getString("cucumber-jvm.version");
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
    private final PluginFactory pluginFactory;
    private final List<Plugin> plugins = new ArrayList<Plugin>();
	private boolean dryRun;
	private boolean pluginNamesInstantiated;

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
		this(new PluginFactory(),argv);
	}
	
	/**
	 * Create a new instance from a list of options, for example:
	 * <pre>{@code Arrays.asList("name=the fox", "plugin=detail_display);}</pre>
	 * @param pluginFactory A existing plugin factory
	 * @param argv
	 *            the arguments
	 */
	public PerfRuntimeOptions(PluginFactory pluginFactory, List<String> argv) {
		this.pluginFactory = pluginFactory;
		argv = new ArrayList<String>(argv); // in case the one passed in is unmodifiable.
		List<String> ls = parse(argv);
		addCucumberOptions(ls);
		
		if (pluginFormatterNames.isEmpty()) {
			//possible future default
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
			} else if (arg.equals("help") || arg.equals("h")) {
				printUsage();
				System.exit(0);
			} else if (arg.startsWith("i18n=")) {
				System.exit(printI18n(arg.split("=")[1]));
			} else if (arg.equals("version") || arg.equals("v")) {
				System.out.println(VERSION);
				System.exit(0);
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
	
	public List<Plugin> getPlugins() {
        if (!pluginNamesInstantiated) {
            for (String pluginName : pluginFormatterNames) {
                Plugin plugin = pluginFactory.create(pluginName);
                plugins.add(plugin);
            }
            for (String pluginName : pluginSummaryPrinterNames) {
                Plugin plugin = pluginFactory.create(pluginName);
                plugins.add(plugin);
            }
            for (String pluginName : pluginDisplayNames) {
                Plugin plugin = pluginFactory.create(pluginName);
                plugins.add(plugin);
            }
            pluginNamesInstantiated = true;
        }
        return plugins;
    }

    public List<Formatter> formatters(ClassLoader classLoader) {
        return pluginProxies(classLoader, Formatter.class);
    }

    public SummaryPrinter summaryPrinter(ClassLoader classLoader) {
        return pluginProxy(classLoader, SummaryPrinter.class);
    }
    
    public DisplayPrinter displayPrinter(ClassLoader classLoader) {
        return pluginProxy(classLoader, DisplayPrinter.class);
    }
    
    /**
     * Creates a dynamic proxy that multiplexes method invocations to all plugins of the same type.
     *
     * @param classLoader used to create the proxy
     * @param type        proxy type
     * @param <T>         generic proxy type
     * @return a list of proxies
     */
    @SuppressWarnings("unchecked")
	private <T> List<T> pluginProxies(ClassLoader classLoader, final Class<T> type) {
    	List<Plugin> plugins = getPlugins();
    	List<Object> proxies = new ArrayList<Object>();
    	
        for (Plugin plugin : plugins) {
            if (type.isInstance(plugin)) {
	        Object proxy = Proxy.newProxyInstance(classLoader, new Class<?>[]{type}, new InvocationHandler() {
	            @Override
	            public Object invoke(Object target, Method method, Object[] args) throws Throwable {
				try {
	                Utils.invoke(plugin, method, 0, args);
	            } catch (Throwable t) {
	                if (!method.getName().equals("startOfScenarioLifeCycle") && !method.getName().equals("endOfScenarioLifeCycle")) {
	                    // IntelliJ has its own formatter which doesn't yet implement these methods.
	                    throw t;
	                }
	            }
	            return null;
	            }
	        });
	        proxies.add(type.cast(proxy));
	       }
       }
        return (List<T>) proxies;
    }
    
    /**
     * Creates a dynamic proxy that multiplexes method invocations to all plugins of the same type.
     *
     * @param classLoader used to create the proxy
     * @param type        proxy type
     * @param <T>         generic proxy type
     * @return a proxy
     */
    private <T> T pluginProxy(ClassLoader classLoader, final Class<T> type) {
    	List<Plugin> plugins = getPlugins();
        Object proxy = Proxy.newProxyInstance(classLoader, new Class<?>[]{type}, new InvocationHandler() {
            @Override
            public Object invoke(Object target, Method method, Object[] args) throws Throwable {
                for (Plugin plugin : plugins) {
                    if (type.isInstance(plugin)) {
                        try {
                            Utils.invoke(plugin, method, 0, args);
                        } catch (Throwable t) {
                            if (!method.getName().equals("startOfScenarioLifeCycle") && !method.getName().equals("endOfScenarioLifeCycle")) {
                                // IntelliJ has its own formatter which doesn't yet implement these methods.
                                throw t;
                            }
                        }
                    }
                }
                return null;
            }
        });
        
        return type.cast(proxy);
    }
    
	private boolean isCucumberPlugin(String string) throws Exception {
		if (cucumber.runtime.formatter.PluginFactory.isSummaryPrinterName(string)) {
			return true;
		}
		if (cucumber.runtime.formatter.PluginFactory.isFormatterName(string)) {
			return true;
		}
		if (cucumber.runtime.formatter.PluginFactory.isStepDefinitionReporterName(string)) {
			return true;
		}
		return false;
	}


	public boolean isDryRun() {
		return dryRun;
	}

	public List<String> getPlanPaths() {
		return planPaths;
	}

	public List<Pattern> getNameFilters() {
		return nameFilters;
	}

	public List<String> getTagFilters() {
		return tagFilters;
	}
	
	public List<String> getCucumberOptions() {
		return cucumberOptions;
	}

	public PerfRuntimeOptions addPlanPaths(List<String> planPaths) {
		this.planPaths.addAll(planPaths);
		return this;
	}

	public PerfRuntimeOptions addNameFilters(List<String> nameFilters) {
		for (String nf : nameFilters) {
			Pattern patternFilter = Pattern.compile(nf);
			this.nameFilters.add(patternFilter);
		}
		return this;
	}

	public PerfRuntimeOptions addTagFilters(List<String> tagFilters) {
		this.tagFilters.addAll(tagFilters);
		return this;
	}
	
	public PerfRuntimeOptions addPlugins(List<String> plugins) {
		for (String plugin : plugins)
		{
			if (PluginFactory.isDisplayName(plugin))
			{
				this.pluginDisplayNames.add(plugin);
			}
			else if (PluginFactory.isFormatterName(plugin))
			{
				this.pluginFormatterNames.add(plugin);
			}
			else if (PluginFactory.isSummaryPrinterName(plugin))
			{
				this.pluginSummaryPrinterNames.add(plugin);
			}
		}
		return this;
	}

	public PerfRuntimeOptions setDryRun(boolean value) {
		dryRun = value;
		return this;
	}
	
	public void disableDisplay()
	{
		this.getPlugins();
		for (int i = 0; i < plugins.size(); i++)
		{
			if (plugins.get(i) instanceof DisplayPrinter)
			{
				this.plugins.remove(i);
			}
		}
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
}