package cucumber.perf.runtime.formatter;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cucumber.perf.api.event.ConcurrentEventListener;
import cucumber.perf.api.event.EventListener;
import cucumber.perf.api.formatter.DisplayPrinter;
import cucumber.perf.api.formatter.SummaryPrinter;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.plugin.Plugin;

/**
 * This class creates plugin instances from a String.
 * <p>
 * The String is of the form name[:output] where name is either a fully qualified class name or one of the built-in
 * short names. The output is optional for some plugins (and mandatory for some).
 * </p>
 *
 * @see Plugin for specific requirements
 */
public final class PluginFactory {
    @SuppressWarnings("rawtypes")
	private final Class[] CTOR_PARAMETERS = new Class[]{String.class, Appendable.class, AppendableBuilder.class, URI.class, URL.class, File.class};

    @SuppressWarnings("serial")
	private static final HashMap<String, Class<? extends Plugin>> PLUGIN_CLASSES = new HashMap<String, Class<? extends Plugin>>() {{
        put("junit", JUnitFormatter.class);
        put("chart_points", ChartPointsFormatter.class);
        put("summary_text", SummaryTextFormatter.class);
        put("logger", LoggerFormatter.class);
        put("statistics",StatisticsFormatter.class);
        put("default_summary", DefaultSummaryPrinter.class);
        put("null_summary", NullSummaryPrinter.class);
        put("null_display", NullDisplayPrinter.class);
        put("detail_display",DetailDisplayPrinter.class);
    }};
    //old pattern ([^:]+):(.*)
    private static final Pattern PLUGIN_WITH_ARG_PATTERN = Pattern.compile("([^:]+):(.*)");
    private static final Pattern PLUGIN_WITH_ARG_AND_OPTS_PATTERN = Pattern.compile("([^:]*)(?::)(.*)(?::(?![\\/]))([^:]*)");
    private String defaultOutFormatter = null;

    private Appendable defaultOut = new PrintStream(System.out) {
        @Override
        public void close() {
            // We have no intention to close System.out
        }
    };

    public Plugin create(String pluginString) {
        Matcher pluginWithArg = PLUGIN_WITH_ARG_PATTERN.matcher(pluginString);
        Matcher pluginWithArgOpts = PLUGIN_WITH_ARG_AND_OPTS_PATTERN.matcher(pluginString);
        String pluginName;
        String argument;
        String options;
        if (pluginWithArgOpts.matches()) {
            pluginName =  pluginWithArgOpts.group(1);
            argument =  pluginWithArgOpts.group(2);
            options = pluginWithArgOpts.group(3);
        } if (pluginWithArg.matches()) {
            pluginName =  pluginWithArg.group(1);
            argument =  pluginWithArg.group(2);
            options = null;
        } else {
            pluginName = pluginString;
            argument = null;
            options = null;
        }
        Class<? extends Plugin> pluginClass = pluginClass(pluginName);
        try {
            return instantiate(pluginString, pluginClass, argument,options);
        } catch (IOException e) {
            throw new CucumberException(e);
        } catch (URISyntaxException e) {
            throw new CucumberException(e);
        }
    }

    private <T extends Plugin> T instantiate(String pluginString, Class<T> pluginClass, String argument, String options) throws IOException, URISyntaxException {
        Constructor<T> twoarg = findDoubleArgConstructor(pluginClass);
        Constructor<T> singlearg = findSingleArgConstructor(pluginClass);
        Constructor<T> empty = findEmptyConstructor(pluginClass);
        String[] opts = options == null? new String[0]:(options.contains(",")?options.split(","):new String[] {options});
        if (twoarg != null) {
            Object ctorArg = convertOrNull(argument, twoarg.getParameterTypes()[0], pluginString);
            if (ctorArg != null)
                return newInstance(twoarg, ctorArg,opts);
        }
        if (singlearg != null) {
            Object ctorArg = convertOrNull(argument, singlearg.getParameterTypes()[0], pluginString);
            if (ctorArg != null)
                return newInstance(singlearg, ctorArg);
        }
        if (argument == null && empty != null) {
            return newInstance(empty);
        }
        if (twoarg != null)
            throw new CucumberException(String.format("You must supply an output argument to %s. Like so: %s:output", pluginString, pluginString));

        throw new CucumberException(String.format("%s must have a constructor that is either empty or a double arg of one of: %s, String[]", pluginClass, asList(CTOR_PARAMETERS)));
    }

    private <T extends Plugin> T newInstance(Constructor<T> constructor, Object... ctorArgs) {
        try {
            return constructor.newInstance(ctorArgs);
        } catch (InstantiationException e) {
            throw new CucumberException(e);
        } catch (IllegalAccessException e) {
            throw new CucumberException(e);
        } catch (InvocationTargetException e) {
            throw new CucumberException(e.getTargetException());
        }
    }

    private Object convertOrNull(String arg, Class<?> ctorArgClass, String formatterString) throws IOException, URISyntaxException {
        if (arg == null) {
            if (ctorArgClass.equals(Appendable.class)) {
                return defaultOutOrFailIfAlreadyUsed(formatterString);
            } else {
                return null;
            }
        }
        if (ctorArgClass.equals(URI.class)) {
            return new URI(arg);
        }
        if (ctorArgClass.equals(URL.class)) {
            return toURL(arg);
        }
        if (ctorArgClass.equals(File.class)) {
            return new File(arg);
        }
        if (ctorArgClass.equals(String.class)) {
            return arg;
        }
        if (ctorArgClass.equals(Appendable.class)) {
        	 return new UTF8OutputStreamWriter(new URLOutputStream(toURL(arg)));
        }
        if (ctorArgClass.equals(AppendableBuilder.class)) {
            return new AppendableBuilder(arg);
        }
        return null;
    }

    private <T> Constructor<T> findDoubleArgConstructor(Class<T> pluginClass) {
        Constructor<T> constructor = null;
        for (Class<?> ctorArgClass : CTOR_PARAMETERS) {
            try {
                Constructor<T> candidate = pluginClass.getConstructor(ctorArgClass,String[].class);
                if (constructor != null) {
                    throw new CucumberException(String.format("Plugin %s should only define a single two-argument constructor", pluginClass.getName()));
                }
                constructor = candidate;
            } catch (NoSuchMethodException ignore) {
            }
        }
        return constructor;
    }
    

    private <T> Constructor<T> findSingleArgConstructor(Class<T> pluginClass) {
        Constructor<T> constructor = null;
        for (Class<?> ctorArgClass : CTOR_PARAMETERS) {
            try {
                Constructor<T> candidate = pluginClass.getConstructor(ctorArgClass);
                if (constructor != null) {
                    throw new CucumberException(String.format("Plugin %s should only define a single one-argument constructor", pluginClass.getName()));
                }
                constructor = candidate;
            } catch (NoSuchMethodException ignore) {
            }
        }
        return constructor;
    }

    private <T> Constructor<T> findEmptyConstructor(Class<T> pluginClass) {
        try {
            return pluginClass.getConstructor();
        } catch (NoSuchMethodException ignore) {
            return null;
        }
    }

    private static Class<? extends Plugin> pluginClass(String pluginName) {
        Class<? extends Plugin> pluginClass = PLUGIN_CLASSES.get(pluginName);
        if (pluginClass == null) {
            pluginClass = loadClass(pluginName);
        }
        return pluginClass;
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Plugin> loadClass(String className) {
        try {
            Class<?> aClass = Thread.currentThread().getContextClassLoader().loadClass(className);

            if (Plugin.class.isAssignableFrom(aClass)) {
                return (Class<? extends Plugin>) aClass;
            }
            throw new CucumberException("Couldn't load plugin class: " + className + ". It does not implement " + Plugin.class.getName());
        } catch (ClassNotFoundException e) {
            throw new CucumberException("Couldn't load plugin class: " + className, e);
        }
    }

    private Appendable defaultOutOrFailIfAlreadyUsed(String formatterString) {
        try {
            if (defaultOut != null) {
                defaultOutFormatter = formatterString;
                return defaultOut;
            } else {
                throw new CucumberException("Only one formatter can use STDOUT, now both " +
                    defaultOutFormatter + " and " + formatterString + " use it. " +
                    "If you use more than one formatter you must specify output path with PLUGIN:PATH_OR_URL");
            }
        } finally {
            defaultOut = null;
        }
    }

    public static boolean isFormatterName(String name) {
        Class<?> pluginClass = getPluginClass(name);
        return EventListener.class.isAssignableFrom(pluginClass) || ConcurrentEventListener.class.isAssignableFrom(pluginClass);
    }

    public static boolean isSummaryPrinterName(String name) {
        Class<?> pluginClass = getPluginClass(name);
        return SummaryPrinter.class.isAssignableFrom(pluginClass);
    }
    
    public static boolean isDisplayName(String name) {
        Class<?> pluginClass = getPluginClass(name);
        return DisplayPrinter.class.isAssignableFrom(pluginClass);
    }

    private static Class<?> getPluginClass(String name) {
        Matcher pluginWithArg = PLUGIN_WITH_ARG_PATTERN.matcher(name);
        Matcher pluginWithArgOpts = PLUGIN_WITH_ARG_AND_OPTS_PATTERN.matcher(name);
        String pluginName;
        if (pluginWithArgOpts.matches()) {
            pluginName =  pluginWithArgOpts.group(1);
        } if (pluginWithArg.matches()) {
            pluginName =  pluginWithArg.group(1);
        } else {
            pluginName =name;
        }
        return pluginClass(pluginName);
    }
    
    private static URL toURL(String pathOrUrl) {
        try {
            if (!pathOrUrl.endsWith("/")) {
                pathOrUrl = pathOrUrl + "/";
            }
            if (pathOrUrl.matches("^(file|http|https):.*")) {
                return new URL(pathOrUrl);
            } else {
                return new URL("file:" + pathOrUrl);
            }
        } catch (MalformedURLException e) {
            throw new CucumberException("Bad URL:" + pathOrUrl, e);
        }
    }

    
}

