package fansite_analytics;

public class Host {
	
	private String hostName;
	private int hostCount = 1;
	private int hostRank = -1;//current ranking. Top is 0. -1 if not in scoreboard
	
	
	public Host(String hostName){
		this.hostName = hostName;
	}
	
	
	public boolean tryAdding(String newHostName){
		if(newHostName.equals(hostName)){
			hostCount++;
			return true;
		}
		return false;
	}
	
	
	public String getName(){
		return hostName;
	}
	public int getCount(){
		return hostCount;
	}

	public boolean equals(String newHostName){
		return hostName.equals(newHostName);
	}
	
	public int getRank(){
		return hostRank;
	}

	public void setRank(int rank){//TODO consider changing access level to protected
		hostRank = rank;
	}

	
	
	
}
