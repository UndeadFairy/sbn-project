package angie;
import angie.configbuilder;
import angie.TweetProcessing;
import angie.TimeDistribution;
import angie.Graphs;
import angie.TimeSeriesComparison;

public class mainTemporalAnalysis {
	// main class launching other parts of the tasks - Part 0
  public static Integer clusterCount = 4;
  public static String resourcesPathPart0 = "src/main/resources/part0/";
  public static String givenDataResourcesPath = resourcesPathPart0 + "given_data/";
  public static String dateStart = "2016-04-01";
  public static String dateEnd = "2016-12-05";
  public static String givenDateStart = "2016-11-26";
  public static String givenDateEnd = "2016-12-07";
  
  public static Integer alphabetSize = 20;	

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