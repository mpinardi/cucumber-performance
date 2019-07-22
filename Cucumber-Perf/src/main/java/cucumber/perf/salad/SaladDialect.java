package cucumber.perf.salad;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SaladDialect {
    private final Map<String, List<String>> keywords;
    private String language;

    public SaladDialect(String language, Map<String, List<String>> keywords) {
        this.language = language;
        this.keywords = keywords;
    }

    public List<String> getPlanKeywords() {
        return keywords.get("plan");
    }

    public List<String> getSimulationKeywords() {
        return keywords.get("simulation");
    }
    
    public List<String> getSimulationPeriodKeywords() {
        return keywords.get("simulationPeriod");
    }
    
    public List<String> getTimeKeywords() {
        return keywords.get("time");
    }

    public List<String> getGroupKeywords() {
        List<String> result = new ArrayList<>();
        result.addAll(getTestKeywords());
        return result;
    }

    public List<String> getTestKeywords() {
        return keywords.get("group");
    }
    
    public List<String> getRunnersKeywords() {
        return keywords.get("runners");
    }
    
    public List<String> getCountKeywords() {
        return keywords.get("count");
    }
    
    public List<String> getRampUpKeywords() {
        return keywords.get("rampup");
    }
    
    public List<String> getRampDownKeywords() {
        return keywords.get("rampdown");
    }
    
    public List<String> getSynchronizedKeywords() {
        return keywords.get("synchronized");
    }
    
    public List<String> getRandomWaitKeywords() {
        return keywords.get("randomwait");
    }
    public String getLanguage() {
        return language;
    }
}
