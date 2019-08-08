package angie;

import static org.apache.lucene.util.Version.LUCENE_41;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.core.Core;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import com.google.common.util.concurrent.AtomicDouble;

import it.stilo.g.algo.ConnectedComponents;
import it.stilo.g.algo.CoreDecomposition;
import it.stilo.g.algo.GraphInfo;
import it.stilo.g.algo.SubGraph;
import it.stilo.g.algo.SubGraphByEdgesWeight;
import it.stilo.g.algo.UnionDisjoint;
import it.stilo.g.structures.WeightedUndirectedGraph;
import it.stilo.g.util.NodesMapper;

public class Graphs {

    public static void writeCoocurrenceGraph(List<String> nodeA, List<String> nodeB, List<Integer> weight, List<Integer> clId, String fileName) {
    	// writes coocurrence graph in a given schema - node;node;weight;cluster id 
        try {
            FileWriter fileWriter = new FileWriter(fileName);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            PrintWriter pw = new PrintWriter(bufferedWriter);
            for (int i = 0; i < nodeA.size(); i++) {
                if (nodeA.get(i) != null) {
                	pw.write(nodeA.get(i) + ";" + nodeB.get(i) + ";" + weight.get(i) + ";" + clId.get(i));
                }
                pw.write("\n");
            }
            pw.close();
        } catch (IOException error) {
            System.out.println(error);
        }
    }

    public static String[] fileToArray(String file) throws IOException {
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> rows = new ArrayList<String>();
        String row = null;
        // while not EOF, write to array
        while ((row = bufferedReader.readLine()) != null) {
            rows.add(row);
        }
        bufferedReader.close();
        String [] result = rows.toArray(new String[rows.size()]);
        return result;
    }

    public static int countCoocurrence(String tweet1, String tweet2, IndexReader indexReader) throws IOException, ParseException {
    	// Computing the weights - number of docs with shared word
        IndexSearcher searcher = new IndexSearcher(indexReader);
        Analyzer analyzer = new StandardAnalyzer(LUCENE_41);
        QueryParser parser = new QueryParser(LUCENE_41, "tweet", analyzer);
        Query searchQuery = parser.parse("+" + tweet1 + " +" + tweet2);
        TopDocs topHits = searcher.search(searchQuery, 5000000);
        ScoreDoc[] scoreDocs = topHits.scoreDocs;
        return scoreDocs.length;
    }

    public static void generateGraph(String cluster, String indexFolder, String filename) throws IOException, ParseException, Exception {
        File directoryIndex = new File(indexFolder);
        IndexReader indexReader = DirectoryReader.open(FSDirectory.open(directoryIndex));
        List<String> nodeA = new ArrayList<String>();
        List<String> nodeB = new ArrayList<String>();
        List<Integer> weight = new ArrayList<Integer>();
        List<Integer> clId = new ArrayList<Integer>();
        String[] rows = fileToArray(cluster);
        for (int i = 0; i < rows.length; i++) {
        	// DELETE LATER
        	if (i % 10 == 0) {
                System.out.println(i + "lines !!!");
        	}
            String[] wordA = rows[i].split(" ");
            int clusterIdA = Integer.parseInt(wordA[1]);
            for (int j = i; j < rows.length; j++) {
                String[] wordB = rows[j].split(" ");
                int clusterIdB = Integer.parseInt(wordB[1]);
                // if same cluster and not equal words -> store it
                if (clusterIdB == clusterIdA && !wordA[0].equals(wordB[0])) {
                    int count = countCoocurrence(wordA[0], wordB[0], indexReader);
                    if (count > 0) {
                        nodeA.add(wordA[0]);
                        nodeB.add(wordB[0]);
                        weight.add(count);
                        clId.add(clusterIdB);
                    }
                }
            }
        }	
        writeCoocurrenceGraph(nodeA, nodeB, weight, clId, filename);
    }

    public static void main(String[] args) throws IOException, ParseException, Exception {
        //generateGraph("src/main/resources/clusters_positive.txt", "src/main/resources/index_tweets_positive",  "src/main/resources/graph_positive.txt");
        //generateGraph("src/main/resources/clusters_negative.txt",  "src/main/resources/index_tweets_negative", "src/main/resources/graph_negative.txt");
    
        
    	int runner = (int) (Runtime.getRuntime().availableProcessors());
        System.out.println(runner);
        
        extractKCoreAndConnectedComponent(0.07);
    }
    
    // depends on pc
    public static int runner = (int) (Runtime.getRuntime().availableProcessors());


    private static List<Integer> numberOfNodesInGraph(String graph, int k) throws IOException {
    
        String[] rows = fileToArray(graph);
        List<Integer> numberNodes = new ArrayList<Integer>();
        int n = rows.length;
        for (int cIter = 0; cIter < k; cIter++) {
            Set<String> words = new HashSet<String>();
            for (int i = 0; i < n; i++) {
                String[] line = rows[i].split(";");
                int cc = Integer.parseInt(line[3]);
                if (cc == cIter) {
                    words.add(line[0]);
                    words.add(line[1]);
                }
            }
            numberNodes.add(words.size());
        }
        return numberNodes;
    }

    private static WeightedUndirectedGraph addNodesGraph(WeightedUndirectedGraph graphUndirected, int k, String graph, NodesMapper<String> mapper) throws IOException {
        // add the nodes from the a file created with coocurrencegraph.java, and returns the graph
        //ReadFile rf = new ReadFile();
        String[] rows = fileToArray(graph);

        // map the words into id for g stilo
        //NodesMapper<String> mapper = new NodesMapper<String>();
        // creathe the graph
        // keep in mind that the id of a word is mapper.getId(s1) - 1 (important the -1)
        int n = rows.length;
        for (int i = 0; i < n; i++) {
            // split the line in 3 parts: node1, node2, and weight
            String[] line = rows[i].split(";");
            if (Integer.parseInt(line[3]) == k) {
                String nodeA = line[0];
                String nodeB = line[1];
                Double weight = Double.parseDouble(line[2]);
                // the graph is directed, add links in both ways
                graphUndirected.add(mapper.getId(nodeA) - 1, mapper.getId(nodeB) - 1, weight);
                //g.add(mapper.getId(node2) - 1, mapper.getId(node1) - 1, w);
            }

        }
        return graphUndirected;
    }

    private static WeightedUndirectedGraph normalizeGraph(WeightedUndirectedGraph graphUndirected) {
        // normalize the weights of the edges
        double sum = 0;
        for (int i = 0; i < graphUndirected.size - 1; i++) {
            sum = 0;
            for (int j = 0; j < graphUndirected.weights[i].length; j++) {
                sum = sum + graphUndirected.weights[i][j];
            }
            // standard normalize
            for (int j = 0; j < graphUndirected.weights[i].length; j++) {
            	graphUndirected.weights[i][j] = graphUndirected.weights[i][j] / sum;
            }
        }
        return graphUndirected;
    }

    private static WeightedUndirectedGraph kcore(WeightedUndirectedGraph graphUndirected) throws InterruptedException {
        WeightedUndirectedGraph graphUndirectedCopy = UnionDisjoint.copy(graphUndirected, runner);
        it.stilo.g.structures.Core cc = CoreDecomposition.getInnerMostCore(graphUndirectedCopy, runner);
        System.out.println("Kcore");
        System.out.println("Minimum degree: " + cc.minDegree);
        System.out.println("Vertices: " + cc.seq.length);
        System.out.println("Seq: " + cc.seq);

        graphUndirectedCopy = UnionDisjoint.copy(graphUndirected, runner);
        WeightedUndirectedGraph s = SubGraph.extract(graphUndirectedCopy, cc.seq, runner);
        return s;
    }

    private static Set<Integer> getLargestSet(Set<Set<Integer>> components) {
        int m = -1;
        Set<Integer> largestSet = new HashSet<>();
        for (Set<Integer> innerSet : components) {
            if (innerSet.size() > m) {
            	largestSet = innerSet;
                m = innerSet.size();
            }
        }
        return largestSet;
    }

    private static WeightedUndirectedGraph getLargestCC(WeightedUndirectedGraph graphUndirected) throws InterruptedException {
        // this get the largest component of the graph and returns a graph too
        //System.out.println(Arrays.deepToString(g.weights));
        int[] indices = new int[graphUndirected.size];
        for (int i = 0; i < graphUndirected.size; i++) {
        	indices[i] = i;
        }
        Set<Set<Integer>> connectedComponents = ConnectedComponents.rootedConnectedComponents(graphUndirected, indices, runner);
        Set<Integer> largestSet = getLargestSet(connectedComponents);
        int[] subnodes = new int[largestSet.size()];
        Iterator<Integer> iterator = largestSet.iterator();
        for (int j = 0; j < subnodes.length; j++) {
            subnodes[j] = iterator.next();
        }

        WeightedUndirectedGraph s = SubGraph.extract(graphUndirected, subnodes, runner);
        return s;
    }

    /* 
    iterate through all the edges, recovering the terms.
    'edges' is a matrix, in which each row is a termID1, and in each column is 
    another termID2 that has an edge with termID1. 
    Ex:
    [0] = [1, 5, 6]
    [1] = [0, 8]
    ...
    Map back each termID to the term string and save to the edges in the following
    format:
    term1 term2 clusterID
     */
    private static void saveGraphToFile(PrintWriter pw, NodesMapper<String> mapper, int[][] edges, int clusterID) throws IOException {
        String term1 = "", term2 = "";

        for (int i = 0; i < edges.length; i++) {
            if (edges[i] != null) {
                term1 = mapper.getNode(i + 1);
                for (int j = 0; j < edges[i].length; j++) {
                    term2 = mapper.getNode(edges[i][j] + 1);
                    pw.println(term1 + " " + term2 + " " + clusterID);
                }
            }
        }
    }

    public static void extractKCoreAndConnectedComponent(double threshold) throws IOException, ParseException, Exception {

        // do the same analysis for the yes-group and no-group
        String[] prefixYesNo = {"positive", "negative"};
        for (String prefix : prefixYesNo) {

            int k = 4;

            // Get the number of nodes inside each cluster
            List<Integer> numberNodes = numberOfNodesInGraph("src/main/resources/graph_" + prefix + ".txt", k);

            PrintWriter pw_cc = new PrintWriter(new FileWriter("src/main/resources/graph_" + prefix + "_largestcc.txt")); //open the file where the largest connected component will be written to
            PrintWriter pw_kcore = new PrintWriter(new FileWriter("src/main/resources/graph_" + prefix + "_kcore.txt")); //open the file where the kcore will be written to

            // create the array of graphs
            WeightedUndirectedGraph[] gArray = new WeightedUndirectedGraph[k];
            for (int i = 0; i < k; i++) {
                System.out.println();
                System.out.println("Cluster " + i);

                gArray[i] = new WeightedUndirectedGraph(numberNodes.get(i) + 1);

                // Put the nodes,
                NodesMapper<String> mapper = new NodesMapper<String>();
                gArray[i] = addNodesGraph(gArray[i], i, "src/main/resources/graph_" + prefix + ".txt", mapper);

                //normalize the weights
                gArray[i] = normalizeGraph(gArray[i]);

                AtomicDouble[] info = GraphInfo.getGraphInfo(gArray[i], 1);
                System.out.println("Nodes:" + info[0]);
                System.out.println("Edges:" + info[1]);
                System.out.println("Density:" + info[2]);

                // extract remove the edges with w<t
                gArray[i] = SubGraphByEdgesWeight.extract(gArray[i], threshold, 1);

                // get the largest CC and save to a file
                WeightedUndirectedGraph largestCC = getLargestCC(gArray[i]);
                saveGraphToFile(pw_cc, mapper, largestCC.in, i);

                // Get the inner core and save to a file
                WeightedUndirectedGraph kcore = kcore(gArray[i]);
                saveGraphToFile(pw_kcore, mapper, kcore.in, i);
            }
            pw_cc.close();
            pw_kcore.close();
        }
    }
}