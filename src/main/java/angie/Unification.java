package angie;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import angie.MainPartOne;

import org.json.simple.JSONObject;

import it.stilo.g.structures.DoubleValues;

public class Unification {
	public static void UnifyMentionsID(String[] args) throws IOException
    {
        
        //Read Politicians UserName List 
		
		
        String Path =  MainPartOne.resourcesPathPart1 + "Part_1_1/Users_IDS_mentions/";
        File dir = new File (Path);
        String line = null;
        ArrayList<String> users_IDs_Neg= new ArrayList<String>();
    	for (File file:dir.listFiles()) {
    		FileReader fileReader = new FileReader(file);
        	BufferedReader bufferedReader = new BufferedReader(fileReader);
        	
        	while ((line = bufferedReader.readLine()) != null) {
        		//Read JSON file
        		users_IDs_Neg.add(line);
        	}
        	fileReader.close();
    	}
    	@SuppressWarnings({ "unchecked", "rawtypes" })
		Set<String> M = new HashSet(users_IDs_Neg);
    	System.out.println(M);
    	System.out.println(M.size());
    	System.out.println(users_IDs_Neg.size());
    	
    	//Save the Hubs
    	
    	
        BufferedWriter IDS_tot = new BufferedWriter(new FileWriter(MainPartOne.resourcesPathPart1 + "Part_1_1/UsersMentionsIDS.txt"));
        for (String m:M) {
        	IDS_tot.write(m);
        	IDS_tot.newLine();
        }
        IDS_tot.close();
        
        //Unify tweets
        String Path_2 =  MainPartOne.resourcesPathPart1 + "Part_1_1/Tweets/";
        File dire = new File (Path_2);
        line = null;
        ArrayList<String> tweets= new ArrayList<String>();
    	for (File file:dire.listFiles()) {
    		FileReader fileReader = new FileReader(file);
        	BufferedReader bufferedReader = new BufferedReader(fileReader);
        	
        	while ((line = bufferedReader.readLine()) != null) {
        		//Read JSON file
        		tweets.add(line);
        	}
        	fileReader.close();
    	}
    	
    		//Save the tweets
    	
    	
        BufferedWriter tweet = new BufferedWriter(new FileWriter(MainPartOne.resourcesPathPart1 + "Part_1_1/Tweets.txt"));
        for (String m:tweets) {
        	tweet.write(m);
        	tweet.newLine();
        }
        tweet.close();		
    }
}
