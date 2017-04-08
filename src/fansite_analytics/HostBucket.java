package fansite_analytics;

import java.util.ArrayList;

public class HostBucket {
	
	//private ArrayList<String> hostNames;
	//private ArrayList<Integer> hostCounter;
	public static int numCollisions = 0;//for testing
	public static int maxBucketSize = 0;//for testing
	public static int numUniqueHosts = 0;//for testing
	private ArrayList<Host> hostList;
	
	
	public HostBucket(){
		//hostNames = new ArrayList<String>();
		//hostCounter = new ArrayList<Integer>();
		hostList = new ArrayList<Host>();
	}
	
	public Host add(String host){
		for(int i = 0; i < hostList.size(); i++){
			if(hostList.get(i).tryAdding(host)){
				return hostList.get(i);
			}
		}
		hostList.add(new Host(host));
		numUniqueHosts++;
		if(hostList.size() > 1){
			numCollisions++;
			if(hostList.size() > maxBucketSize){
				maxBucketSize = hostList.size();
			}
		}
		return hostList.get(hostList.size() - 1);
	}
	
	/*
	public int getHostCount(int index){
		return hostCounter.get(index);
	}*/

	public static void resetTestVars(){
		numCollisions = 0;
		maxBucketSize = 0;
		numUniqueHosts = 0;
	}

}
