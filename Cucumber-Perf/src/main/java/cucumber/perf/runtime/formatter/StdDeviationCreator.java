package cucumber.perf.runtime.formatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import cucumber.perf.api.formatter.StatisticCreator;
import cucumber.perf.api.result.statistics.Stats;

public class StdDeviationCreator implements StatisticCreator{
	
    public StdDeviationCreator(){

    }
    
	@SuppressWarnings("unchecked")
	@Override
	public Object run(Object obj) {
		return run((HashMap<String, List<Long>>) obj);
	}

	@Override
	public Stats run(HashMap<String, List<Long>> sortedResults) {
		Stats stats = new Stats();
		stats.putStatisticType(Stats.StatType.STD_DEVIATION.type);
		for (Entry<String, List<Long>> entry : sortedResults.entrySet())
		{
			double m = mean(entry.getValue());
			List<Double> smsl = subtractMeanSquare(m,entry.getValue());
			double dm = meanD(smsl);
			stats.putStatistic(Stats.StatType.STD_DEVIATION.type.getKey(),Math.sqrt(dm),entry.getKey());
		}
		return stats;
	}
	
	private double sumD(List<Double> values) {
		Double sum = 0.0;
		for (Double l : values)
			sum=sum+l;
		return sum;
	}
	
	private long sum(List<Long> values) {
		long sum = 0;
		for (Long l : values)
			sum=sum+l;
		return sum;
	}
	
	private double mean(List<Long> values) {
		return sum(values)/values.size();
	}
	
	private double meanD(List<Double> values) {
		return sumD(values)/values.size();
	}
	
	private List<Double> subtractMeanSquare(double mean,List<Long> values) {
		List<Double> result = new ArrayList<Double>();
		for (Long l : values) {
		    double sub = (l - mean);
			result.add(sub*sub);
		}
		return result;	
	}
}
