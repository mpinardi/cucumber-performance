package cucumber.perf.runtime.formatter;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import cucumber.perf.api.PerfGroup;
import cucumber.perf.api.event.ConcurrentEventListener;
import cucumber.perf.api.event.EventHandler;
import cucumber.perf.api.event.EventPublisher;
import cucumber.perf.api.event.GroupFinished;
import cucumber.perf.api.event.GroupStarted;
import cucumber.perf.api.formatter.AnsiEscapes;
import cucumber.perf.api.result.GroupResult;
import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.event.Status;

public final class DetailDisplayPrinter implements ConcurrentEventListener,ColorAware {
	private final PrintStream out;
	private boolean monochrome;
	private List<PerfGroup> groups = new ArrayList<PerfGroup>();
    private static final Map<Status, AnsiEscapes> ANSI_ESCAPES = new HashMap<Status, AnsiEscapes>() {
		private static final long serialVersionUID = 1630641595685292213L;
		{
	        put(Status.PASSED, AnsiEscapes.GREEN);
	        put(Status.UNDEFINED, AnsiEscapes.YELLOW);
	        put(Status.PENDING, AnsiEscapes.YELLOW);
	        put(Status.SKIPPED, AnsiEscapes.CYAN);
	        put(Status.FAILED, AnsiEscapes.RED);
		}
	};
    
	private EventHandler<GroupStarted> groupStartedEventhandler = new EventHandler<GroupStarted>() {
		@Override
		public void receive(GroupStarted event) {
			updatePerfGroup(event.getGroupId(), event.getGroup());
			print(event.getGroupId(),null);
		}
    };
    
	private EventHandler<GroupFinished> groupFinishedEventhandler = new EventHandler<GroupFinished>() {
		@Override
		public void receive(GroupFinished event) {
			updatePerfGroup(event.getGroupId(), event.getGroup());
			print(event.getGroupId(),event.getResult());
		}
    };
    
	public DetailDisplayPrinter() {
		this.out = System.out;
	}
	
	@Override
	public void setMonochrome(boolean monochrome) {
		this.monochrome = monochrome;
	}

	@Override
	public void setEventPublisher(EventPublisher publisher) {
		publisher.registerHandlerFor(GroupStarted.class, groupStartedEventhandler);
		publisher.registerHandlerFor(GroupFinished.class, groupFinishedEventhandler);
		
	}
	public void print(int groupId, GroupResult result) {
		Object status = null;
		if (result != null) {
			status = result.getResult().getStatus();
		}
		
		String line = "";
		int i = 0;
		for (PerfGroup pg : this.groups) {
			if (i > 0) {
				line = line + " | ";
			}
			line += trimFeature(pg.getText()) + ":" + pg.getRunning() + "-" + pg.getMaxThreads() + ">"+ ((i == groupId && status != null && !monochrome)?ANSI_ESCAPES.get(result.getResult().getStatus())+""+pg.getRan()+ AnsiEscapes.RESET:pg.getRan());
			i++;
		}
		out.print(line + "\r");

		/* looking at better options
		 * else { int i = 0; for(PerfGroup pg : groups) {
		 * out.println(trimFeature(pg.getText())+":"+pg.getRunning()+"-"+pg.
		 * getMaxThreads()+">"+pg.getRan()); i++; }
		 * out.print(String.format("\033[%dA",i)); // Move up
		 * System.out.print("\033[2K"); }
		 */
	}
	
	private void updatePerfGroup(int groupId, PerfGroup group) {
		if (this.groups.size() <= groupId)
		{
			this.groups.add(group);
		}
		else
		{
			this.groups.set(groupId,group);
		}
	}

	private String trimFeature(String name) {
		if (name.endsWith(".feature")) {
			return name.substring(0, name.length() - 8);
		}
		return name;
	}
}
