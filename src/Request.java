package fansite_analytics;

//import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.sql.Timestamp;

/**
 * Stores data regarding a request in the log file. Particularly, stores the host, timestamp, request, reply code, and number of bytes in the reply.
 * Note that this only supports time zone -0400.
 * @author kmoss
 *
 */
public class Request {
	private static final Pattern p = Pattern.compile("^([^ ]+) - - \\[([0-9]{2}/[a-zA-Z]{3}/[0-9]{4}:[0-9]{2}:[0-9]{2}:[0-9]{2} -0400)\\] \"(.+)\" ([0-9]+) ([-0-9]+)$");
	private static final Pattern pTime = Pattern.compile("^([0-9]{2})/([a-zA-Z]{3})/([0-9]{4}):([0-9]{2}:[0-9]{2}:[0-9]{2}) -0400$");
	private static final Pattern pTimeJDBC = Pattern.compile("^([0-9]{4})-([0-9]{2})-([0-9]{2}) ([0-9]{2}:[0-9]{2}:[0-9]{2})\\.?[0-9]*$");
	
	private String host, request, timeString;
	private short replyCode;
	private int bytes;
	private long timestamp;
	//private Timestamp timestamp;
	
	private byte blockTokens = 0;
	
	/**
	 * Extracts and stores data regarding an access request, constructed from the request string itself.
	 * @param input A request string.
	 */
	public Request(String input){
		Matcher m = p.matcher(input);
		if(m.matches()){
			host = m.group(1);
			timeString = m.group(2);
			timestamp = constructTimestamp(m.group(2));
			request = m.group(3);
			replyCode = Short.parseShort(m.group(4));
			if(m.group(5).matches("-")){
				bytes = 0;
			} else {
				bytes = Integer.parseInt(m.group(5));
			}
		} else {
			throw new IllegalArgumentException("Request string does not match the expected pattern: \""+input+"\"");
		}
	}
	
	/**
	 * Returns a long representing the number of seconds since January 1 1970 at midnight in time zone -0400.
	 * Only time zone -0400 is currently supported.
	 * 
	 * The long is constructed by first creating a Timestamp object from the input string, and using its getTime() method.
	 * The object java.sql.Timestamp supports JDBC timestamp escape format, so this method converts the input string to
	 * that format and passes it into the object.
	 * 
	 * The input string should be in the form "DD/MON/YYYY:HH:MM:SS -0400", where MON is of the form "Jan", "Feb", etc.
	 * @param input A string in the form "DD/MON/YYYY:HH:MM:SS -0400".
	 * @return Number of seconds since January 1 1970 at midnight in time zone -0400.
	 */
	public static long constructTimestamp(String input){
		Matcher m = pTime.matcher(input);
		if(m.matches()){
			String timeInJDBC;
			String month;
			switch(m.group(2)){
			case "Jan":	month = "01"; break;
			case "Feb": month = "02"; break;
			case "Mar":	month = "03"; break;
			case "Apr":	month = "04"; break;
			case "May":	month = "05"; break;
			case "Jun":	month = "06"; break;
			case "Jul":	month = "07"; break;
			case "Aug":	month = "08"; break;
			case "Sep":	month = "09"; break;
			case "Oct":	month = "10"; break;
			case "Nov":	month = "11"; break;
			case "Dec":	month = "12"; break;
			default:
				throw new IllegalArgumentException("Month does not match expected pattern: "+input);
			}
			timeInJDBC =  m.group(3)+"-"+month+"-"+m.group(1)+" "+m.group(4);
			//long time = Timestamp.valueOf(timeInJDBC).getTime() - 14400000;//subtract 4 hours due to timezone
			//return new Timestamp(time);
			return Timestamp.valueOf(timeInJDBC).getTime()/1000;//This does not keep track of the time zone
		} else {
			throw new IllegalArgumentException("Timestamp does not match expected pattern: "+input);
		}
	}

	/**
	 * Given a java.sql.Timestamp object, returns a string for the timestamp in the format "DD/MON/YYYY:HH:MM:SS -0400".
	 * The string assumes the Timestamp object is in time zone -0400, so it functions appropriately with the method constructTimestamp.
	 * @param timestamp A Timestamp object.
	 * @return A string in the format "DD/MON/YYYY:HH:MM:SS -0400".
	 */
	@Deprecated
	public static String getTimestampString(Timestamp timestamp){
		String time = timestamp.toString();
		Matcher m = pTimeJDBC.matcher(time);
		if(m.matches()){
		String month;
		switch(m.group(2)){
		case "01":	month = "Jan"; break;
		case "02":  month = "Feb"; break;
		case "03":	month = "Mar"; break;
		case "04":	month = "Apr"; break;
		case "05":	month = "May"; break;
		case "06":	month = "Jun"; break;
		case "07":	month = "Jul"; break;
		case "08":	month = "Aug"; break;
		case "09":	month = "Sep"; break;
		case "10":	month = "Oct"; break;
		case "11":	month = "Nov"; break;
		case "12":	month = "Dec"; break;
		default:
			throw new IllegalArgumentException("Month does not match expected pattern: "+time);
		}
		return m.group(3)+"/"+month+"/"+m.group(1)+":"+m.group(4)+" -0400";
		} else {
			throw new IllegalArgumentException("Timestamp does not match expected pattern: "+time);
		}
	}
	
	/**
	 * Returns a timestamp string corresponding to the given time in the format "DD/MON/YYYY:HH:MM:SS -0400". It assumes
	 * time zone -0400.
	 * @param time Milliseconds since January 1 1970 at midnight in time zone -0400.
	 * @return A timestamp in the format "DD/MON/YYYY:HH:MM:SS -0400".
	 */
	public static String getTimestampString(long time){
		Timestamp t = new Timestamp(time*1000);
		Matcher m = pTimeJDBC.matcher(t.toString());
		if(m.matches()){
		String month;
		switch(m.group(2)){
		case "01":	month = "Jan"; break;
		case "02":  month = "Feb"; break;
		case "03":	month = "Mar"; break;
		case "04":	month = "Apr"; break;
		case "05":	month = "May"; break;
		case "06":	month = "Jun"; break;
		case "07":	month = "Jul"; break;
		case "08":	month = "Aug"; break;
		case "09":	month = "Sep"; break;
		case "10":	month = "Oct"; break;
		case "11":	month = "Nov"; break;
		case "12":	month = "Dec"; break;
		default:
			throw new IllegalArgumentException("Month does not match expected pattern: "+time);
		}
		return m.group(3)+"/"+month+"/"+m.group(1)+":"+m.group(4)+" -0400";
		} else {
			throw new IllegalArgumentException("Timestamp does not match expected pattern: "+time);
		}
	}
	
	
	
	
	
	public String getHost(){
		return host;
	}
	
	public String getRequest(){
		return request;
	}
	
	public short getReplyCode(){
		return replyCode;
	}
	
	public int getBytes(){
		return bytes;
	}
	
	public String getTimeString(){
		return timeString;
	}
	
	public long getTime(){
		return timestamp;
	}
	
	
	/**
	 * Reconstructs and returns the line from the log file with data regarding the request string.
	 * @return A string with all the information regarding the request.
	 */
	public String getRequestString(){
		String byteString;
		if(bytes == 0){
			byteString = "-";
		} else {
			byteString = "" + bytes;
		}
		return host + " - - ["+ timeString + "] \""+ request +"\" " + replyCode + " " + byteString;
	}
	
	/**
	 * Adds block tokens. These are used in the BlockScanner.
	 */
	public void addBlockToken(){
		blockTokens++;
	}
	
	public byte getBlockTokens(){
		return blockTokens;
	}
	
	

}
