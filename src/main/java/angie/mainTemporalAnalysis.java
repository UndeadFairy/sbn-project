package angie;
import angie.configbuilder;
import angie.TweetProcessing;
import angie.TimeDistribution;
import angie.Graphs;
import angie.TimeSeriesComparison;

public class mainTemporalAnalysis {
	// main class launching other parts of the tasks - Part 0
  public static Integer clusterCount = 4;
  public static void main(String[] args) throws Exception {
	// download tweet data full + selected variables
    configbuilder.main(args);
    // creates text files for time distribution for 12h granularity
    TimeDistribution.main(args);
    // k-means clustering, sax string, time series
    TweetProcessing.main(args);
    // graph analysis, save to text files
    Graphs.main(args);
    // creates text files for timeseries comparison for 3h granularity
    TimeSeriesComparison.main(args);
    
  }
}