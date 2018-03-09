package cucumber.api.perf.formatter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cucumber.api.Result;
import cucumber.api.perf.result.FeatureResult;
import cucumber.api.perf.result.ScenarioResult;
import cucumber.runtime.CucumberException;
import cucumber.runtime.io.URLOutputStream;
import cucumber.runtime.io.UTF8OutputStreamWriter;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JUnitFormatter implements Formatter {
	private static final Pattern ARGUMENT_POSTFIX_PATTERN = Pattern.compile("([^|]+)\\|(.*)");
	private static final Pattern ARGUMENT_POSTFIX_SEPARATOR_PATTERN = Pattern.compile("-|\\[|\\]|\\(|\\)|\\{|\\}|_");
	private static final Pattern ARGUMENT_POSTFIX_PART_PATTERN = Pattern
			.compile("(?:(?!#).)+?(?=@)|(?:(?!@).)+?(?=#)|(?:(?!#)(?!@).)*$");
	private Writer out;
	private final String arg;
	private int count = 0;
	private Document doc;
	private Element rootElement = null;
	private TestCase testCase;
	private Element root;

	public JUnitFormatter(String arg) throws IOException {
		this.arg = arg;
		reset();
	}

	public void reset() {
		TestCase.treatConditionallySkippedAsFailure = false;
		TestCase.currentFeatureFile = null;
		TestCase.previousTestCaseName = "";
		TestCase.exampleNumber = 1;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			rootElement = doc.createElement("testsuite");
			doc.appendChild(rootElement);
		} catch (ParserConfigurationException e) {
			throw new CucumberException("Error while processing unit report", e);
		}
	}

	public void process(Statistics stats) {
		reset();
		for (Entry<String, FeatureResult> r : stats.getAvg().entrySet()) {
			this.addFeatureResult(r.getValue());
		}
		this.finishReport();
	}

	public void addFeatureResult(FeatureResult fresult) {
		for (ScenarioResult res : fresult.getChildResults()) {
			addScenarioResult(res);
		}
	}

	private void addScenarioResult(ScenarioResult result) {
		if (TestCase.currentFeatureFile == null || !TestCase.currentFeatureFile.equals(result.getUri())) {
			TestCase.currentFeatureFile = result.getUri();
			TestCase.previousTestCaseName = "";
			TestCase.exampleNumber = 1;
		}
		testCase = new TestCase(result);
		root = testCase.createElement(doc);
		testCase.writeElement(doc, root);
		rootElement.appendChild(root);

		increaseAttributeValue(rootElement, "tests");
		if (testCase.result.getChildResults().isEmpty()) {
			testCase.handleEmptyTestCase(doc, root, testCase.result.getResult());
		} else {
			testCase.addTestCaseElement(doc, root, testCase.result.getResult());
		}
	}

	public void finishReport() {
		try {
			try {
				this.out = new UTF8OutputStreamWriter(new URLOutputStream(parseUrl(arg)));
			} catch (MalformedURLException e) {
				throw new CucumberException(e);
			} catch (IOException e) {
				throw new CucumberException(e);
			}
			// set up a transformer
			rootElement.setAttribute("name", JUnitFormatter.class.getName());
			rootElement.setAttribute("failures",
					String.valueOf(rootElement.getElementsByTagName("failure").getLength()));
			rootElement.setAttribute("skipped",
					String.valueOf(rootElement.getElementsByTagName("skipped").getLength()));
			rootElement.setAttribute("time", sumTimes(rootElement.getElementsByTagName("testcase")));
			if (rootElement.getElementsByTagName("testcase").getLength() == 0) {
				addDummyTestCase(); // to avoid failed Jenkins jobs
			}
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			StreamResult result = new StreamResult(out);
			DOMSource source = new DOMSource(doc);
			trans.transform(source, result);
			closeQuietly(out);
		} catch (TransformerException e) {
			throw new CucumberException("Error while transforming.", e);
		}
	}

	private void addDummyTestCase() {
		Element dummy = doc.createElement("testcase");
		dummy.setAttribute("classname", "dummy");
		dummy.setAttribute("name", "dummy");
		rootElement.appendChild(dummy);
		Element skipped = doc.createElement("skipped");
		skipped.setAttribute("message", "No features found");
		dummy.appendChild(skipped);
	}

	private String sumTimes(NodeList testCaseNodes) {
		double totalDurationSecondsForAllTimes = 0.0d;
		for (int i = 0; i < testCaseNodes.getLength(); i++) {
			try {
				double testCaseTime = Double
						.parseDouble(testCaseNodes.item(i).getAttributes().getNamedItem("time").getNodeValue());
				totalDurationSecondsForAllTimes += testCaseTime;
			} catch (NumberFormatException e) {
				throw new CucumberException(e);
			} catch (NullPointerException e) {
				throw new CucumberException(e);
			}
		}
		DecimalFormat nfmt = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
		nfmt.applyPattern("0.######");
		return nfmt.format(totalDurationSecondsForAllTimes);
	}

	private void increaseAttributeValue(Element element, String attribute) {
		int value = 0;
		if (element.hasAttribute(attribute)) {
			value = Integer.parseInt(element.getAttribute(attribute));
		}
		element.setAttribute(attribute, String.valueOf(++value));
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

	public void setStrict(boolean strict) {
		TestCase.treatConditionallySkippedAsFailure = strict;
	}

	private static class TestCase {
		private static final DecimalFormat NUMBER_FORMAT = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);

		static {
			NUMBER_FORMAT.applyPattern("0.######");
		}

		private TestCase(ScenarioResult result) {
			this.result = result;
		}

		static String currentFeatureFile;
		static String previousTestCaseName;
		static int exampleNumber;
		static boolean treatConditionallySkippedAsFailure = false;
		private final ScenarioResult result;

		private Element createElement(Document doc) {
			return doc.createElement("testcase");
		}

		private void writeElement(Document doc, Element tc) {
			// tc.setAttribute("classname", testSources.getFeatureName(currentFeatureFile));
			tc.setAttribute("name", calculateElementName(result.getName()));
		}

		private String calculateElementName(String name) {
			String testCaseName = name;
			if (testCaseName.equals(previousTestCaseName)) {
				return testCaseName + (includesBlank(testCaseName) ? " " : "_") + ++exampleNumber;
			} else {
				previousTestCaseName = name;
				exampleNumber = 1;
				return testCaseName;
			}
		}

		private boolean includesBlank(String testCaseName) {
			return testCaseName.indexOf(' ') != -1;
		}

		public void addTestCaseElement(Document doc, Element tc, Result result) {
			tc.setAttribute("time", calculateTotalDurationString(result));

			StringBuilder sb = new StringBuilder();
			addStepAndResultListing(sb);
			Element child;
			if (result.is(Result.Type.FAILED)) {
				addStackTrace(sb, result);
				child = createElementWithMessage(doc, sb, "failure", result.getErrorMessage());
			} else if (result.is(Result.Type.AMBIGUOUS)) {
				addStackTrace(sb, result);
				child = createElementWithMessage(doc, sb, "failure", result.getErrorMessage());
			} else if (result.is(Result.Type.PENDING) || result.is(Result.Type.UNDEFINED)) {
				if (treatConditionallySkippedAsFailure) {
					child = createElementWithMessage(doc, sb, "failure",
							"The scenario has pending or undefined step(s)");
				} else {
					child = createElement(doc, sb, "skipped");
				}
			} else if (result.is(Result.Type.SKIPPED) && result.getError() != null) {
				addStackTrace(sb, result);
				child = createElementWithMessage(doc, sb, "skipped", result.getErrorMessage());
			} else {
				child = createElement(doc, sb, "system-out");
			}

			tc.appendChild(child);
		}

		public void handleEmptyTestCase(Document doc, Element tc, Result result) {
			tc.setAttribute("time", calculateTotalDurationString(result));

			String resultType = treatConditionallySkippedAsFailure ? "failure" : "skipped";
			Element child = createElementWithMessage(doc, new StringBuilder(), resultType, "The scenario has no steps");

			tc.appendChild(child);
		}

		private String calculateTotalDurationString(Result result) {
			return NUMBER_FORMAT.format(((double) result.getDuration()) / 1000000000);
		}

		private void addStepAndResultListing(StringBuilder sb) {
			for (int i = 0; i < result.getChildResults().size(); i++) {
				int length = sb.length();
				String resultStatus = "not executed";
				if (i < result.getChildResults().size()) {
					resultStatus = result.getChildResults().get(i).getResult().getStatus().lowerCaseName();
				}
				sb.append(result.getChildResults().get(i).getName());
				do {
					sb.append(".");
				} while (sb.length() - length < 76);
				sb.append(resultStatus);
				sb.append("\n");
			}
		}

		private void addStackTrace(StringBuilder sb, Result failed) {
			sb.append("\nStackTrace:\n");
			StringWriter sw = new StringWriter();
			failed.getError().printStackTrace(new PrintWriter(sw));
			sb.append(sw.toString());
		}

		private Element createElementWithMessage(Document doc, StringBuilder sb, String elementType, String message) {
			Element child = createElement(doc, sb, elementType);
			child.setAttribute("message", message);
			return child;
		}

		private Element createElement(Document doc, StringBuilder sb, String elementType) {
			Element child = doc.createElement(elementType);
			// the createCDATASection method seems to convert "\n" to "\r\n" on Windows, in
			// case
			// data originally contains "\r\n" line separators the result becomes "\r\r\n",
			// which
			// are displayed as double line breaks.
			// TODO Java 7 PR #1147: Inlined System.lineSeparator()
			String systemLineSeperator = System.getProperty("line.separator");
			child.appendChild(doc.createCDATASection(sb.toString().replace(systemLineSeperator, "\n")));
			return child;
		}

	}

	private static void closeQuietly(Closeable out) {
		try {
			out.close();
		} catch (IOException ignored) {
			// go gentle into that good night
		}
	}
}
