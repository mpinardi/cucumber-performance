package cucumber.perf.runtime.formatter;

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
import cucumber.perf.api.event.EventHandler;
import cucumber.perf.api.event.EventListener;
import cucumber.perf.api.event.EventPublisher;
import cucumber.perf.api.event.StatisticsFinished;
import cucumber.perf.api.result.GroupResult;
import cucumber.perf.api.result.ScenarioResult;
import cucumber.perf.api.result.statistics.Statistics;
import cucumber.runtime.CucumberException;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map.Entry;

public final class JUnitFormatter implements EventListener {
	private AppendableBuilder builder;
	private Writer out;
	private Document doc;
	private Element rootElement = null;
	private TestCase testCase;
	private Element root;

	private EventHandler<StatisticsFinished> statisticsFinishedHandler = new EventHandler<StatisticsFinished>() {
		@Override
		public void receive(StatisticsFinished event) {
			process(event.getResult());
		}
	};

	public JUnitFormatter(AppendableBuilder builder) {
		this.builder = builder;
	}
	
	@Override
	public void setEventPublisher(EventPublisher publisher) {
		publisher.registerHandlerFor(StatisticsFinished.class, statisticsFinishedHandler);
	}

	private void reset() {
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
		for (Entry<String, GroupResult> r : stats.getGroups().entrySet()) {
			this.addFeatureResult(r.getValue());
		}
		this.finishReport();
	}

	private void addFeatureResult(GroupResult fresult) {
		for (ScenarioResult res : fresult.getChildResults()) {
			addScenarioResult(res);
		}
	}

	private void addScenarioResult(ScenarioResult result) {
		if (TestCase.currentFeatureFile == null || !TestCase.currentFeatureFile.equals(result.getTestCase().getUri())) {
			TestCase.currentFeatureFile = result.getTestCase().getUri();
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

	private void finishReport() {
		try {
			this.out = this.builder.build();
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
