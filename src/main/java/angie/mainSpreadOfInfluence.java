package angie;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;

import angie.SpreadAnalysis;

public class mainSpreadOfInfluence {
	// main class launching other parts of the tasks - Part 2
	  public static Integer clusterCount = 4;
	  public static String resourcesPathPart2 = "src/main/resources/part2/";
	  public static String givenDataResourcesPath = resourcesPathPart2 + "given_graph/";
	  public static String givenDateStart = "2016-11-26";
	  public static String givenDateEnd = "2016-12-07";
	  
	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException, InterruptedException {
		SpreadAnalysis.mainSpreadAnalysis();
	}

}
