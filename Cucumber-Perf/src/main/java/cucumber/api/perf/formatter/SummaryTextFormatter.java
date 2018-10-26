package cucumber.api.perf.formatter;

import cucumber.api.perf.result.FeatureResult;
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
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SummaryTextFormatter implements Formatter {
	private static final Pattern ARGUMENT_POSTFIX_PATTERN = Pattern.compile("([^|]+)\\|(.*)");
	private static final Pattern ARGUMENT_POSTFIX_SEPARATOR_PATTERN = Pattern.compile("-|\\[|\\]|\\(|\\)|\\{|\\}|_");
	private static final Pattern ARGUMENT_POSTFIX_PART_PATTERN = Pattern
			.compile("(?:(?!#).)+?(?=@)|(?:(?!@).)+?(?=#)|(?:(?!#)(?!@).)*$");
	private Writer out;
	private final String arg;
	private int count = 0;
	private List<String> lines = new ArrayList<String>();
	
	public SummaryTextFormatter(String arg) throws IOException {
		this.arg = arg;
	}

	public void process(Statistics stats) {
		reset();
		createLines(stats);
		this.finishReport();
	}
	
	public void reset()
	{
		lines = new ArrayList<String>();
	}

	public void createLines(Statistics s){
		
		lines.add("Averages:");

		for (Entry<String, FeatureResult> entry : s.getAvg().entrySet()) {
			lines.add("Feature: " + entry.getKey() + " Count: " + s.getCnt().get(entry.getKey()).getResultDuration()
					+ " Avg: " + entry.getValue().getResultDuration() / 1000000
					+ " Min: " + s.getMin().get(entry.getKey()).getResultDuration() / 1000000 + " Max: "
					+ s.getMax().get(entry.getKey()).getResultDuration() / 1000000);
			for (int sc = 0; sc < entry.getValue().getChildResults().size(); sc++) {
				lines.add("	 Scenario: " + entry.getValue().getChildResults().get(sc).getName() + " Count: "
						+ s.getCnt().get(entry.getKey()).getChildResults().get(sc).getResultDuration() + " Avg: "
						+ entry.getValue().getChildResults().get(sc).getResultDuration() / 1000000 + " Min: "
						+ s.getMin().get(entry.getKey()).getChildResults().get(sc).getResultDuration() / 1000000
						+ " Max: "
						+ s.getMax().get(entry.getKey()).getChildResults().get(sc).getResultDuration() / 1000000);// 1000000
			
				for (int stp = 0; stp < entry.getValue().getChildResults().get(sc).getChildResults().size(); stp++) {
					if (entry.getValue().getChildResults().get(sc).getChildResults().get(stp).getResultDuration() != null)
					{
						lines.add("		Step: "
							+ entry.getValue().getChildResults().get(sc).getChildResults().get(stp).getName() + " Count: "
							+ s.getCnt().get(entry.getKey()).getChildResults().get(sc).getChildResults().get(stp)
									.getResultDuration()+ " Avg: "
							+ entry.getValue().getChildResults().get(sc).getChildResults().get(stp).getResultDuration()
									/ 1000000
							+ " Min: "
							+ s.getMin().get(entry.getKey()).getChildResults().get(sc).getChildResults().get(stp)
									.getResultDuration() / 1000000
							+ " Max: " + s.getMax().get(entry.getKey()).getChildResults().get(sc).getChildResults()
									.get(stp).getResultDuration() / 1000000);
					}
				}
			}
		}
		
		HashMap<String,HashMap<String,Throwable>> errors = s.getErrors();
		if (!errors.isEmpty())
		{
			lines.add("Errors:");
			for (Entry<String,HashMap<String,Throwable>> entry : errors.entrySet()) {
				lines.add("  Feature: " + entry.getKey());
				for (Entry<String,Throwable> sentry: entry.getValue().entrySet()) {
					lines.add("		Step: " + entry.getKey());
					StackTraceElement[] stes = sentry.getValue().getStackTrace();
					lines.add(sentry.getValue().getMessage());
					for (StackTraceElement ste : stes)
					{
						lines.add("\t"+ste.getClassName()+"."+ste.getMethodName()+"("+ste.getFileName()+":"+ste.getLineNumber()+")");
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
				for (String line : lines)
				{
					bw.append(line+"\r\n");
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
