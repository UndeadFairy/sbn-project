package angie;
import angie.mainTemporalAnalysis;
import twitter4j.JSONException;
import twitter4j.JSONObject;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
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

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.TermStats;

import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.nio.charset.StandardCharsets;

public class Index {
	
	 public static String getData(String content) {
		String stringToMatchBegin = "[CDATA[";
		String stringToMatchEnd = "]]></p></o>";
		// get string index of start of what is to be extracted
		int dataBegin = content.indexOf(stringToMatchBegin) + stringToMatchBegin.length();
		// get string index of end of what is to be extracted
		int dataEnd = content.indexOf(stringToMatchEnd);
		return content.substring(dataBegin, dataEnd);
	}

    public static JSONObject readJSON(String xml) throws JSONException {
    	// tweet infos is json, parse it
        JSONObject json = new JSONObject(getData(xml));
        return json;
    }
    
    public static List<String> loadPoliticians(String filename) throws IOException {   
	    // helper function reading content of politician file, line by line, saving to list
    	List<String> politicians = new ArrayList<String>();
    	BufferedReader reader;
    	try {
			reader = new BufferedReader(new FileReader(mainTemporalAnalysis.resourcesPathPart0 + filename + ".txt"));
			String line = reader.readLine();
			while (line != null) {
				// clear @ handle
				politicians.add(line.replace("@", ""));
				// read next line
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return politicians;
    }
    
    public static BufferedReader gzFileBufferedReader(String file) throws IOException {
        // open the input as stream
        FileInputStream fileInputStream = new FileInputStream(file);
        // open the gzip and get through decompression
        GZIPInputStream gZipInputStream = new GZIPInputStream(fileInputStream);
        Reader standardReader = new InputStreamReader(gZipInputStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(standardReader);
        return bufferedReader;
    }
    
    public static final CharArraySet stopwords = CharArraySet.copy(Version.LUCENE_41, ItalianAnalyzer.getDefaultStopSet());
	
    public static Document createLuceneDocument(String tweet, String user, String displayName, long id, String rtUser, long rtId, String dateTimeStr) throws IOException, java.text.ParseException {
		// stores all relevant information about tweet to lucene index for further analysis
		Document document = new Document();
    	String[] dateArray = dateTimeStr.split(" ");
        String dateTimeCombined = dateArray[5] + "-" + dateArray[1] + "-" + dateArray[2] + " " + dateArray[3];
        SimpleDateFormat dateFormatPrint = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss", Locale.US);

    	// basic tweet text cleanup
    	String raw_tweet_text = tweet.replaceAll("(RT @[a-zA-Z0-9-_]{1,}:)", " "); 
    	// further tweet text cleanup
    	String modified_tweet_text = raw_tweet_text.replaceAll("https", " ").replaceAll("http", " ").replaceAll("[^\\p{L}\\s]", "").toLowerCase();

        document.add(new TextField("tweet", modified_tweet_text, Field.Store.YES));
        document.add(new StringField("user", user, Field.Store.YES));
        document.add(new StringField("displayName", displayName, Field.Store.YES));
        document.add(new LongField("id", id, Field.Store.YES));
        document.add(new StringField("rtUser", rtUser, Field.Store.YES));
        document.add(new LongField("rtId", rtId, Field.Store.YES));
        document.add(new LongField("created_at", (dateFormatPrint.parse(dateTimeCombined)).getTime(), Field.Store.YES));

        return document;
    }

    public static IndexWriter createEmptyIndex(String directory, String sentiment) throws IOException {
        ItalianAnalyzer analyzer = new ItalianAnalyzer(Version.LUCENE_41, ItalianAnalyzer.getDefaultStopSet());
        IndexWriterConfig cfg = new IndexWriterConfig(Version.LUCENE_41, analyzer);
        File indexFile = new File(mainTemporalAnalysis.resourcesPathPart0 + "index_tweets_" + sentiment);
        FSDirectory index = FSDirectory.open(indexFile);
        return new IndexWriter(index, cfg);
    }
   
	public static void createIndexesOfTweets() throws IOException, ParseException, JSONException, java.text.ParseException {
        //String tweetFileName = mainTemporalAnalysis.resourcesPathPart0 + "TwitterTweetData" + sentiment + ".txt";
        File[] sourceDataFileList = new File(mainTemporalAnalysis.givenDataResourcesPath).listFiles();
        double counter = 0.0;
        String tweet = "";
        BufferedReader bufferedReader;
        Document document;
        IndexWriter IndexWriter = createEmptyIndex("tweets_index/", "all");
        IndexWriter IndexWriterPositive = createEmptyIndex("tweets_index/", "positive");
        IndexWriter IndexWriterNegative = createEmptyIndex("tweets_index/", "negative");

        for (File gzFile : sourceDataFileList) {
            System.out.println(counter / sourceDataFileList.length * 100.0 + " % Done - Reading files from the full directory");
            counter += 1.0;
            System.out.println("Reading " + gzFile.getName());
            bufferedReader = gzFileBufferedReader(gzFile.getPath());
            
            //read the file, line by line
            while ((tweet = bufferedReader.readLine()) != null) {
            	// get json with desired information
                JSONObject json = readJSON(tweet);
                String displayName = (String) ((JSONObject) json.get("user")).get("name");
                String user = (String) ((JSONObject) json.get("user")).get("screen_name");
                String rtUser = "";
                long id = Long.parseLong((String) ((JSONObject) json.get("user")).get("id_str"));
                long rtId = 0L;
                String createdAt = (String) (json.get("created_at"));
                if (!json.isNull("in_reply_to_screen_name")) {
                    rtUser = (String) json.get("in_reply_to_screen_name");
                    rtId = Long.parseLong((String) json.get("in_reply_to_user_id_str"));
                }

                String tweetText = (String) json.get("text");
                document = createLuceneDocument(tweetText, user, displayName, id, rtUser, rtId, createdAt);
                IndexWriter.addDocument(document); // add the document to the global index
                
                List<String> politiciansPositive = loadPoliticians("TwitterPoliticiansPositiveFull");
                List<String> politiciansNegative = loadPoliticians("TwitterPoliticiansNegativeFull");
                // save to relevant lucene indexes user or retweetuser is a positive or negative politician
                if (politiciansPositive.contains(user) || politiciansPositive.contains(rtUser)) {
                    IndexWriterPositive.addDocument(document); // add the document to the positive index
                } else if (politiciansNegative.contains(user) || politiciansNegative.contains(rtUser)) {
                	// if a negative politician retweets a positive politician by "joke", this tweet is still considered negative
                    IndexWriterNegative.addDocument(document); // add the document to the positive index
                }
            }
            // write changes to index after each file to avoid bloating
            IndexWriter.commit();
            IndexWriterPositive.commit();
            IndexWriterNegative.commit();
        }
        IndexWriter.close();
        IndexWriterPositive.close();
        IndexWriterNegative.close();
        System.out.println("Peon is done with his work");

//        
//        IndexWriter IndexWriter = createEmptyIndex("all_tweets_index/", sentiment);
//		try {
//	        FileReader filereader = new FileReader(tweetFileName);
//	        CSVParser parser =
//	        		new CSVParserBuilder()
//	        		.withSeparator(';')
//	        		.withIgnoreQuotations(true)
//	        		.build();
//	        		final CSVReader reader =
//	        		new CSVReaderBuilder(filereader)
//	        		.withSkipLines(1)
//	        		.withCSVParser(parser)
//	        		.build();
//	        String[] record; 
//	        Document document;
//	        int counter = 0;
//	        while ((record = reader.readNext()) != null) {
//	        	if (record.length == 9) {
//	        		// only lines with correct number of elements considered
//	        		counter++;
//	        		long tweetId = Long.parseLong(record[0]);
//		        	String screen_name = record[1]; //@name
//		        	String displayName = record[2];   // name surname
//		        	int favorite_count = Integer.parseInt(record[3]); // in the end was not used for analysis
//		        	String created_at = record[4];
//		        	int retweet_count = Integer.parseInt(record[5]); // in the end was not used for analysis
//		        	String retweeted_status_user = record[6];
//		        	long retweeted_status_id = Long.parseLong(record[7]);
//		        	// basic tweet text cleanup
//		        	String raw_tweet_text = record[8].replaceAll("(RT @[a-zA-Z0-9-_]{1,}:)", " "); 
//		        	// further tweet text cleanup
//		        	String modified_tweet_text = raw_tweet_text.replaceAll("https", " ").replaceAll("http", " ").replaceAll("[^\\p{L}\\s]", "").toLowerCase();
//
//		        	//if (modified_tweet_text.contains("referendum")) {
//		        		document = toLuceneDocument(modified_tweet_text, screen_name, displayName, tweetId, retweeted_status_user, retweeted_status_id, created_at);
//	                    IndexWriter.addDocument(document); // add the document to the index	
//		        	//}
//		        	
//	        	}
//	        }
//	        System.out.println(counter);
//	        reader.close();
//	        IndexWriter.commit(); // write the index to the file opened to "store" the index
//	        IndexWriter.close();
//	    } 
//	    catch (Exception e) { 
//	        e.printStackTrace(); 
//	    }
    }

	public static void main(String[] args) throws JSONException, IOException, ParseException, java.text.ParseException {
		createIndexesOfTweets();
//		String pathie = mainTemporalAnalysis.resourcesPathPart0;
	}

}
