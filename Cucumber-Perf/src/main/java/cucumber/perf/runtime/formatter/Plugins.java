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
				Plugin plugin = pluginFactory.create(pluginName);
				addPlugin(plugins, plugin);
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
	/*
	*//**
		 * Creates a dynamic proxy that multiplexes method invocations to all plugins of
		 * the same type.
		 *
		 * @param type proxy type
		 * @param <T>  generic proxy type
		 * @return a proxy
		 *//*
			 * @SuppressWarnings("unused") private <T> T pluginProxy(final Class<T> type) {
			 * Object proxy = Proxy.newProxyInstance(classLoader, new Class<?>[]{type}, new
			 * InvocationHandler() {
			 * 
			 * @Override public Object invoke(Object target, Method method, Object[] args)
			 * throws Throwable { for (Object plugin : getPlugins()) { if
			 * (type.isInstance(plugin)) { try { invoke(plugin, method, (long)0, args); }
			 * catch (Throwable t) { if
			 * (!method.getName().equals("startOfScenarioLifeCycle") &&
			 * !method.getName().equals("endOfScenarioLifeCycle")) { // IntelliJ has its own
			 * formatter which doesn't yet implement these methods. throw t; } } } } return
			 * null; } }); return type.cast(proxy); }
			 * 
			 * public static Object invoke(final Object target, final Method method, long
			 * timeoutMillis, final Object... args) throws Throwable { final Method
			 * targetMethod = targetMethod(target, method); return Timeout.timeout(new
			 * Timeout.Callback<Object>() {
			 * 
			 * @Override public Object call() throws Throwable { boolean accessible =
			 * targetMethod.isAccessible(); try { targetMethod.setAccessible(true); return
			 * targetMethod.invoke(target, args); } catch (IllegalArgumentException e) {
			 * throw new CucumberException("Failed to invoke " +
			 * MethodFormat.FULL.format(targetMethod) + ", caused by " +
			 * e.getClass().getName() + ": " + e.getMessage(), e); } catch
			 * (InvocationTargetException e) { throw e.getTargetException(); } catch
			 * (IllegalAccessException e) { throw new CucumberException("Failed to invoke "
			 * + MethodFormat.FULL.format(targetMethod) + ", caused by " +
			 * e.getClass().getName() + ": " + e.getMessage(), e); } finally {
			 * targetMethod.setAccessible(accessible); } } }, timeoutMillis); }
			 * 
			 * private static Method targetMethod(final Object target, final Method method)
			 * throws NoSuchMethodException { final Class<?> targetClass =
			 * target.getClass(); final Class<?> declaringClass =
			 * method.getDeclaringClass();
			 * 
			 * // Immediately return the provided method if the class loaders are the same.
			 * if (targetClass.getClassLoader().equals(declaringClass.getClassLoader())) {
			 * return method; } else { // Check if the method is publicly accessible. Note
			 * that methods from interfaces are always public. if
			 * (Modifier.isPublic(method.getModifiers())) { return
			 * targetClass.getMethod(method.getName(), method.getParameterTypes()); }
			 * 
			 * // Loop through all the super classes until the declared method is found.
			 * Class<?> currentClass = targetClass; while (currentClass != Object.class) {
			 * try { return currentClass.getDeclaredMethod(method.getName(),
			 * method.getParameterTypes()); } catch (NoSuchMethodException e) { currentClass
			 * = currentClass.getSuperclass(); } }
			 * 
			 * // The method does not exist in the class hierarchy. throw new
			 * NoSuchMethodException(String.valueOf(method)); } }
			 */

}
