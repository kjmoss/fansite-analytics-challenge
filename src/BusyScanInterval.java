package fansite_analytics;

public class BusyScanInterval {
	
	private long startTime;
	private int[] trafficArray;
	private int intervalLength;
	private int maxTraffic = 0;
	private int maxTrafficIndex = 0;
	private int minTraffic = 0;
	//int minTrafficIndex = 0;

	private int currentIndex = 0;
	
	
	public BusyScanInterval(long startTime, int intervalLength){
		this.startTime = startTime;
		trafficArray = new int[intervalLength];
		this.intervalLength = intervalLength;
	}
	
	public boolean addNext(int traffic){
		trafficArray[currentIndex] = traffic;
		
		if(currentIndex == 0){
			maxTraffic = traffic;
			minTraffic = traffic;
		} else {
			if(traffic > maxTraffic){
				maxTraffic = traffic;
				maxTrafficIndex = currentIndex;
			}
			if(traffic < minTraffic){
				minTraffic = traffic;
				//minTrafficIndex = currentIndex;
			}
		}
		currentIndex++;
		return currentIndex >= intervalLength;
	}
	
	public int getMaxTraffic(){
		return maxTraffic;
	}
	public int getMinTraffic(){
		return minTraffic;
	}
	public long getMaxTrafficTime(){
		return startTime + maxTrafficIndex;
	}
	
	public int getMaxTrafficIndex(int lowerBound, int upperBound){
		int low = lowerBound < 0 ? 0 : lowerBound;
		int high = upperBound > intervalLength - 1? intervalLength - 1 : upperBound;
		int max = 0;
		int maxInd = low;
		for(int i = low; i <= high; i++){
			if(trafficArray[i] > max){
				max = trafficArray[i];
				maxInd = i;
			}
		}
		return maxInd;
	}
	public int getTrafficAt(int i){
		return trafficArray[i];
	}
	
	public int getCurrentIndex(){
		return currentIndex;
	}
	
	@Deprecated
	public BusyScanInterval getMaxTrafficDummy(int lowerBound, int upperBound){//TODO: make subinterval, not just single time
		int index = getMaxTrafficIndex(lowerBound, upperBound);
		long start = startTime + index;
		BusyScanInterval bs = new BusyScanInterval(start, 1);
		bs.addNext(trafficArray[index]);
		return bs;
	}
	
	public long getStartTime(){
		return startTime;
	}
	public long getEndTime(){
		return startTime + intervalLength - 1;
	}
	
	public int getIntervalLength(){
		return intervalLength;
	}
	
	public BusyScanInterval getSubInterval(int lowerBound, int upperBound){
		BusyScanInterval bsi = new BusyScanInterval(startTime + lowerBound, upperBound - lowerBound + 1);
		for(int i = lowerBound; i <= upperBound; i++){
			bsi.addNext(trafficArray[i]);
		}
		return bsi;
		
	}

}
