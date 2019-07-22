package cucumber.perf.api.cli;

import cucumber.perf.runtime.CucumberPerf;

public class Main {
	public static void main(String[] args) {
		CucumberPerf cukePerf = new CucumberPerf(args);
		byte status = 0;
		try {
			cukePerf.runThreads();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		cukePerf.printResult();
		System.exit(status);
	}
}
