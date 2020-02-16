package cucumber.perf.api.result.statistics;

import java.time.Instant;
import java.util.LinkedHashMap;

public class Chart {
	private LinkedHashMap<Instant,Statistics> chart = new LinkedHashMap<Instant,Statistics>();

	public LinkedHashMap<Instant,Statistics> get() {
		return chart;
	}

	public void set(LinkedHashMap<Instant,Statistics> chart) {
		this.chart = chart;
	}

	public void putPoint(Instant point, Statistics value) {
		this.chart.put(point, value);
	}
	
	public Statistics getPoint(Instant point) {
		if (!this.chart.containsKey(point))
			this.chart.put(point, new Statistics());
		return this.chart.get(point);
	}
}
