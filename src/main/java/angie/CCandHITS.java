package angie;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import it.stilo.g.algo.ConnectedComponents;
import it.stilo.g.algo.HubnessAuthority;
import it.stilo.g.algo.SubGraph;
import it.stilo.g.structures.DoubleValues;
import it.stilo.g.structures.WeightedDirectedGraph;
import it.stilo.g.util.GraphWriter;
import it.stilo.g.util.NodesMapper;
import angie.MainPartOne;
import angie.mainSpreadOfInfluence;

public class CCandHITS {
	public static void CCHits(String[] args) throws IOException, InterruptedException
    {
        int runner = (int) (Runtime.getRuntime().availableProcessors()); 

		//Read Politicians UserName List 
        Scanner Ids = new Scanner(new File(MainPartOne.resourcesPathPart1 + "Part_1_1/M.txt"));

        ArrayList<String> id_mention = new ArrayList<String>();
        while (Ids.hasNext()) {
        	id_mention.add(Ids.next());
        }
        Ids.close();
    	System.out.println("User Ids Extreaction: DONE");
    	
    	ArrayList<String> id_mentions = new ArrayList<String>();
    	for (String s:id_mention) {
    		s = s.replaceAll("^(['\"])(.*)\\1$", "$2");
    		id_mentions.add(s);
    	}
    	
    	//Read graph
    	String graph_path= mainSpreadOfInfluence.givenDataResourcesPath + "Official_SBN-ITA-2016-Net";
    	
    	BufferedReader br = null;
        FileReader fr = null;

		fr = new FileReader(graph_path);
		br = new BufferedReader(fr);
    	
    	ArrayList<ArrayList>edges=new ArrayList<ArrayList>();
    	NodesMapper<Long> mapper = new NodesMapper<Long>();
    	ArrayList<Double>weights=new ArrayList<Double>();
    	HashSet<Integer>size=new HashSet<Integer>();
    	
    	String Line;
    
    	while ((Line = br.readLine()) != null) {
    		String[] L = Line.split("\t");

    		
    		ArrayList e=new ArrayList<>();
    		
    		Long a = Long.parseLong(L[0]);
    		Long b = Long.parseLong(L[1]);
    		Double w = Double.parseDouble(L[2]);
    		
    		//This condition checks that the subgraph exists, if both source and destination
    		//are mentioned in the user list 
    		if(id_mentions.contains(L[0]) && id_mentions.contains(L[1])) {
    			System.out.println("Edge created");
    			//Map IDs
                e.add(mapper.getId(a)); 
                e.add(mapper.getId(b)); 
                
                //Weights vector
                weights.add(w); 
                
    			
        		
        		//Get the size of the graph
        		size.add(mapper.getId(a));
        		size.add(mapper.getId(b));
        		
        		//Get edges vector
        		edges.add(e);
    		}
    		
    	}
    	br.close();
		fr.close();
		
		System.out.println("Edge Creation: DONE");
		int S=size.size();
		//Create graph
		WeightedDirectedGraph g = new WeightedDirectedGraph(S+1);
		
		int Source; 
        int Dest;
        double Weight; 
    	ArrayList<Integer>edges_2=new ArrayList<Integer>();
    	
        for(int i = 0; i < edges.size(); i++) {
        	
            ArrayList e = edges.get(i);
            Object[] arr = new Object[3];
            Source = (int) e.get(0); 
            Dest = (int) e.get(1); 
            Weight = (double) weights.get(i); 
            g.add(Source, Dest, Weight);
            edges_2.add(Source);
            edges_2.add(Dest);
        }
        System.out.println("Graph: DONE");
        //Save the graph
        //Save document with the users that mention a politician
        
        GraphWriter.saveDirectGraph(g, MainPartOne.resourcesPathPart1 + "Part_1_2/subgraph", null);
        
        System.out.println("Subgraph g creation: DONE");
        
        
    	//Find the Connected Components
    	
        //The maximum is set to two as is the minimum size of a possible connected component
        int max=2;
        Set<Integer> LargestC = new HashSet<>();
        for (int i=0 ; i<S; i++) {
        	int[] nodes = {i};
        	//Get all the connected components
            Set<Set<Integer>> Components = ConnectedComponents.rootedConnectedComponents(g, nodes , runner);
            //Find the largest connected component
            for(Set<Integer> c: Components) {
                if(c.size()>max) {
                	LargestC = c;
                	max = c.size();
                	System.out.println(c.size());
                } 
            }
        }
        System.out.print("Largest component found");
        System.out.println("\n");
    	//Create subgraph of the Largest Connected Component
        
        //Create the nodes [] needed for extract the subgraph
        int [] LCCnodes = new int[max];
        ArrayList<Integer> LCC = new ArrayList<Integer>(LargestC);
        for (int i=0 ; i<max; i++) {
        	LCCnodes[i] = LCC.get(i);
        }
        BufferedWriter LCCids = new BufferedWriter (new FileWriter(MainPartOne.resourcesPathPart1 + "Part_1_2/LCC_users_id.txt"));
        for(int it:LCCnodes) {
        	LCCids.write(mapper.getNode(it).toString());
        	LCCids.newLine();
        }
        LCCids.close();
        System.out.print("Subgraph with the Largest Connected Component created");
        System.out.println("\n");
        //Create Subgraph
        WeightedDirectedGraph g_2;
        g_2=SubGraph.extract(g, LCCnodes, runner);
        
        GraphWriter.saveDirectGraph(g_2, MainPartOne.resourcesPathPart1 + "Part_1_2/subgraphLCC", null);
        
        System.out.println("Subgraph g creation: DONE");
        
        //Run HITS algorithm
        Double th = 0.00001;
        
        ArrayList<ArrayList<DoubleValues>> HITS;
        
        HITS = HubnessAuthority.compute(g_2,th,runner);
        System.out.print("HITS computed");
        
        //Get Authority and Hub scores Separately
        
        ArrayList<DoubleValues>Auth = HITS.get(0);
        ArrayList<DoubleValues>Hub = HITS.get(1);
        
        //Save the Authorities
        BufferedWriter AuthIds = new BufferedWriter(new FileWriter(MainPartOne.resourcesPathPart1 + "Part_1_2/AuthoritiesWithScore.txt"));
        for (DoubleValues auth:Auth) {
        	AuthIds.write(mapper.getNode(auth.index) + "\t" + auth.value);
        	AuthIds.newLine();
        }
        AuthIds.close();
        
        //Save the Hubs
        BufferedWriter HubIds = new BufferedWriter(new FileWriter(MainPartOne.resourcesPathPart1 + "Part_1_2/HubsWithScore.txt"));
        for (DoubleValues hub:Hub) {
        	HubIds.write(mapper.getNode(hub.index) + "\t" + hub.value);
        	HubIds.newLine();
        }
        HubIds.close();
        
    }
	
	


}
