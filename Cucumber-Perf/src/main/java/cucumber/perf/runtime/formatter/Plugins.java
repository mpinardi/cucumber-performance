package cucumber.perf.runtime.formatter;

import cucumber.perf.api.event.ConcurrentEventListener;
import cucumber.perf.api.event.EventBus;
import cucumber.perf.api.event.EventListener;
import cucumber.perf.api.formatter.EventWriter;
import cucumber.perf.runtime.PerfRuntimeOptions;
import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.StrictAware;

/*import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;*/
import java.util.ArrayList;
import java.util.List;

public final class Plugins {
	private final List<Plugin> plugins;
	private final ClassLoader classLoader;
	private boolean pluginNamesInstantiated;

	private final PluginFactory pluginFactory;
	private final PerfRuntimeOptions pluginOptions;

	public Plugins(ClassLoader classLoader, PluginFactory pluginFactory, PerfRuntimeOptions pluginOptions) {
		this.classLoader = classLoader;
		this.pluginFactory = pluginFactory;
		this.pluginOptions = pluginOptions;
		this.plugins = createPlugins();
	}

    private List<Plugin> createPlugins() {
        List<Plugin> plugins = new ArrayList<Plugin>();
        if (!pluginNamesInstantiated) {
            for (String pluginName : pluginOptions.getPluginsNames()) {
            	if (!PluginFactory.isMinionName(pluginName)){
	                Plugin plugin = pluginFactory.create(pluginName);
	                addPlugin(plugins, plugin);
            	}
            }
            pluginNamesInstantiated = true;
        }
        return plugins;
    }

	public List<Plugin> getPlugins() {
		return plugins;
	}

	public void addPlugin(Plugin plugin) {
		addPlugin(plugins, plugin);
	}

	private void addPlugin(List<Plugin> plugins, Plugin plugin) {
		plugins.add(plugin);
		setMonochromeOnColorAwarePlugins(plugin);
		setStrictOnStrictAwarePlugins(plugin);
	}

	private void setMonochromeOnColorAwarePlugins(Plugin plugin) {
		if (plugin instanceof ColorAware) {
			ColorAware colorAware = (ColorAware) plugin;
			colorAware.setMonochrome(pluginOptions.isMonochrome());
		}
	}

	private void setStrictOnStrictAwarePlugins(Plugin plugin) {
		if (plugin instanceof StrictAware) {
			StrictAware strictAware = (StrictAware) plugin;
			strictAware.setStrict(pluginOptions.isStrict());
		}
	}

	public void setEventBusOnPlugins(EventBus eventBus) {
		for (Plugin plugin : plugins) {
			if (plugin instanceof ConcurrentEventListener) {
				((ConcurrentEventListener) plugin).setEventPublisher(eventBus);
			} else if (plugin instanceof EventListener) {
				((EventListener) plugin).setEventPublisher(eventBus);
			}
			if (plugin instanceof EventWriter) {
				((EventWriter) plugin).setEventBus(eventBus);
			}
		}
	}
}
