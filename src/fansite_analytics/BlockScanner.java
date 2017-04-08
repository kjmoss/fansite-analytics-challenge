package fansite_analytics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Scans and records requests that occur within five minutes three consecutive failed login attempts by a user within 20 seconds.
 * Failed login attempts are counted as those with an HTTP reply code outside 200-299.
 * A successful login resets the counter.
 * 
 * The scanner takes Requests sequentially (in chronological order). It keeps track of the current time and two linked lists that
 * depend on the time. One linked list, "current", contains all failed login attempts within 20 seconds; and the other, "blocked",
 * contains requests within 5 minutes that initiate a block. Before considering the new request, expired requests are removed from
 * the beginning of the linked lists.
 * 
 * A new request is then checked against "blocked". If it's blocked, it's sent to the blocked log. Otherwise, it is checked
 * against "current". If the login is a success, all (failed) attempts in "current" are removed. If it is a fail, attempts in
 * "current" are incremented and it is added. If a fail in "current" reaches counter 3, the new request is added to "blocked".
 * 
 * (The associated requests in "current" are left alone and allowed to expire. This doesn't affect runtime significantly, but
 * if the block time were shorter than request time, we'd have to fix it.)
 * 
 * 
 * 
 * @author kmoss
 *
 */
public class BlockScanner {
	
	private LinkedList<Request> current;
	private LinkedList<Request> blocked;
	private long currentTime;
	private int numRequestsToStartBlocking = 3;
	
	private ArrayList<String> blockLog;
	
	public BlockScanner(){
		current = new LinkedList<Request>();
		blocked = new LinkedList<Request>();
		blockLog = new ArrayList<String>();
	}
	
	/**
	 * Increments the scanner.
	 * @param r An access request.
	 */
	public void next(Request r){
		currentTime = r.getTime();
		
		if(!current.isEmpty()){
			while(currentTime - current.getFirst().getTime() > 20){
				current.removeFirst();
				if(current.isEmpty()){
					break;
				}
			}
		}
		if(!blocked.isEmpty()){
			while(currentTime - blocked.getFirst().getTime() > 300){
				blocked.removeFirst();
				if(blocked.isEmpty()){
					break;
				}
			}
		}
		
		boolean blockedRequest = false;
		for(Request rb : blocked){
			if(rb.getHost().equals(r.getHost())){
				blockLog.add(r.getRequestString());
				//System.out.println(r.getRequestString());
				blockedRequest = true;
				break;
			}
		}
		if(!blockedRequest){
			if(200 <= r.getReplyCode() && r.getReplyCode() < 300){//TODO: confirm that these are the success codes
				
				//remove failed logins from current
				ListIterator<Request> currentItr = current.listIterator();
				while(currentItr.hasNext()){
					Request rc = (Request) currentItr.next();
					if(rc.getHost().equals(r.getHost())){
						currentItr.remove();
					}
				}
			} else {
				
				boolean newBlock = false;
				for(Request rc : current){
					if(rc.getHost().equals(r.getHost())){
						rc.addBlockToken();
						if(rc.getBlockTokens() >= numRequestsToStartBlocking){
							blocked.add(r);
							newBlock = true;
							break;
						}
					}
				}
				if(!newBlock){
					current.add(r);
					r.addBlockToken();
				}
			}
			
			
		}		
	}
	
	/**
	 * Returns a copy of the current log of blocked access requests.
	 * @return A chronological list of access requests.
	 */
	public ArrayList<String> getBlockedLog(){
		return new ArrayList<String>(blockLog);
	}
	
	
	
	public static void compileBlockedLog(String logFile, String outDirectory) throws IOException{
		
		String line = null;
		
		BlockScanner bs = new BlockScanner();
		BufferedReader br = new BufferedReader(new FileReader(logFile));
		
		while((line = br.readLine()) != null){
			try{
				Request req = new Request(line);
				//long time = req.getTime();
				
				bs.next(req);
				
			} catch(IllegalArgumentException e) {
				System.out.println(e);
			}
		}
		
		br.close();
		
		ArrayList<String> blocked = bs.getBlockedLog();
		
		//File blocked = new File(outDirectory + File.separator + "blocked.txt");
		
		
		BufferedWriter writer = new BufferedWriter( new FileWriter(outDirectory + File.separator + "blocked.txt"));
		
		for(String s : blocked){
			writer.write(s);
			writer.newLine();
		}
		writer.close();
		
	}
	
	
	

}
