package angie;

import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

import java.io.*;
import java.util.*;

import twitter4j.*;



public class configbuilder {
	
	static void sleep(long ms) {
	    try { Thread.sleep(ms); }
	    catch(InterruptedException ex) { Thread.currentThread().interrupt(); }
	    }

	public static void main (String[] args) throws TwitterException, IOException{
		
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
		
		
		String[][] texts = new String[4][3];
		

		
		for (String al : positive_users) {
			ArrayList<Status> statuses = new ArrayList<Status>();
			int pageno = 1;
			//String path = "C:/ALookUkol/TwitterTweetData.txt";
			//File file = new File(path);
			while(true) {
			    try {
			        System.out.println("getting tweets...");
			        int size = statuses.size(); // actual tweets count we got
			        Paging page = new Paging(pageno, 200);
			        statuses.addAll(twitter.getUserTimeline(al, page));
			        System.out.println("total got : " + statuses.size());
			        if (statuses.size() == size) { break; } // we did not get new tweets so we have done the job
			        for (Status st : statuses) {
			        	System.out.println(st.getUser().getName()+" : " + st.getCreatedAt() +" : " +st.getText());
			        
			        	PrintWriter pw = new PrintWriter(new FileWriter("C:/ALookUkol/TwitterTweetData.txt", true));
		                String json = TwitterObjectFactory.getRawJSON(st);
		                System.out.println(json);
			        	pw.println(TwitterObjectFactory.getRawJSON(st.getUser().getName()+" : " + st.getCreatedAt() +" : " +st.getText()+ "\r\n"));
		                pw.close();
			        }
			        pageno++;
			        sleep(1000); // 900 rqt / 15 mn => 1 rqt/s //Every request we sleep for one second
			        }
			    
			    	
			    catch (TwitterException e) {
			        System.out.println(e.getErrorMessage());
			        }
			    }
		}
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

