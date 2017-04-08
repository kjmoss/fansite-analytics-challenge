package fansite_analytics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;


/**
 * Scans and determines the busiest hours (by number of requests) for the site. Hours are set to be disjoint, so lower-ranking
 * hours are required to be disjoint from higher-ranking hours.
 * 
 * The scanner keeps a record of the current time (by timestamp of last request), and has a linkedlist of number of requests 
 * within each second for the last 3600 seconds (1 hour). It also keeps a running total that is modified in conjunction with
 * the linkedlist.
 * 
 * Whenever a new access request is considered, the scanner first checks if the request is in a new second. If not, the current
 * number of requests within the second is incremented and nothing else happens. If there is a new second, the scanner
 * increments seconds until it reaches the new time, storing the overall sum each time it increments.
 * 
 * The overall sum is stored in an object called an BusyScanInterval. Each interval stores sums for an interval of time and
 * calculates the maximum (and other data) among sums within that interval. Intervals have a fixed length of at most an hour,
 * so each interval can contain at most one candidate for busiest hour.
 * 
 * The intervals with the highest maximums are stored in the ranking, where there are a sufficient number so that even after
 * some are discarded due to overlap, there are enough to fill the rankings. After the intervals have been found, they're
 * cross-checked so lower-ranking intervals that are completely within 60 minutes of the highest interval's time are dropped.
 * When there is only some overlap and the max is within the overlap, a sub-interval is made for spots outside the overlap, the
 * old interval is removed, and the new interval takes its proper place in the rankings.
 * 
 * @author kmoss
 *
 */
public class BusyScanner {
	
	private LinkedList<Integer> smallSums = new LinkedList<Integer>();
	
	private int currentSmall;//running total of number of requests within second
	private int currentLarge;//running total of number of requests within hour
	
	private long currentTime; //timestamp for current request
	private long prevTime; //timestamp for previous request
	
	private int intervalLength = 1800;//TODO: Implement variable interval length. Throw exceptions if it's less than 1 or more than 3600.
	private int recordNum;
	private BusyScanInterval[] bestIntervals;
	
	private BusyScanInterval currentInterval;
	private int numRankings = 10;//TODO: Implement variable num rankings...
	
	
	
	public BusyScanner(){
		this(10,1800);
	}
	
	public BusyScanner(int numRankings, int intervalLength){
		this.numRankings = numRankings;
		this.intervalLength = intervalLength;
		
		prevTime = 0;//TODO:For efficiency (and to implement min-traffic correctly), setPrevTime to start time.
		currentLarge = 0;
		currentSmall = 0;
		for(int i = 0; i < 3600; i++){
			smallSums.add(0);
		}
		int temp = (3599 + intervalLength)/intervalLength;
		recordNum = numRankings*(2*temp + 1);//number of intervals to store
		bestIntervals = new BusyScanInterval[recordNum];
		for(int i = 0; i < recordNum; i++){
			bestIntervals[i] = new BusyScanInterval(0,intervalLength);
		}
		currentInterval = new BusyScanInterval(prevTime,intervalLength);
	}
	
	
	
	public void next(long requestTime){	
		
		currentTime = requestTime;
		if(currentTime == prevTime){//still within same second
			currentSmall++;
			return;
		}
		
		//not within same second...
		
		long newSeconds = currentTime - prevTime;//first check if the time gap is large
		if(newSeconds > 3600 + intervalLength){
			newSeconds = 3600 + intervalLength + 1;//indicates that the last interval should be discarded without considering
		}
		
		//iterate on 
		for(int i = 1; i < newSeconds; i++){
			currentLarge -= smallSums.removeFirst();
			smallSums.add(0);
			if(currentInterval.addNext(currentLarge)){
				consider(currentInterval);
				currentInterval = new BusyScanInterval(prevTime + i + 1, intervalLength);
			}
		}
		if(newSeconds == 3601){
			currentInterval = new BusyScanInterval(currentTime, intervalLength);
		}
		
		
		
		currentLarge -= smallSums.removeFirst();
		smallSums.add(currentSmall);
		currentLarge += currentSmall;
		if(currentInterval.addNext(currentLarge)){
			consider(currentInterval);
			currentInterval = new BusyScanInterval(currentTime + 1, intervalLength);
		}		
		
		prevTime = currentTime;
		currentSmall = 1;
		

		
	}
	
	public void end(){//no more requests to add, so considering the last interval
		if(currentInterval.getCurrentIndex() != 0){
			consider(currentInterval);
		}
	}
	
	
	
	private boolean consider(BusyScanInterval bsi){//TODO: Consider min traffic as well. For considering min traffic, intervals with times within the first hour of start time should be discarded
		if(bsi.getMaxTraffic() <= bestIntervals[recordNum - 1].getMaxTraffic()){
			return false;
		}
		bestIntervals[recordNum - 1] = bsi;
		
		for(int i = recordNum - 2; i >= 0; i--){
			if(bestIntervals[i].getMaxTraffic() < bestIntervals[i+1].getMaxTraffic()){
				BusyScanInterval temp = bestIntervals[i];
				bestIntervals[i] = bestIntervals[i+1];
				bestIntervals[i+1] = temp;
			} else {
				break;
			}
		}
		return true;
	}
	
	
	public void next(Request req){
		next(req.getTime());
	}
	
	
	
	
	public BusyScanInterval[] getBestIntervals(){
		ArrayList<BusyScanInterval> best = new ArrayList<BusyScanInterval>();
		for(int k = 0; k < recordNum; k++){
			best.add(bestIntervals[k]);
		}
		
		int i = 0;
		while(i < recordNum && i < best.size()){
			BusyScanInterval bs = best.get(i);
			long bsTime = bs.getMaxTrafficTime();
			int j = i+1;
			while(j < best.size()){
				BusyScanInterval bsTemp = best.get(j);
				long tempTime = bsTemp.getMaxTrafficTime();
				if(bsTime - tempTime < 3600 && bsTime - tempTime >= 0){
					long subIntLower = bsTemp.getStartTime();
					long subIntUpper = bsTime - 3600;
					if(subIntLower <= subIntUpper){
						best.add(bsTemp.getSubInterval(0, (int)(subIntUpper - subIntLower)));
					}
					best.remove(j);
					j--;
				} else if(tempTime - bsTime < 3600 && tempTime - bsTime >= 0){
					long subIntLower = bsTime + 3600;
					long subIntUpper = bsTemp.getEndTime();
					if(subIntLower <= subIntUpper){
						best.add(bsTemp.getSubInterval((int)(subIntUpper - subIntLower), bsTemp.getIntervalLength() - 1));
						//best.add(bsTemp.getMaxTrafficDummy(0, (int)(subIntLower - subIntUpper)));
					}
					best.remove(j);
					j--;
				}
				int l = best.size() - 1;
				while(best.get(l).getMaxTraffic() > best.get(l-1).getMaxTraffic()){
					BusyScanInterval temp = best.get(l);
					best.set(l, best.get(l-1));
					best.set(l-1, temp);
					l--;
				}
				j++;
			}
			i++;
		}
		
		
		BusyScanInterval[] bestFinal = new BusyScanInterval[numRankings];
		for(int k = 0; k < numRankings; k++){
			if(k < best.size()){
				bestFinal[k] = best.get(k);
			}
		}
		
		return bestFinal;
		
		
		
	}
	
	
	
	
	
	public static void compileBusyLog(String logFile, String outDirectory, int numRankings, int intervalLength) throws IOException{
		String line = null;
		
		BusyScanner bs = new BusyScanner(numRankings, intervalLength);
		BufferedReader br = new BufferedReader(new FileReader(logFile));
		
		while((line = br.readLine()) != null){
			try{
				Request req = new Request(line);
				long time = req.getTime();
				
				bs.next(time);
				
			} catch(IllegalArgumentException e) {
				System.out.println(e);
			}
		}
		bs.end();
		
		br.close();
		
		BusyScanInterval[] bestFinal = bs.getBestIntervals();
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(outDirectory + File.separator + "hours.txt"));
		
		for(BusyScanInterval bsi : bestFinal){
			try{
				long time = bsi.getMaxTrafficTime() - 3599;//using start time rather than end time
				String s = Request.getTimestampString(time) + "," + bsi.getMaxTraffic();
				writer.write(s);
				writer.newLine();
			} catch (NullPointerException e){
				break;
			}
		}
		writer.close();
		
		
		
		
		//Testing
		//for(int i = 0; i < bestFinal.length; i++){
		//	System.out.println(Request.getTimestampString(bestFinal[i].getMaxTrafficTime())+","+bestFinal[i].getMaxTraffic());
		//}
	}
	
	public static void compileBusyLog(String logFile, String outDirectory) throws IOException{
		compileBusyLog(logFile, outDirectory, 10, 1800);
	}
	
	

}
