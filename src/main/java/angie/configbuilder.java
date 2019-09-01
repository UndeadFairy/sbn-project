package angie;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import angie.mainTemporalAnalysis;


public class configbuilder {
	static void sleep(long ms) {
		// simple sleep function for download
	    try {
	    	Thread.sleep(ms); 
	    }
	    catch(InterruptedException ex) {
	    	Thread.currentThread().interrupt(); 
	    }
	}
	

    public static boolean testTime(Date inputDate) throws ParseException {
    	// checks if inputTime is in desired interval
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dateStart = sdf.parse(mainTemporalAnalysis.dateStart);
        Date dateStop = sdf.parse(mainTemporalAnalysis.dateEnd);
        if (dateStart.compareTo(inputDate) < 0) {
            if (dateStop.compareTo(inputDate) > 0) {
            	return true;
            }
        }
        return false;
    }


    public static ArrayList<String> verifyPolitician(ArrayList<String> inputList, String sentiment, Twitter twitter) throws ParseException, IOException {
    	// checks if politician has any tweets in given timeframe 1.4.2016 - 5.12.2016 because of the 3200 latest tweets limit
    	String tweetFileName = mainTemporalAnalysis.resourcesPathPart0 + "TwitterPoliticians" + sentiment + ".txt";
    	//File tweetFile = new File(tweetFileName);
		//tweetFile.delete();
		
    	ArrayList<String> passed = new ArrayList<String>();
    	for (String politician : inputList) {
			ArrayList<Status> statuses = new ArrayList<Status>();
			int pageno = 1;
	        int counter = 0;
			while(true) {
			    try {
			        System.out.println("getting tweets...");
			        int size = statuses.size(); // actual tweets count we got
			        // using paging attribute of twitter to work around the limits
			        Paging page = new Paging(pageno, 200);
			        statuses.addAll(twitter.getUserTimeline(politician, page));
			        System.out.println("total got : " + statuses.size());
			        // politician downloaded
			        if (statuses.size() == size) { break; }
			        for (Status st : statuses) {
			        	// test if any of tweets are in desired time interval
			        	Date createdAt = st.getCreatedAt();
			        	if (testTime(createdAt)) {
			        		counter +=1;
			        	}
			        }
			        pageno++;
			        sleep(1000); // 900 rqt / 15 mn => 1 rqt/s //After every request sleep for one second
			        }
			    catch (TwitterException e) {
			        System.out.println(e.getErrorMessage());
			        }
			    }
			if (counter > 0) {
				// save into selected politician list only if they have tweets in time interval
			    passed.add(politician);
			    PrintWriter pw = new PrintWriter(new FileWriter(tweetFileName, true));
			    // write to file
                pw.println(politician);
        		pw.close();
			}
		}
		return passed;
    }
    
    
    public static void downloadTweets (ArrayList<String> politicians, String sentiment, Twitter twitter) throws ParseException, IOException {
    	// will contain only relevant data
    	String tweetFileName = mainTemporalAnalysis.resourcesPathPart0 + "TwitterTweetData" + sentiment + ".txt";
    	// will contain all tweet information for backup purposes
    	String fullTweetFileName = mainTemporalAnalysis.resourcesPathPart0 + "TwitterTweetDataFull" + sentiment + ".txt";

		//File tweetFile = new File(tweetFileName);
		//tweetFile.delete();
		//File tweetFileFull = new File(fullTweetFileName);
		//tweetFileFull.delete();
		
		PrintWriter pw = new PrintWriter(new FileWriter(tweetFileName, true));
		PrintWriter pwFull = new PrintWriter(new FileWriter(fullTweetFileName, true));
		// write header of file
        pw.println("tweet_id" + ";" + "screen_name" + ";" + "user_name" + ";" + "favorite_count" + ";" + "created_at" + ";" + "retweet_count" + ";" + "retweeted_status_user"+ ";" + "retweeted_status_id" + ";" + "tweet_text");

		for (String politician : politicians) {
			// same logic as for verification
			// downloads tweets for preselected users
			ArrayList<Status> statuses = new ArrayList<Status>();
			int pageno = 1;
			while(true) {
			    try {
			        System.out.println("getting tweets...");
			        int size = statuses.size(); // actual tweets count we got
			        Paging page = new Paging(pageno, 200);
			        statuses.addAll(twitter.getUserTimeline(politician, page));
			        System.out.println("total got : " + statuses.size());
			        if (statuses.size() == size) { break; }
			        for (Status st : statuses) {
			        	Date createdAt = st.getCreatedAt();
			        	if (testTime(createdAt)) {
			        		long tweetId = st.getId();
			        		Status retweetedStatus = st.getRetweetedStatus();
			        		long retweetedStatusId = 0;
			        		String retweetedStatusUser = "foo";
			        		if (retweetedStatus != null) {
			        			retweetedStatusId = retweetedStatus.getId();
			        			retweetedStatusUser = retweetedStatus.getUser().getName();
			        		}
			        		String userName = st.getUser().getName();
			        		int favoriteCount = st.getFavoriteCount();
				        	SimpleDateFormat dateFormatPrint = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				        	String createdAtStr = dateFormatPrint.format(createdAt);
			        		int retweetCount = st.getRetweetCount();
			        		String tweetText = st.getText().replace("\n", " ");
			        		// write to text file
			                pw.println(tweetId + ";" + politician + ";" + userName + ";" + favoriteCount + ";" + createdAtStr + ";" + retweetCount + ";" + retweetedStatusUser + ";" + retweetedStatusId + ";" + tweetText);
				        	pwFull.println(TwitterObjectFactory.getRawJSON(st));
			        	}
			        }
			        pageno++;
			        sleep(1000);
			        }
			    catch (TwitterException e) {
			        System.out.println(e.getErrorMessage());
			    }
			}
		}
		pw.close();
		pwFull.close();
    }

    public static ArrayList<String> loadPoliticians(String loadname) throws IOException {   
    	String tweetFileName = mainTemporalAnalysis.resourcesPathPart0 + loadname + ".txt";
    	Scanner s = new Scanner(new File(tweetFileName));
	    ArrayList<String> list = new ArrayList<String>();
	    while (s.hasNext()){
	        list.add(s.next());
	    }
	    s.close();
	    return list;
    }
    
    
	public static void mainConfigbuilder() throws TwitterException, IOException, ParseException{
		ConfigurationBuilder cfg = new ConfigurationBuilder();
		cfg.setJSONStoreEnabled(true);
		cfg.setOAuthAccessToken("");
		cfg.setOAuthAccessTokenSecret("");
		cfg.setOAuthConsumerKey("");
		cfg.setOAuthConsumerSecret("");
		cfg.setTweetModeExtended(true);
		
		TwitterFactory tf = new TwitterFactory(cfg.build());
		Twitter twitter = tf.getInstance();
		ArrayList<String> positive_users = loadPoliticians("TwitterPoliticiansPositiveFull");
		ArrayList<String> negative_users = loadPoliticians("TwitterPoliticiansNegativeFull");
        // can be cast to void, users are saved to file
		ArrayList<String> positive_passed = verifyPolitician(positive_users, "positive", twitter);
		ArrayList<String> negative_passed = verifyPolitician(negative_users, "negative", twitter);
		
		ArrayList<String> positive_passed_loaded = loadPoliticians("TwitterPoliticianspositive");
		ArrayList<String> negative_passed_loaded = loadPoliticians("TwitterPoliticiansnegative");
		
		downloadTweets(positive_passed_loaded, "positive", twitter);
		downloadTweets(negative_passed_loaded, "negative", twitter);
	}
	public static void main(String[] args) throws TwitterException, IOException, ParseException {
		mainConfigbuilder();
	}
}

