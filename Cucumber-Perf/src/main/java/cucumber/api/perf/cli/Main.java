package cucumber.api.perf.cli;

import cucumber.api.perf.CucumberPerf;

public class Main {
	public static void main(String[] args) {
		CucumberPerf cukePerf = new CucumberPerf(args);
		byte status = 0;
		try {
			cukePerf.runThreads();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(status);
	}
}
