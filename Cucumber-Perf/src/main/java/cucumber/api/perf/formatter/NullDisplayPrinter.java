package cucumber.api.perf.formatter;

import java.io.PrintStream;
import java.util.List;

import cucumber.api.perf.PerfGroup;

public class NullDisplayPrinter implements DisplayPrinter{
	private final PrintStream out;

    public NullDisplayPrinter() {
        this.out = System.out;
    }
	@Override
	public void print(List<PerfGroup> groups) {
		// TODO Auto-generated method stub
		
	}

}
