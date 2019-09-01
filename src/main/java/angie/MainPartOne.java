package angie;


import java.io.IOException;

import angie.Mentions;
import angie.Unification;
import angie.UnificationTwo;
import angie.CCandHITS;
import angie.Scores;
import angie.Metrics;
import angie.KPP;

public class MainPartOne {
	public static String resourcesPathPart0 = "src/main/resources/part0/";
	public static String resourcesPathPart1 = "src/main/resources/Part_1/";

	public static void main(String[] args) throws IOException, InterruptedException
    {
		//Part 1.1: From the entire tweets dataset, identify tweets of users that mention one of the politicians
		//M: set of users 
		//T(M): Set of related tweets
		//In this part the Data was separated into different set of files given the high volume of the dataset
		Mentions.MentionPol(args);
		
		
		
		//As the dataset was divided in different parts due to RAM limitations of the computer
		//The information retrieved had to be unified 
		Unification.UnifyMentionsID(args);
		UnificationTwo.UnifyMentions(args);
		Scores.Scorer(args);
		System.out.println("Part 1.1: DONE!");
		Mentions.sleep(3000);
		
		//Part 1.2: Using the provided graph and the library G select the subgraph induced by users in M
		//Find the Largest Connected Component and compute HITS. Find the 2000 highest ranked users
		CCandHITS.CCHits(args);
		System.out.println("Part 1.2: DONE!");
		Mentions.sleep(3000);
		
		//Part 1.3: Partition the users of M in Positive supporters and Negative supporters
		//Identifying the users mentioning more frequently each candidate (Positive and Negative)
		//This part was already done before while doing the Part 1.1 in the Score.Scorer method
		
		//Find the 1000 users for both Positive and Negative who highly support the candidates and are highly central
		//Define a combined measure to select this candidates
		//M' will be the set of these users (belongs to M)
		Metrics.MetricCreator(args);
		System.out.println("Part 1.3: DONE!");
		Mentions.sleep(3000);
		
		//Part 1.4: Identify for each option (YES/NO) the top 500 k-players using the KPP-NEG algorithm
		KPP.KPP_NEG(args);
		System.out.println("Part 1.4: DONE!");
		Mentions.sleep(3000);
		
    }
}
