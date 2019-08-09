package angie;
import java.io.*;
import java.util.*;
import angie.TweetProcessing;

public class TimeSeriesComparison {

    public static HashMap<Integer, LinkedHashSet<String>> clusterFile(String sentiment, String method) throws IOException {
    	String clusterPath = "src/main/resources/graph_" + sentiment + "_" + method + ".txt";
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(clusterPath)));
        String row;
        String[] splitted;
        String tweet;
        Integer clusterID;
        LinkedHashSet<String> cluster;
        HashMap<Integer, LinkedHashSet<String>> clusteredIDs = new HashMap<Integer, LinkedHashSet<String>>();
        while ((row = bufferedReader.readLine()) != null) {
            splitted = row.split(" ");
            tweet = splitted[0];
            clusterID = Integer.parseInt(splitted[2]);
            if ((cluster = clusteredIDs.get(clusterID)) == null) {
                cluster = new LinkedHashSet<String>();
            }
            cluster.add(tweet);
            clusteredIDs.put(clusterID, cluster);
        }
        return clusteredIDs;
    }
    public static void main(String[] args) throws Exception {
    	compareTimeSeries(3, "positive", "kcore");
    	compareTimeSeries(3, "negative", "kcore");
    	compareTimeSeries(3, "positive", "largestcc");
    	compareTimeSeries(3, "negative", "largestcc");

    }
    
    private static void saveTimeSeriesComparison(HashMap<String, double[]> ts, Integer clId , String sentiment, String method) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter("src/main/resources/ts3h_" + sentiment + "_" + clId +  "_" + method + ".txt"));
        for (String tweet : ts.keySet()) {
        	StringJoiner joiner = new StringJoiner(";");
        	for (double frequency : ts.get(tweet)) {
        	    joiner.add(Double.toString(frequency));
        	}
        	pw.println(tweet + ";" + joiner.toString());
        }
        pw.close();
    }
 
    private static void compareTimeSeries(int granularity, String sentiment, String method) throws Exception {
        HashMap<Integer, LinkedHashSet<String>> clusters;
        ArrayList<String> tweets;
        long granularityLong = 3600000L * granularity; // in milliseconds
        clusters = clusterFile(sentiment, method);
        // iterate through all the clusters, to be printed to file for graphs in python
        for (Integer clId : clusters.keySet()) {
            System.out.println("Creating time series of cluster " + clId + " of sentiment " + sentiment + " of method " + method);
            tweets = new ArrayList<String>(clusters.get(clId));
            HashMap<String, double[]> termsTimeSeriesToSave = TweetProcessing.termsTimeSeries(tweets, sentiment, granularityLong);
            saveTimeSeriesComparison(termsTimeSeriesToSave, clId, sentiment, method);
            // save data to disk -> python
//                    ImmutableTriple<ArrayList<String>, ArrayList<double[]>, ArrayList<double[]>> dataToPlot = processTSDataToPlot(hmTermsTS, timeInterval);
//
//                    ArrayList<String> labels = dataToPlot.getLeft();
//                    ArrayList<double[]> xvaluesList = dataToPlot.getMiddle();
//                    ArrayList<double[]> yvaluesList = dataToPlot.getRight();
//
//                    double maxFreq = -1;
//                    for (double[] termFreqs : yvaluesList) {
//                        for (int i = 0; i < termFreqs.length; i++) {
//                            if (termFreqs[i] > maxFreq) {
//                                maxFreq = termFreqs[i];
//                            }
//                        }
//                    }
//
//                    for (double[] termFreqs : yvaluesList) {
//                        for (int i = 0; i < termFreqs.length; i++) {
//                            termFreqs[i] = termFreqs[i] / maxFreq;
//                        }
//                    }
//
//                    graphTitle = "Evolution of terms frequency on time (parameters: " + prefix + ", " + clusterType + ")";
//                    Plotter tsPlotter = new Plotter(graphTitle, labels, xvaluesList, yvaluesList);
//                    tsPlotter.savePlot(RESOURCES_DIRECTORY + "/images/" + prefix + "_" + clusterType + "_" + clusterID + ".PNG");
//                }


        }
        System.out.println("Done");
    }
}
