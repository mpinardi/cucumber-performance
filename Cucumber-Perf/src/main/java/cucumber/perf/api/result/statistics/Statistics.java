package cucumber.perf.api.result.statistics;

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;
import cucumber.perf.api.result.BaseResult;
import cucumber.perf.api.result.GroupResult;

public class Statistics {
	private BaseResult simulation = null;
	private Stats stats = new Stats();
	private HashMap<String,GroupResult> groups = new HashMap<String,GroupResult>();
	private HashMap<String,HashMap<String,StepErrors>> errors = new HashMap<String,HashMap<String,StepErrors>>();
    
	public Statistics() {
		
	}
	
	public Statistics(BaseResult simulation,Stats stats,HashMap<String,GroupResult> groups,HashMap<String,HashMap<String,StepErrors>> errors) {
		this.simulation = simulation;
		this.stats = stats;
		this.groups = groups;
		this.errors = errors;
	}
	
	public HashMap<String,GroupResult> getGroups()
	{
		return groups;
	}
	
	public void setGroups(HashMap<String,GroupResult> groups)
	{
		this.groups = groups;
	}
	
	public void putGroup(String key,GroupResult value)
	{
		this.groups.put(key, value);
	}

	public void putErrors(String group, HashMap<String, StepErrors> errors) {
		this.errors.put(group,errors);
	}
	
	public void setErrors(HashMap<String, HashMap<String, StepErrors>> errors) {
		this.errors = errors;
	}
	
	public HashMap<String, HashMap<String, StepErrors>> getErrors() {
		return errors;
	}
	
	public void setSimulation(BaseResult result) {
		this.simulation = result;
	}

	public BaseResult getSimulation() {
		return simulation;
	}
	
	public Double getAvg(String group, String scenario, String step)
	{
		return stats.getStatistic(Stats.StatType.AVERAGE.key,group,scenario,step);
	}
	
	public Double getAvg(String group, String scenario)
	{
		return stats.getStatistic(Stats.StatType.AVERAGE.key,group,scenario);
	}
	
	public Double getAvg(String group)
	{
		return stats.getStatistic(Stats.StatType.AVERAGE.key,group);
	}
	
	public Double getMin(String group, String scenario, String step)
	{
		return  stats.getStatistic(Stats.StatType.MINIMUM.key,group,scenario,step);
	}
	
	public Double getMin(String group, String scenario)
	{
		return stats.getStatistic(Stats.StatType.MINIMUM.key,group,scenario);
	}
	
	public Double getMin(String group)
	{
		return stats.getStatistic(Stats.StatType.MINIMUM.key,group);
	}
	
	public Double getMax(String group, String scenario, String step)
	{
		return stats.getStatistic(Stats.StatType.MAXIMUM.key,group,scenario,step);
	}
	
	public Double getMax(String group, String scenario)
	{
		return stats.getStatistic(Stats.StatType.MAXIMUM.key,group,scenario);
	}
	
	public Double getMax(String group)
	{
		return stats.getStatistic(Stats.StatType.MAXIMUM.key,group);
	}
	
	public Double getCnt(String group, String scenario, String step)
	{
		return stats.getStatistic(Stats.StatType.COUNT.key,group,scenario,step);
	}
	
	public Double getCnt(String group, String scenario)
	{
		return stats.getStatistic(Stats.StatType.COUNT.key,group,scenario);
	}
	
	public Double getCnt(String group)
	{
		return stats.getStatistic(Stats.StatType.COUNT.key,group);
	}
	
	public Set<Entry<String,Double>> getStatistics(String group, String scenario, String step)
	{
		return stats.getStatistics(group,scenario,step).entrySet();
	}
	
	public Set<Entry<String,Double>> getStatistics(String group, String scenario)
	{
		return stats.getStatistics(group,scenario).entrySet();
	}
	
	public Set<Entry<String,Double>> getStatistics(String group)
	{
		return stats.getStatistics(group).entrySet();
	}
	
	public Set<String> getStatTypes() {
		return stats.getStatisticTypes();
	}
	
	public Stat getStatType(String key) {
		return stats.getStatisticType(key);
	}
	
	public Stats getStats() {
		return stats;
	}

	public void setStats(Stats stats) {
		this.stats = stats;
	}
	
	public void addStats(Stats stats) {
		this.stats.addStatistics(stats);
	}
}
