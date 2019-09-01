package angie;

import java.io.IOException;
import it.stilo.g.algo.KppNeg;
import it.stilo.g.algo.SubGraph;
import it.stilo.g.structures.DoubleValues;
import it.stilo.g.structures.WeightedDirectedGraph;
import it.stilo.g.util.GraphReader;
import it.stilo.g.util.NodesMapper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import angie.MainPartOne;


public class KPP {
	public static HashMap<Long, Double> sortByValue(HashMap<Long, Double> hm) 
    {
        // Create a list from elements of HashMap 
        List<Map.Entry<Long, Double> > list = 
               new LinkedList<Map.Entry<Long, Double> >(hm.entrySet()); 
  
        // Sort the list 
        Collections.sort(list, new Comparator<Map.Entry<Long, Double> >() { 
            public int compare(Map.Entry<Long, Double> o1,  
                               Map.Entry<Long, Double> o2) 
            { 
                return (o2.getValue()).compareTo(o1.getValue()); 
            } 
        }); 
          
        // put data from sorted list to hashmap  
        HashMap<Long, Double> temp = new LinkedHashMap<Long, Double>(); 
        for (Map.Entry<Long, Double> aa : list) { 
            temp.put(aa.getKey(), aa.getValue()); 
        } 
        return temp; 
    } 
	public static void KPP_NEG(String[] args) throws IOException, InterruptedException
    {
		int runner = (int) (Runtime.getRuntime().availableProcessors());
    	NodesMapper<Long> mapper = new NodesMapper<Long>();
    	
    	//Eliminate neutral users : Just take into account users that support Positive or Negative Politicians
    	ArrayList<Integer> UsersNN = new ArrayList<>();
    	String path = MainPartOne.resourcesPathPart1 + "Part_1_1/M.txt";
    	Scanner NN = new Scanner (new File(path));
    	while (NN.hasNext()) {
    		long b = Long.parseLong(NN.next());
    		int id = mapper.getId(b);
    		UsersNN.add(id);
    	}
    	NN.close();
    	int TotalUsers = UsersNN.size();
    	
    	System.out.println("M users retrieved");
    	
    	
    	
    	//Read Largest Connected Component Graph
    	WeightedDirectedGraph g = new WeightedDirectedGraph(TotalUsers);
    	String filename = MainPartOne.resourcesPathPart1 + "Part_1_2/subgraphLCC";
    	GraphReader.readGraph(g, filename, true);
    	
    	System.out.println("Largest Connected Component Graph read");
    	
    	//Add Threshold to reduce running time
    	int th=100;
    	int degree;
    	ArrayList<Integer> UsersTh = new ArrayList<>();
    	int[] vertex = g.getVertex();
    	
    	for (int v : vertex) {
    		try {
    			degree=g.in[v].length + g.out[v].length;
    		} catch (Exception ex) {
    			degree=0;
    		}
    		
    		if (degree > th) {
    			UsersTh.add(v);
    		} 
    	}
    	
    	System.out.println("Threshold applied");
    	
    	//Create nodes for th users
    	int[] nodesTH = new int[UsersTh.size()];
    	for (int i = 0 ; i < UsersTh.size(); i++) {
    		int m = UsersTh.get(i);
    		nodesTH[i]= m;
    	}
    	
    	
    	//Create subgraph
    	WeightedDirectedGraph gTh = SubGraph.extract(g, nodesTH, runner);
    	System.out.println("TH Subgraph created");
    	
    	String line =null;
    	//Get Positive Users
    	String posPath = MainPartOne.resourcesPathPart1 + "Part_1_1/MPos.txt";
    	ArrayList<String> pos_ids = new ArrayList<>();
    	FileReader fileReader = new FileReader(posPath);
    	BufferedReader bufferedReader = new BufferedReader(fileReader);
    	
    	while ((line = bufferedReader.readLine()) != null) {
    		//Read JSON file
    		pos_ids.add(line);
    	}
    	fileReader.close();
    	int total_pos = pos_ids.size();
    	
    	
    	//Get Negative Users
    	String negPath = MainPartOne.resourcesPathPart1 + "Part_1_1/MNeg.txt";
    	ArrayList<String> neg_ids = new ArrayList<>();
    	FileReader fR = new FileReader(negPath);
    	BufferedReader bR = new BufferedReader(fR);
    	
    	while ((line = bR.readLine()) != null) {
    		//Read JSON file
    		neg_ids.add( line);
    	}
    	fR.close();
    	int total_neg = neg_ids.size();
    	
    	System.out.println("Positive and Negative users retieved");
    	
    	//Create nodes for both positive and negative users
    	ArrayList<Integer> nPos = new ArrayList<>();
    	for (int i = 0 ; i < total_pos; i++) {
    		String a = pos_ids.get(i);
    		Long r = Long.parseLong(a);		
    		int id = mapper.getId(r);
    		if (UsersTh.contains(id)) {
    			nPos.add(id);
    		}
    		
    	}
    	
    	ArrayList<Integer> nNeg = new ArrayList<>();
    	for (int i = 0 ; i < total_neg; i++) {
    		String a = neg_ids.get(i);
    		Long r = Long.parseLong(a);		
    		int id = mapper.getId(r);
    		if (UsersTh.contains(id)) {
    			nNeg.add(id);
    		}
    		
    	}
    	
    	
    	int[] nodesPOS = new int[nPos.size()];
    	for (int i=0 ; i<nPos.size() ; i++) {
    		nodesPOS[i]=nPos.get(i);
    	}
    	
    	int[] nodesNEG = new int[nNeg.size()];
    	for (int i=0 ; i<nNeg.size() ; i++) {
    		nodesNEG[i]=nNeg.get(i);
    	}
    	System.out.println(nodesPOS.length);
    	System.out.println("Nodes created");
    	//KPP-Neg on Positive Users
    	List<DoubleValues> KPositive = KppNeg.searchBroker(gTh, nodesPOS, runner);
    	List<DoubleValues> KNegative = KppNeg.searchBroker(gTh, nodesNEG, runner);

    	System.out.println("KPPNeg Calculated");
    	
    	//Convert to HashMap
    	HashMap<Long,Double> MapKPos = new HashMap <Long,Double>();
    	HashMap<Long,Double> MapKNeg = new HashMap <Long,Double>();
    	
    	for (DoubleValues score:KPositive) {
    		MapKPos.put(mapper.getNode(score.index) , score.value);
    	}
    	
    	for (DoubleValues score:KNegative) {
    		MapKNeg.put(mapper.getNode(score.index) , score.value);
    	}
    	
    	//Sort and get Top 500
    	Map<Long, Double> SortedScorePos = sortByValue(MapKPos); 
        Map<Long, Double> SortedScoreNeg = sortByValue(MapKNeg); 
        
        HashMap<Long, Double> KPPpos = new HashMap<Long, Double>();
        HashMap<Long, Double> KPPneg = new HashMap<Long, Double>();
        
        
        Set<Long> keysSP = SortedScorePos.keySet();
        Long[] keysSPArray = keysSP.toArray(new Long[keysSP.size()]);
        for(int i=0; i<keysSPArray.length && i<500;i++) {
            KPPpos.put(keysSPArray[i],(MapKPos.get(keysSPArray[i])));
        }
        
        Set<Long> keysSN = SortedScoreNeg.keySet();
        Long[] keysSNArray = keysSN.toArray(new Long[keysSN.size()]);
        for(int i=0; i<keysSNArray.length && i<500;i++) {
            KPPneg.put(keysSNArray[i],(MapKNeg.get(keysSNArray[i])));
        }
        
        Map<Long, Double> Mpos = sortByValue(KPPpos); 
        Map<Long, Double> Mneg = sortByValue(KPPneg); 
        
        System.out.println("TOP 500 KPP identified");
        
      //Write the TOP 500 with scores
        String Path_3=MainPartOne.resourcesPathPart1 + "Part_1_4/";
        BufferedWriter Scn = new BufferedWriter(new FileWriter(Path_3 + "KPP_Neg_scores.txt"));
        Iterator in = Mneg.entrySet().iterator();
        while (in.hasNext()) {
            Map.Entry pair = (Map.Entry)in.next();
            String ke =pair.getKey().toString();
            String va = pair.getValue().toString();
            String all= ke + ":" + va;
            Scn.write(all);
            Scn.newLine();
        }
        Scn.close();
        
        BufferedWriter Scp = new BufferedWriter(new FileWriter(Path_3 + "KPP_Pos_scores.txt"));
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
        
        BufferedWriter Scn2 = new BufferedWriter(new FileWriter(Path_3 + "KPP_Neg.txt"));
        in = Mneg.entrySet().iterator();
        while (in.hasNext()) {
            Map.Entry pair = (Map.Entry)in.next();
            String ke =pair.getKey().toString();
            Scn2.write(ke);
            Scn2.newLine();
        }
        Scn2.close();
        
        BufferedWriter Scp2 = new BufferedWriter(new FileWriter(Path_3 + "KPP_Pos.txt"));
        in = Mpos.entrySet().iterator();
        while (in.hasNext()) {
            Map.Entry pair = (Map.Entry)in.next();
            String ke =pair.getKey().toString();
            Scp2.write(ke);
            Scp2.newLine();
        }
        Scp2.close();
        
        System.out.println("Files saved");
    }
}
