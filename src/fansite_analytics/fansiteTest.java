package fansite_analytics;

import java.io.File;
import java.io.IOException;


public class fansiteTest {

	public static void main(String[] args) throws IOException {
		
		String logFile = "./log_input/log.txt";
		String outDirectory = "./log_output";
		File outDir = new File(outDirectory);
		outDir.mkdir();
		
		long start, end;
		
		start = System.currentTimeMillis();
		BlockScanner.compileBlockedLog(logFile, outDirectory);
		end = System.currentTimeMillis();
		System.out.println("Blocked log done: " + (end-start) + " milliseconds");
		
		start = System.currentTimeMillis();
		ActiveHostScanner.compileHostsLog(logFile, outDirectory);
		end = System.currentTimeMillis();
		System.out.println("Host log done: " + (end-start) + " milliseconds");
		
		
		start = System.currentTimeMillis();
		ResourceScanner.compileResourceLog(logFile, outDirectory);
		end = System.currentTimeMillis();
		System.out.println("Resource log done: " + (end-start) + " milliseconds");
		
		start = System.currentTimeMillis();
		BusyScanner.compileBusyLog(logFile, outDirectory);
		end = System.currentTimeMillis();
		System.out.println("Busy log done: " + (end-start) + " milliseconds");
		
		
	}
	
}
