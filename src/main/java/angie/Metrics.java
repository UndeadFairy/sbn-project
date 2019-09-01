package angie;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import angie.MainPartOne;

public class Metrics {
	public static HashMap<String, Double> sortByValue(HashMap<String, Double> hm) 
    {
        // Create a list from elements of HashMap 
        List<Map.Entry<String, Double> > list = 
               new LinkedList<Map.Entry<String, Double> >(hm.entrySet()); 
  
        // Sort the list 
        Collections.sort(list, new Comparator<Map.Entry<String, Double> >() { 
            public int compare(Map.Entry<String, Double> o1,  
                               Map.Entry<String, Double> o2) 
            { 
                return (o2.getValue()).compareTo(o1.getValue()); 
            } 
        }); 
          
        // put data from sorted list to hashmap  
        HashMap<String, Double> temp = new LinkedHashMap<String, Double>(); 
        for (Map.Entry<String, Double> aa : list) { 
            temp.put(aa.getKey(), aa.getValue()); 
        } 
        return temp; 
    } 
	public static void MetricCreator(String[] args) throws IOException, InterruptedException
    {
		BufferedReader br = null;
        FileReader fr = null;
        String Path_1 = MainPartOne.resourcesPathPart1 + "Part_1_1/";
        String PosMentionsPath = Path_1 + "PositiveScores.txt";
        String NegMentionsPath = Path_1 + "NegativeScores.txt";
        
        String Path_2 = MainPartOne.resourcesPathPart1 + "Part_1_2/";
        String HubScorePath = Path_2 + "HubsWithScore.txt";
        
        HashMap<String, Double> PosMentions = new HashMap<String, Double>();
        HashMap<String, Double> NegMentions = new HashMap<String, Double>();
        HashMap<String, Double> HubScore = new HashMap<String, Double>();
        
        
        
        //Read the number of mentions for positive users
        fr = new FileReader(PosMentionsPath);
		br = new BufferedReader(fr);
		
		String Line;
		
		while ((Line = br.readLine()) != null) {
			String[] data = Line.split(":");
			//ArrayList da = new ArrayList<>(Arrays.asList(data[1]));
			String key = data[0];
			key = key.replaceAll("^(['\"])(.*)\\1$", "$2");
			//ArrayList da = data[1];
			Double value = Double.parseDouble(data[1]) ;
			PosMentions.put(key,value );
		}
		fr.close();
		
		//Read the number of mentions for negative users
        fr = new FileReader(NegMentionsPath);
		br = new BufferedReader(fr);

		
		while ((Line = br.readLine()) != null) {
			String[] data = Line.split(":");
			//ArrayList da = new ArrayList<>(Arrays.asList(data[1]));
			String key = data[0];
			key = key.replaceAll("^(['\"])(.*)\\1$", "$2");
			//ArrayList da = data[1];
			Double value = Double.parseDouble(data[1]);
			NegMentions.put(key,value*-1);
		}
		fr.close();
		
		//Read the Hub Scores
        fr = new FileReader(HubScorePath);
		br = new BufferedReader(fr);

		
		while ((Line = br.readLine()) != null) {
			String[] data = Line.split("\t");
			//ArrayList da = new ArrayList<>(Arrays.asList(data[1]));
			String key = data[0];
			key = key.replaceAll("^(['\"])(.*)\\1$", "$2");
			//ArrayList da = data[1];
			Double value = Double.parseDouble(data[1]);
			HubScore.put(key,value);
		}
		
		fr.close();
		
		//Normalize Positive and Negative Mentions
		Double maxPos = Collections.max(PosMentions.values());
		Double maxNeg = Collections.max(NegMentions.values());
		Double minPos = Collections.min(PosMentions.values());
		Double minNeg = Collections.min(NegMentions.values());
        
        HashMap<String, Double> NormPos = new HashMap<String, Double>();
        HashMap<String, Double> NormNeg = new HashMap<String, Double>();
        
        Iterator in = PosMentions.entrySet().iterator();
        while (in.hasNext()) {
            Map.Entry pair = (Map.Entry)in.next();
            String ke =pair.getKey().toString();
            Double va = (Double) pair.getValue();
            Double Norm = ((va-minPos)/(maxPos-minPos));
            NormPos.put(ke, Norm);
        }
        
        in = NegMentions.entrySet().iterator();
        while (in.hasNext()) {
            Map.Entry pair = (Map.Entry)in.next();
            String ke =pair.getKey().toString();
            Double va = (Double) pair.getValue();
            Double Norm = ((va-minNeg)/(maxPos-minNeg));
            NormNeg.put(ke, Norm);
        }
        
        //Create score
        HashMap<String, Double> ScorePos = new HashMap<String, Double>();
        HashMap<String, Double> ScoreNeg = new HashMap<String, Double>();
        Double Scoring = null;
        in = HubScore.entrySet().iterator();
        while (in.hasNext()) {
            Map.Entry pair = (Map.Entry)in.next();
            String ke =pair.getKey().toString();
            Double va = (Double) pair.getValue();
            
            if (NormPos.containsKey(ke)) {
            	Scoring = (va + NormPos.get(ke))/2;
            	ScorePos.put(ke, Scoring);
            } else if (NormNeg.containsKey(ke)) {
            	Scoring = (va + NormNeg.get(ke))/2;
            	ScoreNeg.put(ke, Scoring);
            }
            
        }
        Map<String, Double> SortedScorePos = sortByValue(ScorePos); 
        Map<String, Double> SortedScoreNeg = sortByValue(ScoreNeg); 
        
        HashMap<String, Double> M_2Pos = new HashMap<String, Double>();
        HashMap<String, Double> M_2Neg = new HashMap<String, Double>();
        
        
        Set<String> keysSP = SortedScorePos.keySet();
        String[] keysSPArray = keysSP.toArray(new String[keysSP.size()]);
        for(int i=0; i<keysSPArray.length && i<1000;i++) {
            M_2Pos.put(keysSPArray[i],(ScorePos.get(keysSPArray[i])));
        }
        
        Set<String> keysSN = SortedScoreNeg.keySet();
        String[] keysSNArray = keysSN.toArray(new String[keysSN.size()]);
        for(int i=0; i<keysSNArray.length && i<1000;i++) {
            M_2Neg.put(keysSNArray[i],(ScoreNeg.get(keysSNArray[i])));
        }
        
        Map<String, Double> Mpos = sortByValue(M_2Pos); 
        Map<String, Double> Mneg = sortByValue(M_2Neg); 
        
        
        //Write the TOP 1000 with scores
        String Path_3=MainPartOne.resourcesPathPart1 + "Part_1_3/";
        BufferedWriter Scn = new BufferedWriter(new FileWriter(Path_3 + "M_2Neg_scores.txt"));
        in = Mneg.entrySet().iterator();
        while (in.hasNext()) {
            Map.Entry pair = (Map.Entry)in.next();
            String ke =pair.getKey().toString();
            String va = pair.getValue().toString();
            String all= ke + ":" + va;
            Scn.write(all);
            Scn.newLine();
        }
        Scn.close();
        
        BufferedWriter Scp = new BufferedWriter(new FileWriter(Path_3 + "M_2Pos_scores.txt"));
        in = Mpos.entrySet().iterator();
        while (in.hasNext()) {
            Map.Entry pair = (Map.Entry)in.next();
            String ke =pair.getKey().toString();
            String va = pair.getValue().toString();
            String all= ke + ":" + va;
            Scp.write(all);
            Scp.newLine();
        }
        Scp.close();
        
        //Write just the IDS
        
        BufferedWriter Scn2 = new BufferedWriter(new FileWriter(Path_3 + "M_2Neg.txt"));
        in = Mneg.entrySet().iterator();
        while (in.hasNext()) {
            Map.Entry pair = (Map.Entry)in.next();
            String ke =pair.getKey().toString();
            Scn2.write(ke);
            Scn2.newLine();
        }
        Scn2.close();
        
        BufferedWriter Scp2 = new BufferedWriter(new FileWriter(Path_3 + "M_2Pos.txt"));
        in = Mpos.entrySet().iterator();
        while (in.hasNext()) {
            Map.Entry pair = (Map.Entry)in.next();
            String ke =pair.getKey().toString();
            Scp2.write(ke);
            Scp2.newLine();
        }
        Scp2.close();
        
        
        
        
      //Write the all the users with scores already sorted
        BufferedWriter Scn3 = new BufferedWriter(new FileWriter(Path_3 + "M_Neg_scores.txt"));
        in = SortedScoreNeg.entrySet().iterator();
        while (in.hasNext()) {
            Map.Entry pair = (Map.Entry)in.next();
            String ke =pair.getKey().toString();
            String va = pair.getValue().toString();
            String all= ke + ":" + va;
            Scn3.write(all);
            Scn3.newLine();
        }
        Scn3.close();
        
        BufferedWriter Scp3 = new BufferedWriter(new FileWriter(Path_3 + "M_Pos_scores.txt"));
        in = SortedScorePos.entrySet().iterator();
        while (in.hasNext()) {
            Map.Entry pair = (Map.Entry)in.next();
            String ke =pair.getKey().toString();
            String va = pair.getValue().toString();
            String all= ke + ":" + va;
            Scp3.write(all);
            Scp3.newLine();
        }
        Scp3.close();
        
        //Write just the IDS
        
        BufferedWriter Scn4 = new BufferedWriter(new FileWriter(Path_3 + "M_Neg.txt"));
        in = SortedScoreNeg.entrySet().iterator();
        while (in.hasNext()) {
            Map.Entry pair = (Map.Entry)in.next();
            String ke =pair.getKey().toString();
            Scn4.write(ke);
            Scn4.newLine();
        }
        Scn4.close();
        
        BufferedWriter Scp4 = new BufferedWriter(new FileWriter(Path_3 + "M_Pos.txt"));
        in = SortedScoreNeg.entrySet().iterator();
        while (in.hasNext()) {
            Map.Entry pair = (Map.Entry)in.next();
            String ke =pair.getKey().toString();
            Scp4.write(ke);
            Scp4.newLine();
        }
        Scp4.close();
        
    }
}
