package cucumber.perf.runtime.formatter;

import static cucumber.runtime.Utils.toURL;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cucumber.api.Result;
import cucumber.api.formatter.NiceAppendable;
import cucumber.perf.api.PerfGroup;
import cucumber.perf.api.event.EventBus;
import cucumber.perf.api.event.EventHandler;
import cucumber.perf.api.event.EventListener;
import cucumber.perf.api.event.EventPublisher;
import cucumber.perf.api.event.GroupFinished;
import cucumber.perf.api.event.PerfRunStarted;
import cucumber.perf.api.event.SimulationFinished;
import cucumber.perf.api.event.SimulationStarted;
import cucumber.perf.api.formatter.EventWriter;
import cucumber.perf.api.result.BaseResult;
import cucumber.perf.api.result.GroupResult;
import cucumber.perf.api.result.ScenarioResult;
import cucumber.perf.api.result.SimulationResult;
import cucumber.perf.api.result.StepResult;
import cucumber.perf.api.result.TestCase;
import cucumber.perf.api.result.TestStep;
import cucumber.runtime.CucumberException;
import gherkin.deps.com.google.gson.Gson;
import gherkin.pickles.PickleTag;

public class LoggerFormatter implements EventListener, EventWriter {

	private NiceAppendable out;
	private AppendableBuilder builder;
	private String filePath = null;
	private EventBus eventBus = null;
	
	public LoggerFormatter(AppendableBuilder builder) {
		this.builder = builder;
	}
	
	public LoggerFormatter(AppendableBuilder builder, String[] options) {
		this.builder = builder;
		if (options != null && options[0].length() > 0) {
			this.filePath = options[0];
		}
	}

	private EventHandler<GroupFinished> groupFinishedEventhandler=new EventHandler<GroupFinished>(){@Override public void receive(GroupFinished event){log(event.getGroup(),event.getResult());}};

	private EventHandler<SimulationStarted> simStartedEventhandler=new EventHandler<SimulationStarted>(){@Override public void receive(SimulationStarted event){reset(event.getName());}};

	private EventHandler<SimulationFinished> simFinishedEventhandler=new EventHandler<SimulationFinished>(){@Override public void receive(SimulationFinished event){finish();}};

	private EventHandler<PerfRunStarted> perfRunStartedEventHandler = new EventHandler<PerfRunStarted>() {
         @Override
         public void receive(PerfRunStarted event) {
         	processFile();
         }
     };

	@Override
	public void setEventPublisher(EventPublisher publisher) {
		publisher.registerHandlerFor(GroupFinished.class, groupFinishedEventhandler);
		publisher.registerHandlerFor(SimulationStarted.class, simStartedEventhandler);
		publisher.registerHandlerFor(SimulationFinished.class, simFinishedEventhandler);
		publisher.registerHandlerFor(PerfRunStarted.class, perfRunStartedEventHandler);
	}
	
	@Override
	public void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	private void log(PerfGroup group, GroupResult result) {
		Gson gson = new Gson();
		String json = gson.toJson(result);
		out.println(",");
		out.append(json);
	}

	private void finish() {
		try {
			out.append(']');
			out.close();
		} catch (RuntimeException e)
		{
			//Logger spoofed the call
		}
	}

	private void reset(String name) {
		out = new NiceAppendable(this.builder.build());
		out.println("[");
		out.append("{\"simulation\": "+name+"}");
	}
	
	@SuppressWarnings("unchecked")
	private void processFile() {
		if (filePath != null)
		{
			//String fc = readFile(filePath);
			//String fc = loadFromFileSystemOrClasspath(URIPath.parse(filePath));
			String fc = loadFromUrl(toURL(filePath));
			if (!fc.isEmpty())
			{
				if (!fc.endsWith("]"))
					fc = fc + ']';
				Gson gson = new Gson();
				List<Map<String, Object>> json = gson.fromJson(fc, new ArrayList<Map<String, Object>>().getClass());
				eventBus.send(new SimulationFinished(eventBus.getTime(),eventBus.getTimeMillis(),createSimulationResult(json)));
			}
		}
	}

	public SimulationResult createSimulationResult(List<Map<String, Object>> mapList) {
		Map<String, Object> sim = mapList.remove(0);
		Gson gson = new Gson();
		String name = (String) sim.get("simulation");
		LocalDateTime start = gson.fromJson(gson.toJson(mapList.get(0).get("start")), LocalDateTime.class);
		LocalDateTime stop = gson.fromJson(gson.toJson(mapList.get((mapList.size()-1)).get("stop")), LocalDateTime.class);
		return new SimulationResult(name,new Result(Result.Type.PASSED, (stop.toInstant(ZoneOffset.UTC).getEpochSecond()-start.toInstant(ZoneOffset.UTC).getEpochSecond()), null),start,stop,createGroupResultList(mapList));
	}
	
	public List<GroupResult> createGroupResultList(List<Map<String, Object>> mapList) {
		List<GroupResult> result = new ArrayList<GroupResult>();
		for (Map<String, Object> map : mapList) {
			result.add(createGroupResult(map));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private GroupResult createGroupResult(Map<String, Object> map) {
		BaseResult br = createBaseResult(map);
		GroupResult result = new GroupResult(br.getName(), br.getResult(), br.getStart(), br.getStop());
		for (Map<String, Object> m : (List<Map<String, Object>>) map.get("childResults")) {
			result.addChildResult(createScenarioResult(m));
		}
		return result;
	}

	private BaseResult createBaseResult(Map<String, Object> map) {
		@SuppressWarnings("unchecked")
		Result result = createResult((Map<String, Object>) map.get("result"));
		Gson gson = new Gson();
		LocalDateTime start = gson.fromJson(gson.toJson(map.get("start")), LocalDateTime.class);
		LocalDateTime stop = gson.fromJson(gson.toJson(map.get("stop")), LocalDateTime.class);
		return new BaseResult((String) map.get("name"), result, start, stop);
	}

	@SuppressWarnings("unchecked")
	private ScenarioResult createScenarioResult(Map<String, Object> map) {
		Gson gson = new Gson();
		BaseResult br = createBaseResult(map);
		Map<String, Object> tcm = (Map<String, Object>) map.get("testCase");
		List<PickleTag> tags = gson.fromJson(gson.toJson(tcm.get("tags")), (new ArrayList<PickleTag>()).getClass());
		List<cucumber.api.TestStep> testSteps = gson.fromJson(gson.toJson(tcm.get("testSteps")),
				(new ArrayList<TestStep>()).getClass());
		TestCase tc = new TestCase((int) ((double) tcm.get("line")), (String) tcm.get("uri"), (String) tcm.get("name"),
				(String) tcm.get("scenarioDesignation"), tags, testSteps);
		ScenarioResult result = new ScenarioResult(br.getName(), tc, br.getResult(), br.getStart(), br.getStop());
		for (Map<String, Object> m : (List<Map<String, Object>>) map.get("childResults")) {
			result.addChildResult(createStepResult(m));
		}
		return result;
	}

	private StepResult createStepResult(Map<String, Object> map) {
		Gson gson = new Gson();
		return gson.fromJson(gson.toJson(map), StepResult.class);
	}

	private Result createResult(Map<String, Object> map) {
		Gson gson = new Gson();
		return gson.fromJson(gson.toJson(map), Result.class);
	}
	
	private static String loadFromUrl(URL url) {
		
		String result = "";
		InputStreamReader in = getInputStream(url);
		try (BufferedReader br = new BufferedReader(in)) {
			String nextLine;
			while ((nextLine = br.readLine()) != null) {
				result += nextLine;
			}
		} catch (IOException e) {

		}
		closeQuietly(in);
		return result;
	}
	
	private static InputStreamReader getInputStream(URL url)
	{
		try {
			return new UTF8InputStreamReader(new URLInputStream(url));
		} catch (MalformedURLException e) {
			throw new CucumberException(e);
		} catch (IOException e) {
			throw new CucumberException(e);
		} 
	}
	
	private static void closeQuietly(Closeable in) {
		try {
			in.close();
		} catch (IOException ignored) {
			// go gentle into that good night
		}
	}
}
