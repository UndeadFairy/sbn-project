package angie;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import angie.MainPartOne;


import org.apache.commons.collections.map.MultiValueMap;
import org.json.simple.JSONObject;

import it.stilo.g.structures.DoubleValues;

public class UnificationTwo {
	public static void UnifyMentions(String[] args) throws IOException
    {
        
        //Read Politicians UserName List 
		
        String Path =  MainPartOne.resourcesPathPart1 + "Part_1_1/Data_Mentions/";
        File dir = new File (Path);
        MultiValueMap SM=new MultiValueMap();
        ArrayList<String> users_IDs_Neg= new ArrayList<String>();
    	for (File file:dir.listFiles()) {
    		String line=null;
    		FileReader fileReader = new FileReader(file);
        	BufferedReader bufferedReader = new BufferedReader(fileReader);
    		while ((line = bufferedReader.readLine()) != null) {
    			line = line.replace("{","");
    			line = line.replace("}","");
    			
    			String[] str = line.split("],");
    			
    			for (String s:str) {
    				String ns=s + "]";
    				ns = ns.replace("[", "");
    				ns = ns.replace("]", "");
    				
    				String[] data = ns.split(":");
    				//ArrayList da = new ArrayList<>(Arrays.asList(data[1]));
    				String key = data[0];
    				//ArrayList da = data[1];
    				SM.put(key, data[1]);
    			}
    			
    		}
    		
    		//Save document with the map of user ID and the user ID of politicians that mention
        	
        		
        	
    	}
    	 //Save document with the map of user ID and the user ID of politicians that mention
		System.out.println(SM);
		
		
		//Write the TOP 1000 with scores
        String Path_1=MainPartOne.resourcesPathPart1 + "Part_1_1/";
        BufferedWriter Scn = new BufferedWriter(new FileWriter(Path_1 + "UserMentions.txt"));
        Iterator in = SM.entrySet().iterator();
        while (in.hasNext()) {
            Map.Entry pair = (Map.Entry)in.next();
            String ke =pair.getKey().toString();
            ke = ke.replaceAll("^(['\"])(.*)\\1$", "$2");
            String va = pair.getValue().toString();
            va = va.replaceAll("^(['\"])(.*)\\1$", "$2");
            String all= ke + ":" + va;
            Scn.write(all);
            Scn.newLine();
        }
        Scn.close();    }
}
