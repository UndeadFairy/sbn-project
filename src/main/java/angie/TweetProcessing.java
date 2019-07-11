package angie;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVParser;


import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


public class TweetProcessing {
    public static final CharArraySet stopwords = CharArraySet.copy(Version.LUCENE_41, ItalianAnalyzer.getDefaultStopSet());
	
	public static Document toLuceneDocument(String tweet, String user, String displayName, long id, String rtUser, long rtId, String dateTimeStr) throws IOException, ParseException, java.text.ParseException {
        Document document = new Document();
        document.add(new TextField("tweet", tweet, Field.Store.YES));
        document.add(new StringField("user", user, Field.Store.YES));
        document.add(new StringField("displayName", displayName, Field.Store.YES));
        document.add(new LongField("id", id, Field.Store.YES));
        document.add(new StringField("rtUser", rtUser, Field.Store.YES));
        document.add(new LongField("rtId", rtId, Field.Store.YES));
    	SimpleDateFormat dateFormatPrint = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        document.add(new LongField("created_at", (dateFormatPrint.parse(dateTimeStr)).getTime(), Field.Store.YES)); //convert the date to Long
        return document;
    }

    public static IndexWriter createEmptyIndex(String directory, String sentiment) throws IOException {
        ItalianAnalyzer analyzer = new ItalianAnalyzer(Version.LUCENE_41, stopwords);
        IndexWriterConfig cfg = new IndexWriterConfig(Version.LUCENE_41, analyzer);
        File indexFile = new File("src/main/resources/" + "index_tweets_" + sentiment);
        FSDirectory index = FSDirectory.open(indexFile);
        return new IndexWriter(index, cfg);
    }
   
	public static void createIndexAllTweets(String sentiment) throws IOException, ParseException {
        String tweetFileName = "src/main/resources/TwitterTweetData" + sentiment + ".txt";
        IndexWriter IndexWriter = createEmptyIndex("all_tweets_index/", sentiment);
		try { 
	        FileReader filereader = new FileReader(tweetFileName);
	        CSVParser parser =
	        		new CSVParserBuilder()
	        		.withSeparator(';')
	        		.withIgnoreQuotations(true)
	        		.build();
	        		final CSVReader reader =
	        		new CSVReaderBuilder(filereader)
	        		.withSkipLines(1)
	        		.withCSVParser(parser)
	        		.build();
	        String[] record; 
	        Document document;
	        int counter = 0;
	        while ((record = reader.readNext()) != null) { 
	        	if (record.length == 9) {
	        		counter++;
	    	        //System.out.println(record[0]);
	        		long tweetId = Long.parseLong(record[0]);
		        	String screen_name = record[1]; //@name
		        	String displayName = record[2];   // name surname
		        	int favorite_count = Integer.parseInt(record[3]);
		        	String created_at = record[4];
		        	int retweet_count = Integer.parseInt(record[5]);
		        	String retweeted_status_user = record[6];
		        	long retweeted_status_id = Long.parseLong(record[7]);
		        	String tweet_text = record[8].replaceAll("[^\\p{L}\\s]", "").toLowerCase();
                    document = toLuceneDocument(tweet_text, screen_name, displayName, tweetId, retweeted_status_user, retweeted_status_id, created_at);
                    IndexWriter.addDocument(document); // add the document to the index
	        	}
	        }
	        System.out.println(counter);
	        reader.close();
	        IndexWriter.commit(); // write the index to the file opened to "store" the index
	        IndexWriter.close();
	    } 
	    catch (Exception e) { 
	        e.printStackTrace(); 
	    }

    }
public static void main (String[] args) throws IOException, ParseException{
        createIndexAllTweets("positive");
        createIndexAllTweets("negative");
	    createIndexAllTweets("all");
	}
	
}
