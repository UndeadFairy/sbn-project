package angie;

import java.io.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import twitter4j.TwitterException;
import angie.mainTemporalAnalysis;

public class TimeDistribution {

private static List<String> getRecordFromLine(String line, String delimiter) {
	// helper function reading content of file line, saving to list delimited by ';'
    List<String> values = new ArrayList<String>();
    Scanner rowScanner = new Scanner(line);
    rowScanner.useDelimiter(delimiter);
    while (rowScanner.hasNext()) {
        values.add(rowScanner.next());
    }
    rowScanner.close();
    return values;
}
	
public static List<List<String>> loadTweets(String loadname) throws IOException {   
	    // helper function reading content of file, line by line, saving to list of lists
    	String tweetFileName = mainTemporalAnalysis.resourcesPathPart0 + loadname + ".txt";
    	List<List<String>> records = new ArrayList<List<String>>();
    	Scanner scanner = new Scanner(new File(tweetFileName));
    	if (scanner.hasNext()) {
    	    // skip header line
    	    scanner.nextLine();
    	}

	    while (scanner.hasNextLine()) {
	        records.add(getRecordFromLine(scanner.nextLine(), ";"));
	    }
	    scanner.close();
		return records;
    }

public static ArrayList<Integer> selectDates(List<List<String>> loadname) throws ParseException{
	// time analysis performed on tweets
	// creates a data for histogram by week
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date dateStart = sdf.parse(mainTemporalAnalysis.dateStart);
    Date dateStop = sdf.parse(mainTemporalAnalysis.dateEnd);
    // time operations
	Calendar sDateCalendar = new GregorianCalendar();
    sDateCalendar.setTime(dateStart);
	int weekStart = sDateCalendar.get(Calendar.WEEK_OF_YEAR);
    sDateCalendar.setTime(dateStop);
	int weekEnd = sDateCalendar.get(Calendar.WEEK_OF_YEAR);

	ArrayList<Integer> weekSequence = new ArrayList<Integer>();
	for (int i = weekStart; i<=weekEnd; i++) {
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
    	// discard obviously wrong rows, which do not contain time
    	if (row.size() > 4) {
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
	return weekCounter; // data for histogram
}

public static void main (String[] args) throws TwitterException, IOException, ParseException{
	List<List<String>> positiveTweets = loadTweets("TwitterTweetDatapositive");
	List<List<String>> negativeTweets = loadTweets("TwitterTweetDatanegative");
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date dateStart = sdf.parse(mainTemporalAnalysis.dateStart);
    Date dateStop = sdf.parse(mainTemporalAnalysis.dateEnd);
    
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

	//write output of histogram count
	String histogramFileName = mainTemporalAnalysis.resourcesPathPart0 + "TweetHistogram.txt";
	//File tweetFile = new File(histogramFileName);
	//tweetFile.delete();
	PrintWriter pw = new PrintWriter(new FileWriter(histogramFileName, true));
    pw.println(weekSequence);
    pw.println(weeksPositive);
    pw.println(weeksNegative);
    pw.close();
}

 

}
