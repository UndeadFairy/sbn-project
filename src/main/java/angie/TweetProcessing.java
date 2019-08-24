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
import angie.mainTemporalAnalysis;

public class TweetProcessing {
    public static final CharArraySet stopwords = CharArraySet.copy(Version.LUCENE_41, ItalianAnalyzer.getDefaultStopSet());
	public static Document createLuceneDocument(String tweet, String user, String displayName, long id, String rtUser, long rtId, String dateTimeStr) throws IOException, ParseException, java.text.ParseException {
		// stores all relevant information about tweet to lucene index for further analysis
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
        File indexFile = new File(mainTemporalAnalysis.resourcesPathPart0 + "index_tweets_" + sentiment);
        FSDirectory index = FSDirectory.open(indexFile);
        return new IndexWriter(index, cfg);
    }
   
	public static void createIndexAllTweets(String sentiment) throws IOException, ParseException {
        String tweetFileName = mainTemporalAnalysis.resourcesPathPart0 + "TwitterTweetData" + sentiment + ".txt";
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
	        		// only lines with correct number of elements considered
	        		counter++;
	        		long tweetId = Long.parseLong(record[0]);
		        	String screen_name = record[1]; //@name
		        	String displayName = record[2];   // name surname
		        	int favorite_count = Integer.parseInt(record[3]); // in the end was not used for analysis
		        	String created_at = record[4];
		        	int retweet_count = Integer.parseInt(record[5]); // in the end was not used for analysis
		        	String retweeted_status_user = record[6];
		        	long retweeted_status_id = Long.parseLong(record[7]);
		        	// basic tweet text cleanup
		        	String raw_tweet_text = record[8].replaceAll("(RT @[a-zA-Z0-9-_]{1,}:)", " "); 
		        	// further tweet text cleanup
		        	String modified_tweet_text = raw_tweet_text.replaceAll("https", " ").replaceAll("http", " ").replaceAll("[^\\p{L}\\s]", "").toLowerCase();

		        	//if (modified_tweet_text.contains("referendum")) {
		        		document = createLuceneDocument(modified_tweet_text, screen_name, displayName, tweetId, retweeted_status_user, retweeted_status_id, created_at);
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
    	// helper function to handle lucene termstats results and return them as strings
        ArrayList<String> termsList = new ArrayList<String>();

        for (TermStats term : termsStats) {
            termsList.add(term.termtext.utf8ToString());
        }
        return termsList;
    }
    

    private static ArrayList<String> topNTerms (int N, String sentiment) throws IOException, Exception {
        // returns a list containing the N most frequent terms in the dictionary located in indexDirectory
    	String dirName = mainTemporalAnalysis.resourcesPathPart0 + "index_tweets_" + sentiment;
        File indexFile = new File(dirName);
        FSDirectory indexFSDirectory = FSDirectory.open(indexFile);
        IndexReader indexReader = DirectoryReader.open(indexFSDirectory);
        TermStats[] termStats = HighFreqTerms.getHighFreqTerms(indexReader, N, "tweet");
        
        ArrayList<String> termsStatsList = fromTermStatsToList(termStats);
        return termsStatsList;
    }


	public static HashMap<String, double[]> termsTimeSeries(ArrayList<String> termsStatsList, String sentiment, long grain) throws IOException, Exception {
	    // create vector of frequencies inside the hashmap for all terms
	    HashMap<String, double[]> termsFrequencies = new HashMap<String, double[]>();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	    long startDate = sdf.parse(mainTemporalAnalysis.givenDateStart).getTime();
        long endDate = sdf.parse(mainTemporalAnalysis.givenDateEnd).getTime();
	    // count of intervals
	    int numberOfIntervals = (int) Math.ceil((endDate - startDate) / grain) + 1;
	    for (int i = 0; i<termsStatsList.size();i++) {
	        termsFrequencies.put(termsStatsList.get(i), new double[numberOfIntervals]);
	    } 
	    
	    String dirName = mainTemporalAnalysis.resourcesPathPart0 + "index_tweets_" + sentiment;
	    File indexFile = new File(dirName);
	    FSDirectory indexFSDirectory = FSDirectory.open(indexFile);
	    IndexReader indexReader = DirectoryReader.open(indexFSDirectory);
	    Document doc;
	    ItalianAnalyzer analyzer = new ItalianAnalyzer(Version.LUCENE_41, ItalianAnalyzer.getDefaultStopSet());
	    int vectorIndex;
	    Long tweetDate;
	    double[] frequencies;
	    // iterate through all documents, then increase the frequency of the term for topN
	    for (int i = 0; i < indexReader.maxDoc(); i++) {
	        doc = indexReader.document(i);
	        tweetDate = Long.parseLong(doc.get("created_at"));
	        vectorIndex = (int) Math.floor((tweetDate - startDate) / grain);
	        String tweet = doc.get("tweet");
	        ArrayList<String> tokenized = tokenizeString(analyzer, tweet);
	        // iterate over all the words
	        for(int j = 0; j < tokenized.size(); j++) {
	        	String term = tokenized.get(j);
	    		if (termsFrequencies.containsKey(term)) {
	                // update the frequencies
	    			frequencies = termsFrequencies.get(term);
	    			frequencies[vectorIndex]++;
	                termsFrequencies.put(term, frequencies);
	            }
	        }
	    }
	    return termsFrequencies;
	}


	public static ArrayList<String> tokenizeString(ItalianAnalyzer analyzer, String string) {
		// using a chosen analyser, tokenize
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


    private static String buildSAX(double[] timeSeries, int alphabetSize, double nThreshold, Alphabet alphabet) throws SAXException {
        // from timeseries of the word frequencies, create a SAX string that represents it
    	SAXProcessor sp = new SAXProcessor();
        SAXRecords res = sp.ts2saxByChunking(timeSeries, timeSeries.length, alphabet.getCuts(alphabetSize), nThreshold);
        System.out.println(res.getSAXString(""));
        return res.getSAXString("");
    }

	private static HashMap<String, String> transformTimeSeriesToSAX( HashMap<String, double[]> termsFrequencies, int alphabetSize) throws SAXException {
	    // creates SAX from time series for all documents
		HashMap<String, String> termSAX = new HashMap<String, String>();
	
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
	
	    // normalize all elements to 0, 1
	    for (Entry<String, double[]> ee : termsFrequencies.entrySet()) {
	        String tweet = ee.getKey();
	        String sax;
		    try {
		        sax = buildSAX(termsFrequencies.get(tweet), alphabetSize, Math.round(alphabetSize / maxFreq), new NormalAlphabet());
		        termSAX.put(tweet, sax);
		    } catch (SAXException ex) {
		        System.out.println(ex.getMessage());
		    }
	    }
	    return termSAX;
	}
	
	private static void writeClusters(final HashMap<String, Integer> clusters, String sentiment) throws IOException {
		// helper function, saves cluster hashmap to text file
	    final PrintWriter pw = new PrintWriter(new FileWriter(mainTemporalAnalysis.resourcesPathPart0 + "clusters_" + sentiment + ".txt"));
	    for (Entry<String, Integer> cluster : clusters.entrySet()) {
	        String key = cluster.getKey();
	        pw.println(key + " " + clusters.get(key));
	    }
	    pw.close();
	}

	private static HashMap<String, Integer> clusterMapping(HashMap<String, Integer> saxClusters, HashMap<String, String> termSAX) {
		// map all terms to their cluster
	    HashMap<String, Integer> termClusters = new HashMap<>();
	    String sax;
	    for (String term : termSAX.keySet()) {
	        sax = termSAX.get(term);
	        termClusters.put(term, saxClusters.get(sax));
	    }
	    return termClusters;
	}

	public static void main (String[] args) throws Exception{
	        //createIndexAllTweets("positive");
	        //createIndexAllTweets("negative");
		    //createIndexAllTweets("all"); // not really used in the end
		    
		    
		    ArrayList<String> nTermsPositive = topNTerms(1000, "positive");
		    ArrayList<String> nTermsNegative = topNTerms(1000, "negative");
		   // ArrayList<String> nTermsAll = topNTerms(1000, "all"); // not really used in the end
		    
		    HashMap<String, double[]> termTimeSeriesPositive = termsTimeSeries(nTermsPositive, "positive", 43200000L);
		    HashMap<String, double[]> termTimeSeriesNegative = termsTimeSeries(nTermsNegative, "negative", 43200000L);
	
	        int alphabetSize = mainTemporalAnalysis.alphabetSize;
	        int k = mainTemporalAnalysis.clusterCount;
	        HashMap<String, String> termSAXPositive = transformTimeSeriesToSAX(termTimeSeriesPositive, alphabetSize);
	        HashMap<String, String> termSAXNegative = transformTimeSeriesToSAX(termTimeSeriesNegative, alphabetSize);
	        System.out.println("all before done");
	
	        KMeansAlgorithm KMeansPositive = new KMeansAlgorithm(k, alphabetSize, new ArrayList<>(termSAXPositive.values()));
	        HashMap<String, Integer> positiveSAXClusters = KMeansPositive.getClusters();
	        System.out.println("positive getclusters done");
	
	        KMeansAlgorithm KMeansNegative = new KMeansAlgorithm(k, alphabetSize, new ArrayList<>(termSAXNegative.values()));
	        HashMap<String, Integer> negativeSAXClusters = KMeansNegative.getClusters();
	        System.out.println("negative getclusters done");
	        
	        HashMap<String, Integer> clustersPositive = clusterMapping(positiveSAXClusters, termSAXPositive);
	        HashMap<String, Integer> clustersNegative = clusterMapping(negativeSAXClusters, termSAXNegative);
	        System.out.println("mapping done");
	
	        writeClusters(clustersPositive, "positive");
	        writeClusters(clustersNegative, "negative");
	        System.out.println("write done");

	}
}
