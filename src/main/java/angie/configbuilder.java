package angie;

import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import twitter4j.*;



public class configbuilder {
	
	static void sleep(long ms) {
	    try { Thread.sleep(ms); }
	    catch(InterruptedException ex) { Thread.currentThread().interrupt(); }
	    }
	

    public static boolean testTime(Date inputDate) throws ParseException {
    	// checks if inputTime is in desired interval
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dateStart = sdf.parse("2016-04-01");
        Date dateStop = sdf.parse("2016-12-05");

        if (dateStart.compareTo(inputDate) < 0) {
            //System.out.println("Date1 is after Date2");
            if (dateStop.compareTo(inputDate) > 0) {
            	return true;
            }
        }
        return false;
    }
    
    public static void downloadTweets (ArrayList<String> politicians, String sentiment, Twitter twitter) throws ParseException, IOException {
    	String tweetFileName = "src/main/resources/TwitterTweetData" + sentiment + ".txt";
		File tweetFile = new File(tweetFileName);
		tweetFile.delete();

		
		for (String al : politicians) {
			ArrayList<Status> statuses = new ArrayList<Status>();
			int pageno = 1;
			while(true) {
			    try {
			        System.out.println("getting tweets...");
			        int size = statuses.size(); // actual tweets count we got
			        Paging page = new Paging(pageno, 200);
			        statuses.addAll(twitter.getUserTimeline(al, page));
			        System.out.println("total got : " + statuses.size());
			        if (statuses.size() == size) { break; } // we did not get new tweets so we have done the job
			        for (Status st : statuses) {
			        	Date createdAt = st.getCreatedAt();
			        	if (testTime(createdAt)) {
				        	System.out.println(st.getUser().getName()+" : " + st.getCreatedAt() +" : " +st.getText());
			        		PrintWriter pw = new PrintWriter(new FileWriter(tweetFileName, true));
			                //String json = TwitterObjectFactory.getRawJSON(st);
			                //System.out.println(json);
			        		String userName = st.getUser().getName();
			        		String tweetText = st.getText();
				        	//pw.println(TwitterObjectFactory.getRawJSON(st.getUser().getName()+" : " + st.getCreatedAt() +" : " +st.getText()+ "\r\n"));
			                pw.println(userName +";" + createdAt.toString() + ";" + tweetText);
			        		pw.close();
			        	}
			        }
			        pageno++;
			        sleep(1000); // 900 rqt / 15 mn => 1 rqt/s //Every request we sleep for one second
			        }
			    
			    	
			    catch (TwitterException e) {
			        System.out.println(e.getErrorMessage());
			        }
			    }
		}
    }

	public static void main (String[] args) throws TwitterException, IOException, ParseException{
		
		ConfigurationBuilder cfg= new ConfigurationBuilder(); 
		cfg.setJSONStoreEnabled(true);
		cfg.setOAuthAccessToken("792417527259402240-v49BmlmHUXWGSYMze0k3Hgdls3PzIAY");
		cfg.setOAuthAccessTokenSecret("dOYrb91H8AY97awtDyNdkvJHUOB5GnkhcZxJnF1ACC60l");
		cfg.setOAuthConsumerKey("GVWdMmKZ2yxJHvaOVXQXabIKq");
		cfg.setOAuthConsumerSecret("gZnSFV2du4JHcY7T35FRi4HYM4AZagNjGsVaRaxzTmo6ETC8hF");
		
		TwitterFactory tf = new TwitterFactory(cfg.build());
		Twitter twitter = tf.getInstance();
		
		ArrayList<String> positive_users = new ArrayList<String>(
			    Arrays.asList("@matteorenzi", "@angealfa", "@DenisVerdini", "@enrico_zanetti"));
		
		ArrayList<String> negative_users = new ArrayList<String>(
			    Arrays.asList("@beppe_grillo", "@berlusconi", "@NFratoianni ", "@matteosalvinimi", "@GiorgiaMeloni", "@RaffaeleFitto"));
		
		
//		String[][] texts = new String[4][3];

		downloadTweets(positive_users, "positive", twitter);
		downloadTweets(negative_users, "negative", twitter);

		
	}}// while(true)
		
		/*
		for (String al : positive_users) {
		
			Paging page = new Paging (1,200);	
				
			List<Status> status = twitter.getUserTimeline(al,page);
			for (Status st : status) {
				System.out.println(st.getUser().getName()+"......."+st.getText());
			}
		}
		*/
	
/*	
	for (String al : positive_users) {
		
		
		Paging page = new Paging (1, 100);	
		List<Status> status = twitter.getUserTimeline(al, page);
		for (Status st : status) {
			
			System.out.println(st.getUser().getName()+"......."+st.getText());
			String text = st.getUser().getName()+st.getText();
			try {

                PrintWriter pw = new PrintWriter(new FileWriter("TwitterTweetData.txt", true));
                pw.println(TwitterObjectFactory.getRawJSON(st));
                pw.close();	
              } catch ( IOException e ) {
                 e.printStackTrace();
              }

		}
		}
	}
	
}*/

