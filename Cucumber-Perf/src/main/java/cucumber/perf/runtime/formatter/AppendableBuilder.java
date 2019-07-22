package cucumber.perf.runtime.formatter;

import static cucumber.runtime.Utils.toURL;

import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cucumber.runtime.CucumberException;

public class AppendableBuilder {
	private static final Pattern ARGUMENT_POSTFIX_PATTERN = Pattern.compile("([^|]+)\\|(.*)");
	private static final Pattern ARGUMENT_POSTFIX_SEPARATOR_PATTERN = Pattern.compile("-|\\[|\\]|\\(|\\)|\\{|\\}|_");
	private static final Pattern ARGUMENT_POSTFIX_PART_PATTERN = Pattern
			.compile("(?:(?!#).)+?(?=@)|(?:(?!@).)+?(?=#)|(?:(?!#)(?!@).)*$");
	private String arg;
	private int count = 0;
	
	public AppendableBuilder(String arg) {
		this.arg = arg;
	}
	
	public Writer build() throws CucumberException
	{
		try {
			return new UTF8OutputStreamWriter(new URLOutputStream(parseUrl(this.arg)));
		} catch (MalformedURLException e) {
			throw new CucumberException(e);
		} catch (IOException e) {
			throw new CucumberException(e);
		}
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
		return toURL(path + parsePostFix(argument));
		//new URL(path + parsePostFix(argument));
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
	
}
