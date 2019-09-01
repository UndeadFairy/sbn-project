package angie;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import angie.MainPartOne;

public class Scores {
	public static void Scorer(String[] args) throws IOException
    {
        
        //Read Politicians UserName List 
		
		
        String Path =  MainPartOne.resourcesPathPart1 + "Part_1_1/Scores/";
        File dir = new File (Path);
        String line = null;
        ArrayList<String> MNegatives = new ArrayList<>();
    	ArrayList<String> MPositives = new ArrayList<>();
    	ArrayList<String> MnoNeu = new ArrayList<>();
    	
        HashMap<String, Integer> metric = new HashMap<String, Integer>();
        HashMap<String, Integer> posScores = new HashMap<String, Integer>();
        HashMap<String, Integer> negScores = new HashMap<String, Integer>();
        HashMap<String, Integer> nonNeuScores = new HashMap<String, Integer>();
        
    	for (File file:dir.listFiles()) {
    		FileReader fileReader = new FileReader(file);
        	BufferedReader bufferedReader = new BufferedReader(fileReader);
        	
        	while ((line = bufferedReader.readLine()) != null) {
        		line = line.replace("{","");
    			line = line.replace("}","");
                line= line.replaceAll("^(['\"])(.*)\\1$", "$2");
        		String[] str = line.split(",");  
        		
        		for (String s:str) {
    				String[] data = s.split(":");
    				//ArrayList da = new ArrayList<>(Arrays.asList(data[1]));
    				String key = data[0];
    				//ArrayList da = data[1];
    				int value = Integer.parseInt(data[1]);	
    				if (!metric.containsKey(key)) {
    					metric.put(key, value);
    				} else if (metric.containsKey(key)){
    					int v=metric.get(key);
    					v=v+value;
    					metric.put(key,v);
    				}
    			}
        	}
        	fileReader.close();
    	}
    	Iterator it = metric.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String ke =pair.getKey().toString();
            int val=(int) pair.getValue();
            if (val < 0) {
            	MNegatives.add(ke);
            	MnoNeu.add(ke);
            	negScores.put(ke,val);
            	nonNeuScores.put(ke,val);
            } else if (val>0) {
            	MPositives.add(ke);
            	MnoNeu.add(ke);
            	posScores.put(ke,val);
            	nonNeuScores.put(ke,val);
            }
        }
    	System.out.println(MPositives.size());
    	System.out.println(MNegatives.size());
    	System.out.println(MnoNeu.size());
    	System.out.println(metric.size());
    	
    	//Save document with the metric to separate negative supporters from positive supporters
    	
    	String path= MainPartOne.resourcesPathPart1 + "Part_1_1/";
        //mapper.writeValue(new File(path + "Scores.txt"), metric);
        
    	BufferedWriter Sc = new BufferedWriter(new FileWriter(path + "Scores.txt"));
        Iterator in = metric.entrySet().iterator();
        while (in.hasNext()) {
            Map.Entry pair = (Map.Entry)in.next();
            String ke =pair.getKey().toString();
            ke= ke.replaceAll("^(['\"])(.*)\\1$", "$2");
            ke= ke.replaceAll("^(['\"])(.*)\\1$", "$2");

            String va = pair.getValue().toString();
            String all= ke + ":" + va;
            Sc.write(all);
            Sc.newLine();
        }
        Sc.close();
        
        BufferedWriter Scp = new BufferedWriter(new FileWriter(path + "PositiveScores.txt"));
        in = posScores.entrySet().iterator();
        while (in.hasNext()) {
            Map.Entry pair = (Map.Entry)in.next();
            String ke =pair.getKey().toString();
            ke= ke.replaceAll("^(['\"])(.*)\\1$", "$2");
            ke= ke.replaceAll("^(['\"])(.*)\\1$", "$2");
            String va = pair.getValue().toString();
            String all= ke + ":" + va;
            Scp.write(all);
            Scp.newLine();
        }
        Scp.close();
        
        BufferedWriter Scn = new BufferedWriter(new FileWriter(path + "NegativeScores.txt"));
        in = negScores.entrySet().iterator();
        while (in.hasNext()) {
            Map.Entry pair = (Map.Entry)in.next();
            String ke =pair.getKey().toString();
            ke= ke.replaceAll("^(['\"])(.*)\\1$", "$2");
            ke= ke.replaceAll("^(['\"])(.*)\\1$", "$2");
            String va = pair.getValue().toString();
            String all= ke + ":" + va;
            Scn.write(all);
            Scn.newLine();
        }
        Scn.close();
        
        BufferedWriter Scnn = new BufferedWriter(new FileWriter(path + "AllMScores.txt"));
        in = nonNeuScores.entrySet().iterator();
        while (in.hasNext()) {
            Map.Entry pair = (Map.Entry)in.next();
            String ke =pair.getKey().toString();
            ke= ke.replaceAll("^(['\"])(.*)\\1$", "$2");
            ke= ke.replaceAll("^(['\"])(.*)\\1$", "$2");
            String va = pair.getValue().toString();
            String all= ke + ":" + va;
            Scnn.write(all);
            Scnn.newLine();
        }
        Scnn.close();
        
    
        BufferedWriter neg = new BufferedWriter(new FileWriter(path + "MNeg.txt"));
        for (String a : MNegatives) {
        	a = a.replaceAll("^(['\"])(.*)\\1$", "$2");
        	a= a.replaceAll("^(['\"])(.*)\\1$", "$2");
        	neg.write(a);
        	neg.newLine();
        }
        neg.close();
        
        BufferedWriter pos = new BufferedWriter(new FileWriter(path + "MPos.txt"));
        for (String a : MPositives) {
        	a = a.replaceAll("^(['\"])(.*)\\1$", "$2");
        	a= a.replaceAll("^(['\"])(.*)\\1$", "$2");

        	pos.write(a);
        	pos.newLine();
        }
        pos.close();
        
        BufferedWriter non = new BufferedWriter(new FileWriter(path + "M.txt"));
        for (String a : MnoNeu) {
        	a = a.replaceAll("^(['\"])(.*)\\1$", "$2");
        	a= a.replaceAll("^(['\"])(.*)\\1$", "$2");

        	non.write(a);
        	non.newLine();
        }
        non.close();
    }
}
