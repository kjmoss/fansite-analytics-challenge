package fansite_analytics;

public class FileObj {
	
	private String fullPath;
	//int fileSize;
	/*
	 * In case memory sent differs from request to request for some unknown reason, we cannot fix the file size.
	 * Ideally we could;
	 */
	private int numAccesses = 0;
	private long memSent = 0;
	private int fileRank = -1;
	
	public FileObj(String fullPath){
		this.fullPath = fullPath;
	}
	
	
	
	public boolean equals(String fullPathName){
		return this.fullPath.equals(fullPathName);
	}
	
	
	public FileObj increment(int memSent){
		numAccesses ++;
		this.memSent += memSent;
		return this;
	}
	
	public long getMemSent(){
		return memSent;
	}
	
	public String getFullPath(){
		return fullPath;
	}
	
	public int getFileRank(){
		return fileRank;
	}
	public void setFileRank(int newRank){
		fileRank = newRank;
	}
	
	public int getNumAccesses(){
		return numAccesses;
	}
}
