package angie;
import angie.mainTemporalAnalysis;
import twitter4j.JSONException;
import twitter4j.JSONObject;

import org.apache.lucene.analysis.it.ItalianAnalyzer;
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

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
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
	    // helper function reading content of politician file, line by line, removing twitter handle and saving to list
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

    public static HashMap<Integer, Long> prepareDaySequence() throws java.text.ParseException {
    	// creates a empty hashmap for histogram by day
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dateStart = sdf.parse(mainTemporalAnalysis.givenDateStart);
        Date dateStop = sdf.parse(mainTemporalAnalysis.givenDateEnd);
        // time operations - initiate holding sequence of days
    	Calendar sDateCalendar = new GregorianCalendar();
        sDateCalendar.setTime(dateStart);
    	int dayStart = sDateCalendar.get(Calendar.DAY_OF_YEAR);
        sDateCalendar.setTime(dateStop);
    	int dayEnd = sDateCalendar.get(Calendar.DAY_OF_YEAR);

    	HashMap<Integer, Long> daySequence = new HashMap<Integer, Long>();
    	// prefill with zeros to initiate counter
    	for (int i = dayStart; i <= dayEnd; i++) {
    		daySequence.put(i, 0L);
    	}
    	return daySequence;
    }

    public static HashMap<Integer, Long> updateHistogram(HashMap<Integer, Long> dateSequence, long dateTime) throws ParseException, java.text.ParseException{
    	// increment one to a histogram bucket where input datetime lies
    	Calendar sDateCalendar = new GregorianCalendar();
    	sDateCalendar.setTimeInMillis(dateTime);
    	int dayOfYear = sDateCalendar.get(Calendar.DAY_OF_YEAR);
    	if (dateSequence.get(dayOfYear) != null) {
    		dateSequence.put(dayOfYear, dateSequence.get(dayOfYear) + 1L);
    	}
    	return dateSequence; // data for histogram
    }


    private static Long parseTimeStringToLong (String dateTimeStr) throws java.text.ParseException {
    	// converts createdAt from tweet to long datetime
    	// given format is Sat Nov 26 14:29:46 +0000 2016
    	String[] dateArray = dateTimeStr.split(" ");
        String dateTimeCombined = dateArray[5] + "-" + dateArray[1] + "-" + dateArray[2] + " " + dateArray[3];
        SimpleDateFormat dateFormatPrint = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss", Locale.US);
        Long dateTime = (dateFormatPrint.parse(dateTimeCombined)).getTime();
        return dateTime;
    }
    
    public static IndexWriter createEmptyIndex(String directory, String sentiment) throws IOException {
    	// creates empty Lucene index in given directory
        ItalianAnalyzer analyzer = new ItalianAnalyzer(Version.LUCENE_41, ItalianAnalyzer.getDefaultStopSet());
        IndexWriterConfig cfg = new IndexWriterConfig(Version.LUCENE_41, analyzer);
        File indexFile = new File(mainTemporalAnalysis.resourcesPathPart0 + "index_tweets_" + sentiment);
        FSDirectory index = FSDirectory.open(indexFile);
        return new IndexWriter(index, cfg);
    }

    public static Document createLuceneDocument(String tweet, String user, String displayName, long id, String rtUser, long rtId, long dateTime) throws IOException, java.text.ParseException {
		// stores all relevant information about tweet to lucene index for further analysis
		Document document = new Document();
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
        document.add(new LongField("created_at", dateTime, Field.Store.YES));

        return document;
    }

    public static void histogramToFile (HashMap <Integer, Long> histogram, String sentiment) throws IOException {
    	// saves hashmap with histogram to a file, separated by ';' day by day
    	PrintWriter pw = new PrintWriter(new FileWriter(mainTemporalAnalysis.resourcesPathPart0 + "histogram_provided_dataset_" + sentiment + ".txt"));
      	StringJoiner joinerKeys = new StringJoiner(";");
      	StringJoiner joinerValues = new StringJoiner(";");
      	Object[] keys =  histogram.keySet().toArray();
      	Arrays.sort(keys);
    	 for (Object key : keys) {
    		 // write row like key;vals, separated by ;
    		 joinerKeys.add(key.toString());
    		 joinerValues.add(histogram.get(key).toString());
         }
      	pw.println(joinerKeys.toString());
      	pw.println(joinerValues.toString());
        pw.close();
    }
   
	public static void createIndexesOfTweets() throws IOException, ParseException, JSONException, java.text.ParseException {
		// parses given data from a folder to get relevant attributes to save to index
		// saves data to Lucene index and also creates a histogram and writes to a file
        File[] sourceDataFileList = new File(mainTemporalAnalysis.givenDataResourcesPath).listFiles();
        double progressCounter = 0.0;
        String tweet = "";
        BufferedReader bufferedReader;
        Document document;
        IndexWriter IndexWriter = createEmptyIndex("tweets_index/", "all");
        IndexWriter IndexWriterPositive = createEmptyIndex("tweets_index/", "positive");
        IndexWriter IndexWriterNegative = createEmptyIndex("tweets_index/", "negative");
        HashMap<Integer, Long> daySequencePositive = prepareDaySequence();
        HashMap<Integer, Long> daySequenceNegative = prepareDaySequence();

        for (File gzFile : sourceDataFileList) {
            System.out.println(progressCounter / sourceDataFileList.length * 100.0 + " % Done - Reading files from the full directory");
            progressCounter += 1.0;
            System.out.println("Reading " + gzFile.getName());
            bufferedReader = gzFileBufferedReader(gzFile.getPath());
            
            // read the file, line by line
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
                	// this is a retweet, save additional info
                    rtUser = (String) json.get("in_reply_to_screen_name");
                    rtId = Long.parseLong((String) json.get("in_reply_to_user_id_str"));
                }

                String tweetText = (String) json.get("text");
                long dateTime = parseTimeStringToLong(createdAt);
                document = createLuceneDocument(tweetText, user, displayName, id, rtUser, rtId, dateTime);
                IndexWriter.addDocument(document); // add the document to the global index
                
                List<String> politiciansPositive = loadPoliticians("TwitterPoliticiansPositiveFull");
                List<String> politiciansNegative = loadPoliticians("TwitterPoliticiansNegativeFull");
                // save to relevant lucene indexes if user or retweetuser is a positive or negative politician
                if (politiciansPositive.contains(user) || politiciansPositive.contains(rtUser)) {
                    IndexWriterPositive.addDocument(document); // add the document to the positive index
                    updateHistogram(daySequencePositive, dateTime);
                } else if (politiciansNegative.contains(user) || politiciansNegative.contains(rtUser)) {
                	// if a negative politician retweets a positive politician by "joke", this tweet is still considered negative
                    IndexWriterNegative.addDocument(document); // add the document to the positive index
                    updateHistogram(daySequenceNegative, dateTime);
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
        histogramToFile(daySequencePositive, "positive");
        histogramToFile(daySequenceNegative, "negative");

        System.out.println("Worker is done with his work!");
    }

	public static void mainIndex() throws JSONException, IOException, ParseException, java.text.ParseException {
		createIndexesOfTweets();
	}
	public static void main(String[] args) throws JSONException, IOException, ParseException, java.text.ParseException {
		mainIndex();
	}

}
