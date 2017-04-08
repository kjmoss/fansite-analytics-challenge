package fansite_analytics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author kmoss
 *
 */
public class ResourceScanner {
	
	private static final Pattern p = Pattern.compile("^[A-Z]+ (/[^ ]*) HTTP/1\\.0$");
	
	private DirectoryObj rootDir;
	private int numRankings = 10;
	
	private FileObj[] bestFiles;
	
	
	public ResourceScanner(){
		rootDir = new DirectoryObj("/");
		bestFiles = new FileObj[numRankings];
		for(int i = 0; i < bestFiles.length; i++){
			bestFiles[i] = new FileObj("Dummy");
			bestFiles[i].setFileRank(i);
		}
		
		
	}
	
	
	public void next(Request req){
		if(req.getBytes() == 0){
			return;
		}
		Matcher m = p.matcher(req.getRequest());
		if(m.matches()){
			FileObj f = rootDir.incrementFile(m.group(1), m.group(1),req.getBytes());
			
			
			if(f.getFileRank() == -1 && f.getMemSent() > bestFiles[numRankings - 1].getMemSent()){
				bestFiles[numRankings-1].setFileRank(-1);
				bestFiles[numRankings - 1] = f;
				f.setFileRank(numRankings - 1);
				
			}
			for(int i = f.getFileRank(); i > 0; i--){
				if(bestFiles[i].getMemSent() > bestFiles[i-1].getMemSent()){
					FileObj temp = bestFiles[i];
					bestFiles[i] = bestFiles[i-1];
					bestFiles[i-1] = temp;
					bestFiles[i-1].setFileRank(i-1);
					bestFiles[i].setFileRank(i);
				} else {
					break;
				}
			}
		}
	}
	
	
		
	
	
	
	
	private FileObj[] getBestFiles(){
		return bestFiles;
	}
	
	
	
	public static void compileResourceLog(String logFile, String outDirectory) throws IOException{
		

		String line = null;
		
		ResourceScanner rs = new ResourceScanner();
		BufferedReader br = new BufferedReader(new FileReader(logFile));
		
		while((line = br.readLine()) != null){
			try{
				Request req = new Request(line);
				rs.next(req);
				
			} catch(IllegalArgumentException e) {
				System.out.println(e);
			}
		}
		
		br.close();
		
		
		
		
		BufferedWriter writer = new BufferedWriter( new FileWriter(outDirectory + File.separator + "resources.txt"));
		
		for(FileObj f : rs.getBestFiles()){
			if(!f.getFullPath().equals("Dummy")){
				writer.write(f.getFullPath());
				writer.newLine();
			}
			
			//TESTING
			//System.out.println(f.getFullPath()+" "+f.getMemSent()+" "+f.getFileRank());
			
		}
		writer.close();
		
		
	}
	
	
	
	

}
