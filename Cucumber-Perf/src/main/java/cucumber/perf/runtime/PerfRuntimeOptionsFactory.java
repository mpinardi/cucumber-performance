package cucumber.perf.runtime;

import cucumber.perf.api.CucumberPerfOptions;
//import io.cucumber.core.model.Classpath;
import io.cucumber.core.resource.ClasspathSupport;
import java.util.ArrayList;
import java.util.List;

public class PerfRuntimeOptionsFactory {
    private final Class<?> clazz;
    private boolean plansSpecified = false;
    
    public PerfRuntimeOptionsFactory(Class<?> clazz) {
        this.clazz = clazz;
    }
   
    public PerfRuntimeOptions create() {
        List<String> args = buildArgsFromOptions();
        return new PerfRuntimeOptions(args);
    }

    private List<String> buildArgsFromOptions() {
        List<String> args = new ArrayList<String>();
        for (Class<?> classWithOptions = clazz; hasSuperClass(classWithOptions); classWithOptions = classWithOptions.getSuperclass()) {
            CucumberPerfOptions options = getOptions(classWithOptions);
            if (options != null) {
                addDryRun(options, args);
                addTags(options, args);
                addName(options, args);
                addPlans(options, args);
                addPlugins(options, args);
            }
        }
        addDefaultPlanPathIfNoPlanPathIsSpecified(args, clazz);
        return args;
    }

    private void addName(CucumberPerfOptions options, List<String> args) {
        for (String name : options.name()) {
            args.add("name="+name);
        }
    }
    
    private void addDryRun(CucumberPerfOptions options, List<String> args) {
        if (options.dryRun()) {
            args.add("dryrun");
        }
    }
    
    private void addTags(CucumberPerfOptions options, List<String> args) {
        for (String tags : options.tags()) {
            args.add("tags="+tags);
        }
    }
    
    private void addPlans(CucumberPerfOptions options, List<String> args) {
        if (options != null && options.plans().length != 0) {
        	  for (String plans : options.plans()) {
        		  args.add("plans="+plans);
              }
            plansSpecified = true;
        }
    }
    
    private void addPlugins(CucumberPerfOptions options, List<String> args) {
        for (String plugin : options.plugin()) {
            args.add("plugin="+plugin);
        }
    }

    private void addDefaultPlanPathIfNoPlanPathIsSpecified(List<String> args, Class<?> clazz) {
        if (!plansSpecified) {
            args.add(ClasspathSupport.CLASSPATH_SCHEME + packagePath(clazz));
        }
    }

    static String packagePath(Class<?> clazz) {
        return packagePath(packageName(clazz.getName()));
    }

    static String packagePath(String packageName) {
        return packageName.replace('.', '/');
    }

    static String packageName(String className) {
        return className.substring(0, Math.max(0, className.lastIndexOf(".")));
    }

    private boolean hasSuperClass(Class<?> classWithOptions) {
        return classWithOptions != Object.class;
    }

    private CucumberPerfOptions getOptions(Class<?> clazz) {
        return clazz.getAnnotation(CucumberPerfOptions.class);
    }
}

