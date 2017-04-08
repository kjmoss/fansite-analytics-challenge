package fansite_analytics;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirectoryObj {
	
	private ArrayList<DirectoryObj> subdirectories;
	private ArrayList<FileObj> files;
	
	private static Pattern p = Pattern.compile("^([^/ ]*/)([^/ ]*/)([^ ]+)$");
	
	private String directory;
	
	
	public DirectoryObj(String directoryName){// name ends with /
		directory = directoryName;
		subdirectories = new ArrayList<DirectoryObj>();
		files = new ArrayList<FileObj>();
	}
	
	public FileObj incrementFile(String filePath, String fullPath, int bytesAdded){
		Matcher m = p.matcher(filePath);
		if(m.matches()){
			//System.out.println(filePath);
			for(DirectoryObj d : subdirectories){
				if(d.equals(m.group(2))){
					return d.incrementFile(m.group(2)+m.group(3), fullPath, bytesAdded);
				}
			}
			DirectoryObj newDir = new DirectoryObj(m.group(2));
			subdirectories.add(newDir);
			return newDir.incrementFile(m.group(2)+m.group(3), fullPath, bytesAdded);
		} else {
			for(FileObj f : files){
				if(f.equals(fullPath)){
					return f.increment(bytesAdded);
				}
			}
			FileObj newFile = new FileObj(fullPath);
			files.add(newFile);
			return newFile.increment(bytesAdded);
		}
		
	}
	
	public boolean equals(String directoryName){
		return directory.equals(directoryName);
	}

}
