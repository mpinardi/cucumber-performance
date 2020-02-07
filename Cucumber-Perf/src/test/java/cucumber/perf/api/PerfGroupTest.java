package cucumber.perf.api;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import org.junit.Test;

import cucumber.perf.api.PerfGroup;
import cucumber.perf.api.plan.PathPlanSupplier;
import cucumber.perf.api.plan.PerfPlan;
import cucumber.perf.api.plan.PlanParser;
import cucumber.perf.runtime.CucumberPerfTest.options1;
import cucumber.perf.runtime.PerfRuntimeOptions;
import cucumber.perf.runtime.PerfRuntimeOptionsFactory;
import cucumber.perf.salad.ast.Count;
import cucumber.perf.salad.ast.Group;
import cucumber.perf.salad.ast.Runners;
import cucumber.perf.salad.ast.SimulationDefinition;
import io.cucumber.core.internal.gherkin.ast.DataTable;
import io.cucumber.core.internal.gherkin.ast.Node;
import io.cucumber.core.internal.gherkin.ast.TableCell;
import io.cucumber.core.internal.gherkin.ast.TableRow;

public class PerfGroupTest {


	@Test
	public void testPerfGroupStringStringIntIntDataTable() {
		List<TableCell> tcl = Arrays.asList(new TableCell[] {new TableCell(null, "value out")});
		List<TableCell> tcl2 = Arrays.asList(new TableCell[] {new TableCell(null, "test")});
		DataTable d = new DataTable(Arrays.asList(new TableRow[] {new TableRow(null, tcl),new TableRow(null, tcl2)}));
		PerfGroup pg = new PerfGroup("When","Test",1, 3, d);
		assertEquals(3,pg.getCount());
	}

	@Test
	public void testPerfGroupGroup() {
		List<Node> args = new ArrayList<Node>();
		args.add(new Count(null,"Count","3"));
		args.add(new Runners(null,"Runners","1"));
		PerfGroup pg = new PerfGroup(new Group(null, "Group", "test.feature", args));
		assertEquals(3,pg.getCount());
	}

	@Test
	public void testGetMaxThreads() {
		PerfRuntimeOptionsFactory optf = new PerfRuntimeOptionsFactory(options1.class);
		PerfRuntimeOptions opt = optf.create();
		PlanParser parser = new PlanParser(UUID::randomUUID);
		Supplier<ClassLoader> classLoader = this.getClass()::getClassLoader;
		List<PerfPlan> res =  new PathPlanSupplier(classLoader, opt.getPlanPaths(), parser).get();
	    List <PerfGroup> pg = buildGroups(res.get(0).getSaladPlan().getPlan().getChildren().get(0));
		assertEquals(2,pg.get(0).getMaxThreads());
	}

	@Test
	public void testSetThreads() {
			List<Node> args = new ArrayList<Node>();
			args.add(new Count(null,"Count","3"));
			args.add(new Runners(null,"Runners","1"));
			PerfGroup pg = new PerfGroup(new Group(null, "Group", "test.feature", args));
			pg.setThreads(11);
			assertEquals(11,pg.getThreads());
	}

	@Test
	public void testGetRunning() {
		List<Node> args = new ArrayList<Node>();
		args.add(new Count(null,"Count","3"));
		args.add(new Runners(null,"Runners","1"));
		PerfGroup pg = new PerfGroup(new Group(null, "Group", "test.feature",args));
		assertEquals(0,pg.getRunning());
	}

	@Test
	public void testGetKeyword() {
		List<Node> args = new ArrayList<Node>();
		args.add(new Count(null,"Count","3"));
		args.add(new Runners(null,"Runners","1"));
		PerfGroup pg = new PerfGroup(new Group(null, "Group", "test.feature", args));
		assertEquals("Group",pg.getKeyword());
	}

	@Test
	public void testGetText() {
		List<Node> args = new ArrayList<Node>();
		args.add(new Count(null,"Count","3"));
		args.add(new Runners(null,"Runners","1"));
		PerfGroup pg = new PerfGroup(new Group(null, "Group", "test.feature", args));
		assertEquals("test.feature",pg.getText());
	}

	@Test
	public void testIncrementRunning() {
		List<Node> args = new ArrayList<Node>();
		args.add(new Count(null,"Count","3"));
		args.add(new Runners(null,"Runners","1"));
		PerfGroup pg = new PerfGroup(new Group(null, "Group", "test.feature",args));
		pg.incrementRunning();
		assertEquals(1,pg.getRunning());
	}

	@Test
	public void testDecrementRunning() {
		List<Node> args = new ArrayList<Node>();
		args.add(new Count(null,"Count","3"));
		args.add(new Runners(null,"Runners","1"));
		PerfGroup pg = new PerfGroup(new Group(null, "Group", "test.feature", args));
		pg.decrementRunning();
		assertEquals(-1,pg.getRunning());
	}

	@Test
	public void testGetRan() {
		List<Node> args = new ArrayList<Node>();
		args.add(new Count(null,"Count","3"));
		args.add(new Runners(null,"Runners","1"));
		PerfGroup pg = new PerfGroup(new Group(null, "Group", "test.feature", args));
		pg.incrementRan();
		pg.incrementRan();
		assertEquals(2,pg.getRan());
	}


	private List<PerfGroup> buildGroups(SimulationDefinition sim) {
		List<PerfGroup> groups = new ArrayList<PerfGroup>();
		for (Group g : sim.getGroups()) {
			groups.add(new PerfGroup(g));
		}
		return groups;
	}
}
