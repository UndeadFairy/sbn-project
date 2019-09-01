package angie;
import java.io.*;
import java.util.*;
import angie.TweetProcessing;

public class TimeSeriesComparison {

    public static HashMap<Integer, LinkedHashSet<String>> clusterFile(String sentiment, String method) throws IOException {
    	// reads a graph file results from previous parts and save them to a hashmap
    	String clusterPath = mainTemporalAnalysis.resourcesPathPart0 +"graph_" + sentiment + "_" + method + ".txt";
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(clusterPath)));
        String row;
        String[] splitted;
        String tweet;
        Integer clID;
        LinkedHashSet<String> cluster;
        HashMap<Integer, LinkedHashSet<String>> clusteredIDs = new HashMap<Integer, LinkedHashSet<String>>();
        while ((row = bufferedReader.readLine()) != null) {
            splitted = row.split(" ");
            clID = Integer.parseInt(splitted[2]);
            tweet = splitted[0];
            if ((cluster = clusteredIDs.get(clID)) == null) {
            	// if empty, create a new one
                cluster = new LinkedHashSet<String>();
            }
            // update the cluster adding tweet
            cluster.add(tweet);
            clusteredIDs.put(clID, cluster);
        }
        bufferedReader.close();
        return clusteredIDs;
    }
 
    private static void compareTimeSeries(int granularity, String sentiment, String method) throws Exception {
        HashMap<Integer, LinkedHashSet<String>> clusters;
        ArrayList<String> tweets;
        long granularityLong = 3600000L * granularity; // in milliseconds
        clusters = clusterFile(sentiment, method);
        // iterate through all the clusters, to be saved to file for graphs in Python
        for (Integer clId : clusters.keySet()) {
            System.out.println("Creating time series of cluster " + clId + " of sentiment " + sentiment + " of method " + method);
            tweets = new ArrayList<String>(clusters.get(clId));
            HashMap<String, double[]> termsTimeSeriesToSave = TweetProcessing.termsTimeSeries(tweets, sentiment, granularityLong);
            saveTimeSeriesComparison(termsTimeSeriesToSave, clId, sentiment, method);
        }
    }
    
    private static void saveTimeSeriesComparison(HashMap<String, double[]> ts, Integer clId , String sentiment, String method) throws IOException {
        // save results of time series comparison as a ';' delimited file
    	PrintWriter pw = new PrintWriter(new FileWriter(mainTemporalAnalysis.resourcesPathPart0 +"ts3h_" + sentiment + "_" + clId +  "_" + method + ".txt"));
        for (String tweet : ts.keySet()) {
        	StringJoiner joiner = new StringJoiner(";");
        	for (double frequency : ts.get(tweet)) {
        	    joiner.add(Double.toString(frequency));
        	}
        	pw.println(tweet + ";" + joiner.toString());
        }
        pw.close();
    }

    public static void mainTimeSeriesComparison() throws Exception {
    	compareTimeSeries(3, "positive", "kcore");
    	compareTimeSeries(3, "negative", "kcore");
    	compareTimeSeries(3, "positive", "largestcc");
    	compareTimeSeries(3, "negative", "largestcc");
    }

    public static void main(String[] args) throws Exception {
    	mainTimeSeriesComparison();
    }
}
