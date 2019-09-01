package angie;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.commons.collections.map.MultiValueMap;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import angie.MainPartOne;

//import it.stilo.g.*;
//import it.stilo.g.structures.WeightedDirectedGraph;
//import it.stilo.g.structures.WeightedUndirectedGraph;
//import it.stilo.g.util.GraphReader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;

public class Mentions {
	static void sleep(long ms) {
	    try { Thread.sleep(ms); }
	    catch(InterruptedException ex) { Thread.currentThread().interrupt(); }
	    }
    public static void MentionPol(String[] args) throws IOException
    {
    	
    	ArrayList<String> Partes = new ArrayList<>(Arrays.asList("1_1", "1_2", "1_3","1_4","1_5","1_6","1_7","1_8","2_1", "2_2", "2_3","2_4","2_5","2_6","2_7","2_8","3_1", "3_2", "3_3","3_4","3_5","3_6","3_7","3_8","4_1", "4_2", "4_3","4_4","4_5","4_6","4_7","4_8","5_1", "5_2", "5_3","5_4","5_5","5_6","5_7","5_8","6_1", "6_2", "6_3","6_4","6_5","6_6","6_7","6_8","7_1", "7_2", "7_3","7_4","7_5","7_6","7_7","7_8","8_1", "8_2", "8_3","8_4","8_5","8_6","8_7","8_8","9_1", "9_2", "9_3","9_4","9_5","9_6","9_7","9_8","10_1", "10_2", "10_3","10_4"));

    	for (String ParteE:Partes) {
    		ArrayList<JSONObject> json=new ArrayList<JSONObject>();
            JSONObject obj;
            
            //Read Politicians UserName List 
            Scanner positives = new Scanner(new File(MainPartOne.resourcesPathPart0 + "PositivePoliticians.txt"));

            ArrayList<String> Pol = new ArrayList<String>();
            ArrayList<String> posPol = new ArrayList<String>();
            while (positives.hasNext()) {
            	posPol.add(positives.next());
            }
            positives.close();
            System.out.println("Positive Users Retrieved");
            Scanner negatives = new Scanner(new File(MainPartOne.resourcesPathPart0 + "NegativePoliticians.txt"));
            ArrayList<String> negPol = new ArrayList<String>();
            while (negatives.hasNext()) {
            	negPol.add(negatives.next());
            }
            negatives.close();
            System.out.println("Negative Users Retrieved");
            Pol.addAll(posPol);
            Pol.addAll(negPol);
            
            
            ArrayList<String> Politicians = new ArrayList<String>();
            for (int i = 0; i < Pol.size(); i++)
            {

                String str = Pol.get(i).substring(1, Pol.get(i).length());
                Politicians.add(str);

            }
            
            Set PolSet = new HashSet(Politicians);
            
            
            //Separate Negatives from Positives
            ArrayList<String> Nega = new ArrayList<String>();
            for (int i = 0; i < negPol.size(); i++)
            {

                String str = negPol.get(i).substring(1, negPol.get(i).length());
                Nega.add(str);

            }
            
            Set NegPolSet = new HashSet(Nega);
            
            
            
            ArrayList<String> Posi = new ArrayList<String>();
            for (int i = 0; i < posPol.size(); i++)
            {

                String str = posPol.get(i).substring(1, posPol.get(i).length());
                Posi.add(str);

            }
            
            Set PosPolSet = new HashSet(Posi);
            
            
            Set<String> negIds = new HashSet<String>();	
            Set<String> posIds = new HashSet<String>();
           				
            HashMap<String, Integer> metric = new HashMap<String, Integer>();
																																																																																																																																																																																																																																																																																																																																																	
            //String ParteE  = "4_2";
    		ArrayList<String> M = new ArrayList<String>();
    		ArrayList<String> TM = new ArrayList<String>();
    		ArrayList<String> Mm = new ArrayList<String>();
    		
            //JSON parser object to parse read file
            JSONParser jsonParser = new JSONParser();
            int counterr=1;
            String line = null;
            MultiValueMap SM=new MultiValueMap();
            
            try 
            {
            	File dir = new File ("../Project/data/Parte_" + ParteE + "/");
            	
            	for (File file:dir.listFiles()) {
            		System.out.println(file);
            		//sleep(3000);
            		FileReader fileReader = new FileReader(file);
                	BufferedReader bufferedReader = new BufferedReader(fileReader);
                	
                	
                	
                	while ((line = bufferedReader.readLine()) != null) {
                		//Read JSON file
                		//This score will be the metric that separates negative or positive supporters
                		counterr=counterr+1;
                		if (counterr%200000==1) {
                			System.out.println("Sleeping");
                			//sleep(60000);
                		} else {
                			if (counterr%50000==1) {
                    			counterr=counterr+1;
                    			System.out.println("Sleeping");
                        		//sleep(30000);
                        		
                        	} else {
            	        		
            	        		String newLine = new String();
            	        		String Check = new String();
            	        		Check = line.substring(0,44);
            	        		if (Check.contains("null")) {
            	        			newLine=line.substring(46,line.length() - 11);
            	        		} else if(Check.contains("zh-")) {
            	        			newLine=line.substring(47,line.length() - 11);
            	        		} else {       	        		
                	        		newLine=line.substring(44,line.length() - 11);
            	        		}
            	        		System.out.println(line);
            	        		obj = (JSONObject) new JSONParser().parse(newLine);
            	                json.add(obj);
            	                
            	    			//Get tweet from status
            	    			String tweet = (String) obj.get("text");
            	    			
            	    			//Get user object from status
            	    			JSONObject user = (JSONObject) obj.get("user");
            	    			//Get user's id
            	    			String id = (String) user.get("id_str");   
            	              
            	    			//Get entities in the status
            	    			JSONObject entities = (JSONObject) obj.get("entities");
            	
            	    			//Get the entity user mentions which is a json array
            	    			JSONArray user_mentions = (JSONArray) entities.get("user_mentions");
            	             
            	
            	    			for(int i=0; i<user_mentions.size(); i++){
            	             	//If the array is not empty, it means if the user actually mentions someone
            	    				if (user_mentions.get(i) != null ) {
            	    					//Save the object corresponding to each element of the JsonArray
            	    					JSONObject mentions_object = (JSONObject) user_mentions.get(i);
            	              		
            	    					//Get the id in the object previously saved
            	    					String id_mentions = (String) mentions_object.get("id_str"); 
            	    					
            	    					//Get the string name of the mentioned user to filter
            	    					String screen_name_mentions = (String) mentions_object.get("screen_name");
            	    					
            	    					if (PolSet.contains(screen_name_mentions)) {
            	    						//Dictionary which contains the name of the user mentioning the politicians in P
            	    						//as keys and the corresponding Politicians as values.
            	    						
            	    						SM.put(id, id_mentions);
            	    	                    M.add(id);
            	    	                    TM.add(tweet);
            	    	                    int score = 0;
            	    						if (!metric.containsKey(id)) {
            	    							
            	    							if (NegPolSet.contains(screen_name_mentions)) {
                	    							metric.put(id,-1);
                	    							negIds.add(id_mentions);
                	    						} else if (PosPolSet.contains(screen_name_mentions)) {
                	    							metric.put(id,1);
                	    							posIds.add(id_mentions);
                	    						}
            	    							
            	    							metric.put(id, 0);
            	    						} else if (metric.containsKey(id)) {
            	    							score = metric.get(id);
            	    							if (NegPolSet.contains(screen_name_mentions)) {
            	    								metric.put(id, metric.get(id) - 1);
                	    							negIds.add(id_mentions);
                	    						} else if (PosPolSet.contains(screen_name_mentions)) {
                	    							metric.put(id, metric.get(id) + 1);
                	    							posIds.add(id_mentions);
                	    						}
            	    						}
            	    						
            		                      	
            	    					}
            	    				}	
            	    			}
            	    		System.out.println(counterr);
            	    		//System.out.println("RETRIEVED");
            		               
            	        		
                        	}
                		}
                		
                	}
                	
                	System.out.println(SM);
                 		
                
                	bufferedReader.close();
            	} 
            	
        		
            	
            		
            	
                

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            
            Set<String> keys = SM.keySet();
            System.out.println(keys);
            
            
            //Save document with the map of user ID and the user ID of politicians that mention
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new File(MainPartOne.resourcesPathPart1 + "Part_1_1/Data_Mentions/data_mentions_" + ParteE + ".txt"), SM);
            
            //Save document with the metric to separate negative supporters from positive supporters
            mapper = new ObjectMapper();
            mapper.writeValue(new File(MainPartOne.resourcesPathPart1 + "Part_1_1/Data_Mentions/scores_" + ParteE + ".txt"), metric);
            
            //Save positive IDS and NegativeIDS
            BufferedWriter pos_pol_wr = new BufferedWriter(new FileWriter(MainPartOne.resourcesPathPart1 + "Part_1_1/Users_IDS_mentions/pos_pol_id_" + ParteE +".txt"));
            for (String a : posIds) {
            	pos_pol_wr.write(a);
            	pos_pol_wr.newLine();
            }
            pos_pol_wr.close();
            
            BufferedWriter neg_pol_wr = new BufferedWriter(new FileWriter(MainPartOne.resourcesPathPart1 + "Part_1_1/Users_IDS_mentions/neg_pol_id_" + ParteE +".txt"));
            for (String a : negIds) {
            	neg_pol_wr.write(a);
            	neg_pol_wr.newLine();
            }
            neg_pol_wr.close();
            
            //Save document with the users that mention a politician
            BufferedWriter users_mentions = new BufferedWriter(new FileWriter(MainPartOne.resourcesPathPart1 + "Part_1_1/Users_IDS_mentions/user_mentions_id_" + ParteE +".txt"));
            
    	    for (String it : keys) {
    	    	users_mentions.write(it);
    	    	users_mentions.newLine();
    	    }
    	    users_mentions.close();
            
    	  //Save document with the users that mention a politician
            BufferedWriter tweets = new BufferedWriter(new FileWriter(MainPartOne.resourcesPathPart1 + "Part_1_1/Users_IDS_mentions/tweets" + ParteE +".txt"));
            
    	    for (String it : TM) {
    	    	tweets.write(it);
    	    	tweets.newLine();
    	    }
    	    tweets.close();
    		
    		sleep(60000);
    		
    	}
    	
        
    }
    
    
}
