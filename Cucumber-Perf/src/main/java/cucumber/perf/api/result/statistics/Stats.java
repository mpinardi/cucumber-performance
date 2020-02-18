package cucumber.perf.api.result.statistics;

import static java.util.Locale.ROOT;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

public class Stats {
	private LinkedHashMap<String,Stat> statTypes = new LinkedHashMap<String,Stat>();
	private HashMap<String,LinkedHashMap<String,Double>> stats = new HashMap<String,LinkedHashMap<String,Double>>();
	public static String SEP = ".";
	
	public enum StatType {
		AVERAGE("avg", new Stat("avg","Average","Avg","avg",Stat.StatDataType.NANOS)), 
		MINIMUM("min", new Stat("min","Minimum","Min","min",Stat.StatDataType.NANOS)),
		MAXIMUM("max", new Stat("max","Maximum","Max","max",Stat.StatDataType.NANOS)), 
		MEDIAN("med", new Stat("med","Median","Med","med",Stat.StatDataType.NANOS)),
		COUNT("cnt", new Stat("cnt","Count","Count","cnt",Stat.StatDataType.COUNT)),
		PASSED("pass", new Stat("pass","Passed","Pass","pass",Stat.StatDataType.COUNT)),
		FAILED("fail", new Stat("fail","Failed","Fail","fail",Stat.StatDataType.COUNT)),
		ERRORED("error", new Stat("error","Errored","Error","error",Stat.StatDataType.COUNT)), 
		PERCENTILE("prctl", new Stat("prctl","Percentile","Prctl","prctl",Stat.StatDataType.NANOS)),
		PERCENTAGE("%", new Stat("%","Percentage","Percent","%",Stat.StatDataType.OTHER)),
		STD_DEVIATION("stdev", new Stat("stdev","Standard Deviation","StdDev","stdev",Stat.StatDataType.OTHER)),
		CONCURRENCY("cncrnt", new Stat("cncrnt","Concurrency","Concurrency","cncrnt",Stat.StatDataType.OTHER));
		public String key;
		public Stat type;
		
		StatType(String key, Stat type)
		{
			this.key = key;
			this.type = type;
		}

        public static StatType fromLowerCaseName(String lowerCaseName) {
            return valueOf(lowerCaseName.toUpperCase(ROOT));
        }

        public String lowerCaseName() {
            return name().toLowerCase(ROOT);
        }

        public String firstLetterCapitalizedName() {
            return name().substring(0, 1) + name().substring(1).toLowerCase(ROOT);
        }
    }
	
	public void putStatisticType(Stat stat)
	{
		statTypes.put(stat.getKey(), stat);
	}
	
	public Stat getStatisticType(String key)
	{
		return statTypes.get(key);
	}
	
	public Set<String> getStatisticTypes()
	{
		if (!statTypes.isEmpty())
			return statTypes.keySet();
		return null;
	}
	
	public Set<String> getStatisticKeys()
	{
		if (!stats.isEmpty())
			return stats.keySet();
		return null;
	}
	
	public LinkedHashMap<String,Double> getStatistics(String group, String scenario, String step)
	{
		return stats.get(group+SEP+scenario+SEP+step);
	}
	
	public LinkedHashMap<String,Double> getStatistics(String group, String scenario)
	{
		return stats.get(group+SEP+scenario);
	}
	
	public LinkedHashMap<String,Double> getStatistics(String groupOrKey)
	{
		return stats.get(groupOrKey);
	}
	
	public Double getStatistic(String name,String group, String scenario, String step)
	{
		LinkedHashMap<String,Double> s = stats.get(group+SEP+scenario+SEP+step);
		return s!=null?s.get(name):null;
	}
	
	public Double getStatistic(String name,String group, String scenario)
	{
		LinkedHashMap<String,Double> s = stats.get(group+SEP+scenario);
		return s!=null?s.get(name):null;
	}
	
	public Double getStatistic(String name,String groupOrKey)
	{
		LinkedHashMap<String,Double> s = stats.get(groupOrKey);
		return s!=null?s.get(name):null;
	}
	
	public void putStatistic(String name, Double value, String group, String scenario, String step)
	{
		if (this.stats.containsKey(group+SEP+scenario+SEP+step)) {
			this.stats.get(group+SEP+scenario+SEP+step).put(name, value);
		} else {
			LinkedHashMap<String, Double> hm = this.getNewMap();
			hm.put(name, value);
			this.stats.put(group+SEP+scenario+SEP+step,hm);
		}
	}
	
	public void putStatistic(String name,Double value, String group, String scenario)
	{
		if (this.stats.containsKey(group+SEP+scenario)) {
			this.stats.get(group+SEP+scenario).put(name, value);
		} else {
			LinkedHashMap<String, Double> hm = this.getNewMap();
			hm.put(name, value);
			this.stats.put(group+SEP+scenario,hm);
		}
	}
	
	public void putStatistic(String name,Double value,String groupOrKey)
	{
		if (this.stats.containsKey(groupOrKey)) {
			this.stats.get(groupOrKey).put(name, value);
		} else {
			LinkedHashMap<String, Double> hm = this.getNewMap();
			hm.put(name, value);
			this.stats.put(groupOrKey,hm);
		}
	}
	
	public void putKey(String group, String scenario, String step)
	{
		if (!this.stats.containsKey(group+SEP+scenario+SEP+step)) {
			this.stats.put(group+SEP+scenario+SEP+step,this.getNewMap());
		}
	}
	
	public void putKey(String group, String scenario)
	{
		if (!this.stats.containsKey(group+SEP+scenario)) {
			this.stats.put(group+SEP+scenario,this.getNewMap());
		}
	}
	
	public void putKey(String groupOrKey)
	{
		if (!this.stats.containsKey(groupOrKey)) {
			this.stats.put(groupOrKey,this.getNewMap());
		}
	}
	
	public void addStatistics(Stats stats)
	{
		for (String pathKey : stats.getStatisticKeys())
		{
			for (String type : stats.getStatisticTypes()){
				if (!this.statTypes.containsKey(type)) {
					this.statTypes.put(type,stats.statTypes.get(type));
				}
			}
			if (this.stats.containsKey(pathKey)) {
				for (Entry<String, Double> stat : stats.getStatistics(pathKey).entrySet()){
					this.stats.get(pathKey).putIfAbsent(stat.getKey(),stat.getValue());
				}
			} else {
				LinkedHashMap<String, Double> hm = new LinkedHashMap<String,Double>();
				this.stats.put(pathKey,hm);
				for (String st : this.getStatisticTypes()){
					this.stats.get(pathKey).put(this.getStatisticType(st).getKey(), 0.0);
				}
				for (Entry<String, Double> stat : stats.getStatistics(pathKey).entrySet()){
					this.stats.get(pathKey).put(stat.getKey(),stat.getValue());
				}
			}
		}
	}
	
	private LinkedHashMap<String,Double> getNewMap()
	{
		LinkedHashMap<String,Double> lm = new LinkedHashMap<String,Double>();
		Set<String> types = getStatisticTypes();
		if (types!=null)
		{
			for (String type : types)
			{
				lm.put(type,0.0);
			}
		}
		return lm;
	}

	public boolean isEmpty() {
		return this.stats.isEmpty();
	}
}
