package fansite_analytics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ActiveHostScanner {
	
	public static final long FNV64PRIME = 0x100000001b3L;//1099511628211
	public static final long FNV64INIT = 0xcbf29ce484222325L;//14695981039346656037
	
	private int tableSize;
	private HostBucket[] hostTable;//TODO Implement dynamic resizing
	
	private int numRanks = 10;
	private Host[] topHosts;
	
	public ActiveHostScanner(int tableSize){
		this.tableSize = tableSize;
		hostTable = new HostBucket[tableSize];
		
		topHosts = new Host[numRanks];
		for(int i = 0; i < numRanks; i++){
			topHosts[i] = new Host("filler");//Filler host. This will have initial count 1, which had better not be in the top rankings
			topHosts[i].setRank(i);
		}
		
		
	}
	
	
	/**
	 * Applies the 64-bit Fowler-No-Voll hash function.
	 * @param s The string to be hashed. Should be nonempty.
	 * @return A hashed value
	 */
	public static long FNVHash(String s){//TODO: consider sanitizing input first... (all lower-case, etc.)
		if(s.equals("")){
			throw new IllegalArgumentException("The empty string is not a valid input.");
		}
		s = s.toLowerCase();
		char[] c = s.toCharArray();
		long h = FNV64INIT;
		for(int i = 0; i < c.length; i++){
			h ^= c[i]; //check that these are functioning as intended (e.g. negatives?)
			h *= FNV64PRIME;
		}
		return h; 
	}
	
	
	public void next(Request req){
		int reqIndex = ((int)(FNVHash(req.getHost())%tableSize) + tableSize)%tableSize;//Ensuring the index isn't negative
		if(hostTable[reqIndex] == null){
			hostTable[reqIndex] = new HostBucket();
		}
		Host h = hostTable[reqIndex].add(req.getHost());//increments count for host and returns host for further analysis
		
		//check if h is now in rankings
		if(h.getRank() == -1 && topHosts[numRanks - 1].getCount() < h.getCount()){
			h.setRank(numRanks-1);
			topHosts[numRanks - 1].setRank(-1);
			topHosts[numRanks-1] = h;
		}
		
		//possibly reorder ranks
		for(int i = h.getRank(); i > 0; i--){//doesn't trigger if h is not in rankings (i = -1)
			if(topHosts[i-1].getCount() < topHosts[i].getCount()){
				Host temp = topHosts[i-1];
				topHosts[i-1] = topHosts[i];
				topHosts[i] = temp;
				topHosts[i].setRank(i);;
				topHosts[i-1].setRank(i-1);;
			} else {
				break;
			}
		}
		
	
	}
	
	public void printTopToConsole(){
		for(Host h : topHosts){
			System.out.println(h.getName()+","+h.getCount());
		}
		System.out.println("Table size: "+tableSize+"; Number of hosts: "+HostBucket.numUniqueHosts +"; Collisions: "+HostBucket.numCollisions+"; Max bucket size: "+HostBucket.maxBucketSize);
		
	}
	
	
	public static void compileHostsLog(String logFile, String outDirectory, int tableSize) throws IOException{
		
		
		String line = null;
		
		ActiveHostScanner ahs = new ActiveHostScanner(tableSize);
		BufferedReader br = new BufferedReader(new FileReader(logFile));
		
		while((line = br.readLine()) != null){
			try{
				Request req = new Request(line);
				ahs.next(req);
				
			} catch(IllegalArgumentException e) {
				System.out.println(e);
			}
		}
		
		br.close();
		
		//ahs.printTopToConsole();//testing
		
		
		
		
		BufferedWriter writer = new BufferedWriter( new FileWriter(outDirectory + File.separator + "hosts.txt"));
		
		for(Host h : ahs.topHosts){
			if(!h.getName().equals("filler")){
				writer.write(h.getName()+","+h.getCount());
				writer.newLine();
			}
		}
		writer.close();
		
		
		
		
	}
	
	
	public static void compileHostsLog(String logFile, String outDirectory) throws IOException{
		compileHostsLog(logFile, outDirectory, 4000000);
	}
	


}
