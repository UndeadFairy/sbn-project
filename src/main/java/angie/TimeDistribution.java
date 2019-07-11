package angie;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TimeDistribution {

private static List<String> getRecordFromLine(String line) {
    List<String> values = new ArrayList<String>();
    Scanner rowScanner = new Scanner(line);
    rowScanner.useDelimiter(";");
    while (rowScanner.hasNext()) {
        values.add(rowScanner.next());
    }
    
    return values;
}
	
public static List<List<String>> loadTweets(String loadname) throws IOException {   
	    
    	String tweetFileName = "src/main/resources/" + loadname + ".txt";
    	List<List<String>> records = new ArrayList<List<String>>();

    	Scanner scanner = new Scanner(new File(tweetFileName));
    	
    	if (scanner.hasNext()) {
    	    // skip header line
    	    scanner.nextLine();
    	}

    	
	    while (scanner.hasNextLine()) {
	        records.add(getRecordFromLine(scanner.nextLine()));
	    }
	    scanner.close();
		return records;
    }

public static ArrayList<Integer> selectDates(List<List<String>> loadname) throws ParseException{
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date dateStart = sdf.parse("2016-04-01");
    Date dateStop = sdf.parse("2016-12-05");
    
	Calendar sDateCalendar = new GregorianCalendar();
    sDateCalendar.setTime(dateStart);
	int weekStart = sDateCalendar.get(Calendar.WEEK_OF_YEAR);
    sDateCalendar.setTime(dateStop);
	int weekEnd = sDateCalendar.get(Calendar.WEEK_OF_YEAR);

	ArrayList<Integer> weekSequence = new ArrayList<Integer>();
	for (int i=weekStart; i<=weekEnd; i++) {
		weekSequence.add(i);
	}

	SimpleDateFormat dateFormatPrint = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	ArrayList<Integer> weekCounter = new ArrayList<Integer>();
	for (int i = 0; i < weekSequence.size(); i++) {
		// pre-fill with zeros to initiate counter
		weekCounter.add(0);
	}


    for (int i = 0; i < loadname.size(); i++) {
    	List<String> row = loadname.get(i);
    	if (row.size() > 4) {
    		//System.out.println(row);
        	String createdAtStr = row.get(4);
        	Date createdAt = dateFormatPrint.parse(createdAtStr);
        	sDateCalendar.setTime(createdAt);
        	int week = sDateCalendar.get(Calendar.WEEK_OF_YEAR);
        	// find index of week to access later
        	int weekIndex = weekSequence.lastIndexOf(week);
        	// get current count
        	int thatWeek = weekCounter.get(weekIndex);
        	// add one
        	weekCounter.set(weekIndex, thatWeek + 1);
    	}
    }
	return weekCounter;
	
}

public static void main (String[] args) throws TwitterException, IOException, ParseException{
	
	List<List<String>> positiveTweets = loadTweets("TwitterTweetDatapositive");
	List<List<String>> negativeTweets = loadTweets("TwitterTweetDatanegative");
	//List<List<String>> sampleTweets = loadTweets("TwitterTweetDatapositive");
	//ArrayList<String> insider = sampleTweets[0]
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date dateStart = sdf.parse("2016-04-01");
    Date dateStop = sdf.parse("2016-12-05");
    
	Calendar sDateCalendar = new GregorianCalendar();
    sDateCalendar.setTime(dateStart);
	int weekStart = sDateCalendar.get(Calendar.WEEK_OF_YEAR);
    sDateCalendar.setTime(dateStop);
	int weekEnd = sDateCalendar.get(Calendar.WEEK_OF_YEAR);

	ArrayList<Integer> weekSequence = new ArrayList<Integer>();
	for (int i=weekStart; i<=weekEnd; i++) {
		weekSequence.add(i);
	}
	
	ArrayList <Integer> weeksPositive = selectDates(positiveTweets);
	ArrayList <Integer> weeksNegative = selectDates(negativeTweets);

	System.out.println(weeksPositive);
	System.out.println(weeksNegative);
	
	//write output of histogram count
	String histogramFileName = "src/main/resources/TweetHistogram.txt";
	File tweetFile = new File(histogramFileName);
	tweetFile.delete();
	PrintWriter pw = new PrintWriter(new FileWriter(histogramFileName, true));
    pw.println(weekSequence);
    pw.println(weeksPositive);
    pw.println(weeksNegative);
    pw.close();
	
}

 

}
