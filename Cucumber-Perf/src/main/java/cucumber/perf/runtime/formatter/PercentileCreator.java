package cucumber.perf.runtime.formatter;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import cucumber.perf.api.formatter.StatisticCreator;
import cucumber.perf.api.result.statistics.Stat;
import cucumber.perf.api.result.statistics.Stats;

public class PercentileCreator implements StatisticCreator{

	private double percentile = 90.0d;
	private String postfix = "";
	
    public PercentileCreator( String[] options){
    	for (String opt : options)
	    {
    		try {
    			postfix = opt;
    			try {
    				Integer.parseInt(opt);
    				opt = opt+".0d";
    			} catch( Exception e) {}
				percentile = Double.parseDouble(opt);
			} catch( Exception e) {
				percentile = 90.0d;
				postfix= "90";
			}
	    }
    }
    
	@SuppressWarnings("unchecked")
	@Override
	public Object run(Object obj) {
		return run((HashMap<String, List<Long>>) obj);
	}

	@Override
	public Stats run(HashMap<String, List<Long>> sortedResults) {
		Stats stats = new Stats();
		Stat type = new Stat(Stats.StatType.PERCENTILE.type,""+postfix);
		stats.putStatisticType(type);
		for (Entry<String, List<Long>> entry : sortedResults.entrySet())
		{
			Double percent = percentile/100;
			Long prctlLoc = Math.round(entry.getValue().size()*percent);
			stats.putStatistic(type.getKey(),entry.getValue().get((prctlLoc.intValue()-1)).doubleValue(),entry.getKey());
		}
		return stats;
	}

}
