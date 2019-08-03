package angie;
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

import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;


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
        ItalianAnalyzer analyzer = new ItalianAnalyzer(Version.LUCENE_41, ItalianAnalyzer.getDefaultStopSet());
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
		        	String raw_tweet_text = record[8].replaceAll("(RT @[a-zA-Z0-9-_]{1,}:)", " ");
			        //System.out.println(raw_tweet_text);

		        	String modified_tweet_text = raw_tweet_text.replaceAll("https", " ").replaceAll("http", " ").replaceAll("[^\\p{L}\\s]", "").toLowerCase();
			        //System.out.println(modified_tweet_text);

		        	//if (modified_tweet_text.contains("referendum")) {
		        		document = toLuceneDocument(modified_tweet_text, screen_name, displayName, tweetId, retweeted_status_user, retweeted_status_id, created_at);
	                    IndexWriter.addDocument(document); // add the document to the index	
		        	//}
		        	
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
	

	
    public static ArrayList<String> fromTermStatsToList(TermStats[] termsStats) {
        ArrayList<String> termsList = new ArrayList<String>();

        for (TermStats term : termsStats) {
            termsList.add(term.termtext.utf8ToString());
        }

        return termsList;
    }
    


    // return a list containing the N most frequent terms in the dictionary located in indexDirectory
    private static ArrayList<String> topNTerms (int N, String sentiment) throws IOException, Exception {
    	String dirName = "src/main/resources/index_tweets_" + sentiment;
        File indexFile = new File(dirName);
        FSDirectory indexFSDirectory = FSDirectory.open(indexFile);
        IndexReader indexReader = DirectoryReader.open(indexFSDirectory);
        TermStats[] termStats = HighFreqTerms.getHighFreqTerms(indexReader, N, "tweet");
        
        ArrayList<String> termsStatsList = fromTermStatsToList(termStats);
        return termsStatsList;
    }
 
    

   


private static HashMap<String, double[]> termsTimeSeries( ArrayList<String> termsStatsList, String sentiment, long grain) throws IOException, Exception {
    //initialize the vector of frequencies inside the hashmap for all terms. The index of this vector is define as [minDate, minDate+interval, minDate+2*interval,...]
    HashMap<String, double[]> termsFrequencies = new HashMap<String, double[]>();
    long startDate = 1459468800000L;
    long endDate = 1480896000000L;
    
    // to 
    int numberOfIntervals = (int) Math.ceil((endDate - startDate) / grain) + 1;
    for (int i = 0; i<termsStatsList.size();i++) {
        termsFrequencies.put(termsStatsList.get(i), new double[numberOfIntervals]);

    } 
    
    /*
    iterate through all documents, getting all terms. If the term is one of the topN, then increment the 
    frequency of the term in that interval. Each interval (usually 12h) is one position of the vector 'vecFrequencies'.
     */
    String dirName = "src/main/resources/index_tweets_" + sentiment;
    File indexFile = new File(dirName);
    FSDirectory indexFSDirectory = FSDirectory.open(indexFile);
    IndexReader indexReader = DirectoryReader.open(indexFSDirectory);
    Document doc;
    ItalianAnalyzer analyzer = new ItalianAnalyzer(Version.LUCENE_41, ItalianAnalyzer.getDefaultStopSet());
    LinkedHashSet terms;
    int vectorIndex;
    Long tweetDate;
    double[] vecFrequencies; //the index of this vector is define as [minDate, minDate+interval, minDate+2*interval,...]
    for (int i = 0; i < indexReader.maxDoc(); i++) {
        doc = indexReader.document(i);
        tweetDate = Long.parseLong(doc.get("created_at"));
        vectorIndex = (int) Math.floor((tweetDate - startDate) / grain); //define the position of the timeseries vector
        // iterate over all the words of the tweet. The words are text-processed
        String tweet = doc.get("tweet");
        ArrayList<String> tokenized = tokenizeString(analyzer,tweet);
        for(int j = 0; j < tokenized.size(); j++) {
        	String term = tokenized.get(j);
    		if (termsFrequencies.containsKey(term)) {
                // update the frequency in the timeseries of the term
                vecFrequencies = termsFrequencies.get(term);
                vecFrequencies[vectorIndex]++;
                termsFrequencies.put(term, vecFrequencies);
            }
        }
    }
    return termsFrequencies;
}

public static ArrayList<String> tokenizeString(ItalianAnalyzer analyzer, String string) {
    ArrayList<String> result = new ArrayList<String>();
    try {
      TokenStream stream  = analyzer.tokenStream(null, new StringReader(string));
      stream.reset();
      while (stream.incrementToken()) {
        result.add(stream.getAttribute(CharTermAttribute.class).toString());
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return result;
  }


   // private final int alphabetSize;
   // private final double nThreshold;
   // private final Alphabet na;

    // from a timeseries of the word frequencies, create a SAX string that represents it
    private static String buildSAX(double[] timeSeries, int alphabetSize, double nThreshold, Alphabet alphabet) throws SAXException {
    	SAXProcessor sp = new SAXProcessor();
        SAXRecords res = sp.ts2saxByChunking(timeSeries, timeSeries.length, alphabet.getCuts(alphabetSize), nThreshold);
        System.out.println(res.getSAXString(""));
        return res.getSAXString("");
    }

private static HashMap<String, String> transformTimeSeriesToSAX( HashMap<String, double[]> termsFrequencies, int alphabetSize) throws SAXException {
    HashMap<String, String> termSAX = new HashMap<String, String>();

    // normalize the vector of frequencies, in this way there will be a better comparison
    // between 2 different SAX strings
    TreeSet allFrequencies = new TreeSet();
    for (Entry<String, double[]> ee : termsFrequencies.entrySet()) {
        double[] values = ee.getValue();
        for (int i=0;i<values.length;i++) {
        	allFrequencies.add(values[i]);
        }
    }
    double maxFreq = Double.parseDouble(allFrequencies.last().toString());
    for (Entry<String, double[]> ee : termsFrequencies.entrySet()) {
        double[] values = ee.getValue();
        for (int i=0;i<values.length;i++) {
        	values[i] = values[i] / maxFreq;
        }
    }
    // divide all elements by the maxFreq, normalizing, then, between 0 and 1
   
    for (Entry<String, double[]> ee : termsFrequencies.entrySet()) {
        String tweet = ee.getKey();
        String sax;
	    try {
	        //SAXBuilder saxUtils = new SAXBuilder(alphabetSize, Math.round(alphabetSize / maxFreq), new NormalAlphabet());
	        sax = buildSAX(termsFrequencies.get(tweet), alphabetSize, Math.round(alphabetSize / maxFreq), new NormalAlphabet());
	        termSAX.put(tweet, sax);
	    } catch (SAXException ex) {
	        System.out.println(ex.getMessage());
	    }
    }
    return termSAX;
}

private static void writeClusters(final HashMap<String, Integer> clusters, String sentiment) throws IOException {
    final PrintWriter pw = new PrintWriter(new FileWriter("src/main/resources/clusters_" + sentiment + ".txt"));

    for (Entry<String, Integer> cluster : clusters.entrySet()) {
        String key = cluster.getKey();
        pw.println(key + " " + clusters.get(key));
    }

    pw.close();
}

private static HashMap<String, Integer> clusterMapping(HashMap<String, Integer> saxClusters, HashMap<String, String> termSAX) {
    HashMap<String, Integer> termClusters = new HashMap<>();
    String sax;

    // iterate through all the terms, assigning to them the correspondent cluster
    // of their SAX string
    for (String term : termSAX.keySet()) {
        sax = termSAX.get(term);
        termClusters.put(term, saxClusters.get(sax));
    }

    return termClusters;
}

public static void main (String[] args) throws Exception{
        //createIndexAllTweets("positive");
        //createIndexAllTweets("negative");
	    //createIndexAllTweets("all");
	    
	    
	    ArrayList<String> nTermsPositive = topNTerms(1000, "positive");
	    ArrayList<String> nTermsNegative = topNTerms(1000, "negative");
	    ArrayList<String> nTermsAll = topNTerms(1000, "all");
	    
	    HashMap<String, double[]> termTimeSeriesPositive = termsTimeSeries(nTermsPositive, "positive", 43200000L);
	    HashMap<String, double[]> termTimeSeriesNegative = termsTimeSeries(nTermsNegative, "negative", 43200000L);

        int alphabetSize = 20;
        int k = 4;
        HashMap<String, String> termSAXPositive = transformTimeSeriesToSAX(termTimeSeriesPositive, alphabetSize);
        HashMap<String, String> termSAXNegative = transformTimeSeriesToSAX(termTimeSeriesNegative, alphabetSize);
        System.out.println("all before done");

        KMeansAlgorithm KMeansPositive = new KMeansAlgorithm(k, alphabetSize, new ArrayList<>(termSAXPositive.values()));
        HashMap<String, Integer> positiveSAXClusters = KMeansPositive.getClusters();
        System.out.println("positive getclusters done");

        KMeansAlgorithm KMeansNegative = new KMeansAlgorithm(k, alphabetSize, new ArrayList<>(termSAXNegative.values()));
        HashMap<String, Integer> negativeSAXClusters = KMeansNegative.getClusters();
        System.out.println("negative getclusters done");
        // from the SAX strings, get back to the terms, generating the clusters
        // for the terms, not the SAX that represent the term frequency vector
        HashMap<String, Integer> clustersPositive = clusterMapping(positiveSAXClusters, termSAXPositive);
        HashMap<String, Integer> clustersNegative = clusterMapping(negativeSAXClusters, termSAXNegative);
        System.out.println("mapping done");

        writeClusters(clustersPositive, "positive");
        writeClusters(clustersNegative,  "negative");
        System.out.println("write done");

	    
	}
	
}
