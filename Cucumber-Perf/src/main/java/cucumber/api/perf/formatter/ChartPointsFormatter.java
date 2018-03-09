package cucumber.api.perf.formatter;

import cucumber.api.perf.result.FeatureResult;
import cucumber.api.perf.result.ScenarioResult;
import cucumber.api.perf.result.StepResult;
import cucumber.runtime.CucumberException;
import cucumber.runtime.io.URLOutputStream;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChartPointsFormatter implements Formatter {
	private static final Pattern ARGUMENT_POSTFIX_PATTERN = Pattern.compile("([^|]+)\\|(.*)");
	private static final Pattern ARGUMENT_POSTFIX_SEPARATOR_PATTERN = Pattern.compile("-|\\[|\\]|\\(|\\)|\\{|\\}|_");
	private static final Pattern ARGUMENT_POSTFIX_PART_PATTERN = Pattern
			.compile("(?:(?!#).)+?(?=@)|(?:(?!@).)+?(?=#)|(?:(?!#)(?!@).)*$");
	private Writer out;
	private final String arg;
	private int count = 0;
	private LinkedHashMap<String,List<String>> lineGroups = new LinkedHashMap<String,List<String>>();
	
	public ChartPointsFormatter(String arg) throws IOException {
		this.arg = arg;
	}

	public void process(Statistics stats) {
		reset();
		for (Entry<String, List<HashMap<String, FeatureResult>>> e : stats.getChartPoints().entrySet())
		{
			for (HashMap<String, FeatureResult> l : e.getValue())
			{
				addLines(l);
			}
		}
		this.finishReport();
	}
	
	public void reset()
	{
		lineGroups= new LinkedHashMap<String,List<String>>();
	}

	public void addLines(HashMap<String, FeatureResult> features) {
		//line: feature,scenario,step,type,datetime,value
		for (Entry<String,FeatureResult> e : features.entrySet())
		{
			String feature = e.getValue().getName();
			String scenario = "";
			String step = "";
			String type = e.getKey();
			String datetime = e.getValue().getStop().toString();
			long value = e.getValue().getResultDuration();
			if (lineGroups.containsKey(feature+":"+type))
			{
				lineGroups.get(feature+":"+type).add(feature+","+scenario+","+step+","+type+","+datetime+","+value);
			}
			else
			{
				List<String> arr = new ArrayList<String>();
				arr.add(feature+","+scenario+","+step+","+type+","+datetime+","+value);
				lineGroups.put(feature+":"+type,arr);
			}
			
			for (ScenarioResult sr : e.getValue().getChildResults()) {
				scenario = sr.getName();
				datetime = sr.getStop().toString();
				value = sr.getResultDuration();
				if (lineGroups.containsKey(feature+"."+scenario+":"+type))
				{
					lineGroups.get(feature+"."+scenario+":"+type).add(feature+","+scenario+","+step+","+type+","+datetime+","+value);
				}
				else
				{
					List<String> arr = new ArrayList<String>();
					arr.add(feature+","+scenario+","+step+","+type+","+datetime+","+value);
					lineGroups.put(feature+"."+scenario+":"+type,arr);
				}
				for (StepResult stpr : sr.getChildResults()) {
					step = stpr.getName();
					datetime = sr.getStop().toString();
					value = sr.getResultDuration();
					if (lineGroups.containsKey(feature+"."+scenario+"."+step+":"+type))
					{
						lineGroups.get(feature+"."+scenario+"."+step+":"+type).add(feature+","+scenario+","+step+","+type+","+datetime+","+value);
					}
					else
					{
						List<String> arr = new ArrayList<String>();
						arr.add(feature+","+scenario+","+step+","+type+","+datetime+","+value);
						lineGroups.put(feature+"."+scenario+"."+step+":"+type,arr);
					}
				}
			}
		}
	}

	public void finishReport() {
			try {
				this.out = new OutputStreamWriter(new URLOutputStream(parseUrl(arg)));
			} catch (MalformedURLException e) {
				throw new CucumberException(e);
			} catch (IOException e) {
				throw new CucumberException(e);
			}
			
			try (BufferedWriter bw = new BufferedWriter(out)) {
				for (Entry<String,List<String>> group : lineGroups.entrySet())
				{
					for (String line : group.getValue())
					{
						bw.append(line+"\n");
					}
				}

			} catch (IOException e) {

			}
			closeQuietly(out);
	}

	private URL parseUrl(String url) throws MalformedURLException {
		Matcher argumentWithPostfix = ARGUMENT_POSTFIX_PATTERN.matcher(url);
		String path;
		String argument;

		if (argumentWithPostfix.matches()) {
			path = argumentWithPostfix.group(1);
			argument = argumentWithPostfix.group(2);
		} else {
			path = url;
			argument = "";
		}
		return new URL(path + parsePostFix(argument));
	}

	private String parsePostFix(String argument) {
		String[] a = argument.split("\\.");
		List<String> args = new ArrayList<String>();
		// if there was an extension
		if (a.length > 1) {
			Matcher ms = ARGUMENT_POSTFIX_SEPARATOR_PATTERN.matcher(a[0]);
			// if separator
			int last = 0;
			while (ms.find()) {
				if (ms.start() > 0) {
					args.add(a[0].substring(last, ms.start()));
					last = ms.start() + 1;
				}
			}
			if (last > 0 && last != a[0].length()) {
				args.add(a[0].substring(last, a[0].length()));
			}

			if (args.size() == 0) {
				// no separator
				args.add(a[0]);
			}
		}

		for (String larg : args) {
			Matcher m = ARGUMENT_POSTFIX_PART_PATTERN.matcher(larg);
			while (m.find()) {
				String value = larg.substring(m.start(), m.end());

				try {
					Integer.parseInt(value);
					count = count+1;
					argument = argument.replace("#" + value,
							String.format("%0" + (value.length())+ "d", count));
				} catch (NumberFormatException n) {
					if (!value.isEmpty()) {
						argument = argument.replace("@" + value, parsePostFixDate(value));
					}
				}
			}
		}
		return argument;
	}

	private String parsePostFixDate(String format) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
		LocalDateTime dateTime = LocalDateTime.now();
		return dateTime.format(formatter);
	}
	
	private static void closeQuietly(Closeable out) {
		try {
			out.close();
		} catch (IOException ignored) {
			// go gentle into that good night
		}
	}
}
