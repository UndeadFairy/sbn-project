package angie;

import static org.apache.lucene.util.Version.LUCENE_41;
import angie.mainTemporalAnalysis;

import java.io.*;
import java.util.*;

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
    public static int runner = (int) (Runtime.getRuntime().availableProcessors());    // depends on pc
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

    private static Set<Integer> getLargestSet(Set<Set<Integer>> components) {
    	// return largest set from given set of sets
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

    public static int getCoocurrence(String tweet1, String tweet2, IndexReader indexReader) throws IOException, ParseException {
    	// Computing the weights - number of docs with shared word, returning top n hits to consider
        IndexSearcher searcher = new IndexSearcher(indexReader);
        Analyzer analyzer = new StandardAnalyzer(LUCENE_41);
        QueryParser parser = new QueryParser(LUCENE_41, "tweet", analyzer);
        Query searchQuery = parser.parse("+" + tweet1 + " +" + tweet2);
        TopDocs topHits = searcher.search(searchQuery, 5000000);
        return topHits.scoreDocs.length;
    }

    public static void generateCoocurenceGraph(String indexFolder, String cluster, String filename) throws IOException, ParseException, Exception {
    	File directoryIndex = new File(indexFolder);
        IndexReader indexReader = DirectoryReader.open(FSDirectory.open(directoryIndex));
        List<String> nodeA = new ArrayList<String>();
        List<String> nodeB = new ArrayList<String>();
        List<Integer> weight = new ArrayList<Integer>();
        List<Integer> clId = new ArrayList<Integer>();
        String[] rows = fileToArray(cluster);
        for (int i = 0; i < rows.length; i++) {
            String[] wordA = rows[i].split(" ");
            int clusterIdA = Integer.parseInt(wordA[1]);
            for (int j = i; j < rows.length; j++) {
                String[] wordB = rows[j].split(" ");
                int clusterIdB = Integer.parseInt(wordB[1]);
                // if same cluster and not equal words -> store it
                if (clusterIdB == clusterIdA && !wordA[0].equals(wordB[0])) {
                	// compute cooccurence of both words in the whole index
                    int count = getCoocurrence(wordA[0], wordB[0], indexReader);
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

    private static List<Integer> numberOfNodesInGraph(String graph) throws IOException {
    	// count number of nodes in a given graph by clusters
        String[] rows = fileToArray(graph);
        List<Integer> numberNodes = new ArrayList<Integer>();
        int n = rows.length;
        for (int c = 0; c < mainTemporalAnalysis.clusterCount; c++) {
            Set<String> words = new HashSet<String>();
            for (int i = 0; i < n; i++) {
                String[] line = rows[i].split(";");
                int clusterIdFromFile = Integer.parseInt(line[3]);
                if (clusterIdFromFile == c) {
                	// get both words
                    words.add(line[0]);
                    words.add(line[1]);
                }
            }
            // save their number
            numberNodes.add(words.size());
        }
        return numberNodes;
    }

    private static WeightedUndirectedGraph addNodes(WeightedUndirectedGraph graphUndirected, Integer k, String graph, NodesMapper<String> mapper) throws IOException {
        // read file with cooccurrences, get all the nodes and return the graph
        String[] rows = fileToArray(graph);
        int n = rows.length;
        for (int i = 0; i < n; i++) {
            // split the line in 4 parts: node1, node2, and weight, clusterid
            String[] line = rows[i].split(";");
            if (Integer.parseInt(line[3]) == k) {
                String nodeA = line[0];
                String nodeB = line[1];
                Double weight = Double.parseDouble(line[2]);
                // the graph is directed, add links in both ways
                graphUndirected.add(mapper.getId(nodeA) - 1, mapper.getId(nodeB) - 1, weight);
            }
        }
        return graphUndirected;
    }

    private static WeightedUndirectedGraph normalize(WeightedUndirectedGraph graphUndirected) {
        // normalize the weights of the edges to 0,1
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

    private static WeightedUndirectedGraph computeKcore(WeightedUndirectedGraph graphUndirected) throws InterruptedException {
    	// using stilo g library compute kcore and save the result
        WeightedUndirectedGraph graphUndirectedCopy = UnionDisjoint.copy(graphUndirected, runner);
        it.stilo.g.structures.Core core = CoreDecomposition.getInnerMostCore(graphUndirectedCopy, runner);
        graphUndirectedCopy = UnionDisjoint.copy(graphUndirected, runner);
        WeightedUndirectedGraph resultingGraph = SubGraph.extract(graphUndirectedCopy, core.seq, runner);
        return resultingGraph;
    }

    private static WeightedUndirectedGraph computeLargestCC(WeightedUndirectedGraph graphUndirected) throws InterruptedException {
    	// using stilo g library compute largest connected component and save the result
    	// prefill array with ints 0-size for rootedConnectedComponents
        int[] indices = new int[graphUndirected.size];
        for (int i = 0; i < graphUndirected.size; i++) {
        	indices[i] = i;
        }
        Set<Set<Integer>> connectedComponents = ConnectedComponents.rootedConnectedComponents(graphUndirected, indices, runner);
        Set<Integer> largestSet = getLargestSet(connectedComponents);
        int[] subnodes = new int[largestSet.size()];
        Iterator<Integer> iterator = largestSet.iterator();
        for (int j = 0; j < largestSet.size(); j++) {
            subnodes[j] = iterator.next();
        }

        WeightedUndirectedGraph resultingGraph = SubGraph.extract(graphUndirected, subnodes, runner);
        return resultingGraph;
    }

    private static void storeGraph(PrintWriter pw, NodesMapper<String> mapper, int[][] edges, int clusterID) throws IOException {
    	// writes a graph to a file in a format, 'word1 word2 clusterid'
        String tweet1 = "";
        String tweet2 = "";
        for (int i = 0; i < edges.length; i++) {
        	// only if there is an edge
            if (edges[i] != null) {
            	tweet1 = mapper.getNode(i + 1);
                for (int j = 0; j < edges[i].length; j++) {
                	tweet2 = mapper.getNode(edges[i][j] + 1);
                    pw.println(tweet1 + " " + tweet2 + " " + clusterID);
                }
            }
        }
    }

    public static void extractAll(String sentiment, double threshold) throws IOException, ParseException, Exception {
    	// aggregates calls to both kcore and largestcc, performs logging a saves outputs to files
        // Get the number of nodes inside each cluster
        List<Integer> numberNodes = numberOfNodesInGraph(mainTemporalAnalysis.resourcesPathPart0 +"graph_" + sentiment + ".txt");
        PrintWriter pwCC = new PrintWriter(new FileWriter(mainTemporalAnalysis.resourcesPathPart0 +"graph_" + sentiment + "_largestcc.txt"));
        PrintWriter pwKcore = new PrintWriter(new FileWriter(mainTemporalAnalysis.resourcesPathPart0 +"graph_" + sentiment + "_kcore.txt"));

        // create the array of graphs
        WeightedUndirectedGraph[] graphsArray = new WeightedUndirectedGraph[mainTemporalAnalysis.clusterCount];
        for (int i = 0; i < mainTemporalAnalysis.clusterCount; i++) {
            System.out.println("Cluster " + i + "just started");
            graphsArray[i] = new WeightedUndirectedGraph(numberNodes.get(i) + 1);

            NodesMapper<String> mapper = new NodesMapper<String>();
            graphsArray[i] = addNodes(graphsArray[i], i, mainTemporalAnalysis.resourcesPathPart0 +"graph_" + sentiment + ".txt", mapper);
            graphsArray[i] = normalize(graphsArray[i]);

            // remove edges where weight < threshold
            graphsArray[i] = SubGraphByEdgesWeight.extract(graphsArray[i], threshold, 2);

            WeightedUndirectedGraph largestCCGraph = computeLargestCC(graphsArray[i]);
            storeGraph(pwCC, mapper, largestCCGraph.in, i);

            WeightedUndirectedGraph kcoreGraph = computeKcore(graphsArray[i]);
            storeGraph(pwKcore, mapper, kcoreGraph.in, i);
        }
        pwCC.close();
        pwKcore.close();
    }
    
    public static void mainGraphs() throws IOException, ParseException, Exception {
    	generateCoocurenceGraph(mainTemporalAnalysis.resourcesPathPart0 +"index_tweets_positive", mainTemporalAnalysis.resourcesPathPart0 +"clusters_positive.txt", mainTemporalAnalysis.resourcesPathPart0 +"graph_positive.txt");
    	generateCoocurenceGraph(mainTemporalAnalysis.resourcesPathPart0 +"index_tweets_negative", mainTemporalAnalysis.resourcesPathPart0 +"clusters_negative.txt", mainTemporalAnalysis.resourcesPathPart0 +"graph_negative.txt");        
    	extractAll("positive", 0.05);
    	extractAll("negative", 0.05);
    }
    
    public static void main(String[] args) throws IOException, ParseException, Exception {
    	mainGraphs();
    }
}