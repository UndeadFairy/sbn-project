package angie;

import it.stilo.g.structures.LongIntDict;
import it.stilo.g.structures.WeightedDirectedGraph;
import it.stilo.g.util.GraphReader;
import java.io.*;
import java.util.*;
import java.util.function.ToIntFunction;
import org.apache.commons.lang3.ArrayUtils;

public class SpreadAnalysis {

    public static int getRandomElementOfArray(int[] array) {
    	// returns a random element from an array
        int rnd = new Random().nextInt(array.length);
        return array[rnd];
    }

    public static void shuffleArrayInPlace(int[] arrayToShuffle) {
        // shuffles an array randomly in place
        Random random = new Random();
        for (int i = arrayToShuffle.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            if (index != i) {
                int a = arrayToShuffle[index];
                arrayToShuffle[index] = arrayToShuffle[i];
                arrayToShuffle[index] = a;
            }
        }
    }

    public static int getMostFrequent(int[] elements) {
    	// returns most frequent number in an array, if two, get randomly one of them 
    	shuffleArrayInPlace(elements);
        int c = 0;
        int tempC = 0;
        int mostFreq = getRandomElementOfArray(elements);
        int temp = getRandomElementOfArray(elements);
        for (int i = 0; i < (elements.length - 1); i++) {
            tempC = 0;
            temp = elements[i];
            for (int j = 1; j < elements.length; j++) {
                if (temp == elements[j]) {
                	tempC++;
                }
            }
            if (tempC > c) {
            	mostFreq = temp;
                c = tempC;
            }
        }
        return mostFreq;
    }
    
    private static void printList(String filename, ArrayList<Integer> list) throws IOException {
    	// saves an arraylist line by line to a text file
        FileWriter writer = new FileWriter(filename);
        for (int i = 0; i < list.size(); i++) {
        	writer.write(list.get(i).toString());
            writer.write("\n");
        }
        writer.close();
    }

    private static List<String> readList(String file) throws IOException {
    	// helper function reading text file line by line, saving strings to list
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> rows = new ArrayList<String>();
        String row = null;
        // while not EOF, write to list
        while ((row = bufferedReader.readLine()) != null) {
            rows.add(row);
        }
        bufferedReader.close();
        return rows;
    }
    
    private static int[] loadSeed(LongIntDict mapLong2Int, String filename) throws IOException {
        List<String> seed = readList(filename);
        // save ids after mapping
        int[] seeds = new int[seed.size()];

        // map it into the graph IDs
        for (int i = 0; i < seed.size(); i++) {
            seeds[i] = mapLong2Int.get(Long.parseLong(seed.get(i)));
        }
        return seeds;
    }

    public static void spreadOfInfluenceAlgorithm(WeightedDirectedGraph graph, int[] seedsPositive, int[] seedsNegative, String type, int threshold) throws IOException {
        ArrayList<Integer> tmp = new ArrayList<Integer>();
        for (int i = 0; i < graph.size; i++) {
            if ((ArrayUtils.contains(seedsPositive, i) == false) && (ArrayUtils.contains(seedsNegative, i) == false)) {
                tmp.add(i);
            }
        }
        // stream list of nodes not belonging to any identified group
        int[] seedsUnknown = tmp.stream().mapToInt(new ToIntFunction<Integer>() {
        	// should be lambda but now it runs in java 1.7
			@Override
			public int applyAsInt(Integer i) {
				return i;
			}
		}).toArray();

        // Set the labels
        int[] labels = new int[graph.size];
        for (int i = 0; i < seedsPositive.length; i++) {
            labels[seedsPositive[i]] = 1;
        }
        for (int i = 0; i < seedsNegative.length; i++) {
            labels[seedsNegative[i]] = 2;
        }
        for (int i = 0; i < seedsUnknown.length; i++) {
            labels[seedsUnknown[i]] = 404;
        }

        int[] nodes = new int[labels.length];
        for (int i = 0; i < graph.size; i++) {
        	// workaround by creating artificial ids, later replaced
            nodes[i] = i;
        }

        Map<Integer, Integer> nodesLabels = new HashMap<>();
        for (int i = 0; i < nodes.length; i++) {
            int nodesTmp = nodes[i];
            int labelsTmp = labels[i];
            nodesLabels.put(nodesTmp, labelsTmp);
        }

        // tracking changes in the results (convergence)
        int counterConvergence = 0;
        int counter = 0;

        // In every loop, store the progress further
        ArrayList<Integer> counterMidPositive = new ArrayList<Integer>();
        ArrayList<Integer> counterMidNegative = new ArrayList<Integer>();
        ArrayList<Integer> counterMidUnknown = new ArrayList<Integer>();
        counterMidPositive.add(seedsPositive.length);
        counterMidNegative.add(seedsNegative.length);
        counterMidUnknown.add(seedsUnknown.length);

        // End code run if run threshold exceeded or no change anymore
        boolean stop = false;
        int newLabelNode;
        while (stop == false) {
            counter++;
            counterConvergence = 0;
            shuffleArrayInPlace(nodes);

            System.out.println("Iteration " + counter + " counting...");
            int lenthis = counterMidPositive.size() - 1;
            System.out.println("Positive labels: " + counterMidPositive.get(lenthis));
            lenthis = counterMidNegative.size() - 1;
            System.out.println("Negative labels: " + counterMidNegative.get(lenthis));
            lenthis = counterMidUnknown.size() - 1;
            System.out.println("Unknown labels: " + counterMidUnknown.get(lenthis));
            System.out.println("");

            // start the counters for the new iteration
            counterMidPositive.add(0);
            counterMidNegative.add(0);
            counterMidUnknown.add(0);

            for (int k = 0; k < nodes.length; k++) {
                int node = nodes[k];
                // for each node, identify the neighbours, get their labels 
                // and change node label if neighbours positive or negative, otherwise keep
                int[] neighbours = graph.in[node];
                newLabelNode = nodesLabels.get(node);
                if (neighbours != null) {
                    int[] neighbourLabels = new int[neighbours.length];
                    for (int j = 0; j < neighbours.length; j++) {
                        int nj = neighbours[j];
                        int tmpLabel = nodesLabels.get(nj);
                        neighbourLabels[j] = tmpLabel;
                    }
                    ArrayList<Integer> neighbourLabelsYesNo = new ArrayList<Integer>();
                    for (int m = 0; m < neighbourLabels.length; m++) {
                        int neighbourLabelsnl = neighbourLabels[m];
                        if (neighbourLabelsnl == 1 || neighbourLabelsnl == 2) {
                            neighbourLabelsYesNo.add(neighbourLabelsnl);
                        }
                    }
                    if (neighbourLabelsYesNo.size() > 0) {
                        int[] neighbourLabelsYesNoArray = neighbourLabelsYesNo.stream().mapToInt(new ToIntFunction<Integer>() {
							@Override
							public int applyAsInt(Integer i) {
								return i;
							}
						}).toArray();
                        newLabelNode = getMostFrequent(neighbourLabelsYesNoArray);
                    } else {
                        newLabelNode = nodesLabels.get(node);
                    }
                }
                // check if labels are the same - terminating condition
                if (nodesLabels.get(node).equals(newLabelNode)) {
                    counterConvergence++;
                }
                // Replace the label
                nodesLabels.replace(node, newLabelNode);

                // modify temporal counters
                if (newLabelNode == 1) {
                    int len = counterMidPositive.size() - 1;
                    int counterLast = counterMidPositive.get(len);
                    counterMidPositive.remove(len);
                    counterMidPositive.add(counterLast + 1);
                } else if (newLabelNode == 2) {
                    int len = counterMidNegative.size() - 1;
                    int counterLast = counterMidNegative.get(len);
                    counterMidNegative.remove(len);
                    counterMidNegative.add(counterLast + 1);
                } else {
                    int len = counterMidUnknown.size() - 1;
                    int counterLast = counterMidUnknown.get(len);
                    counterMidUnknown.remove(len);
                    counterMidUnknown.add(counterLast + 1);
                }
            }
            // check convergence
            if (counterConvergence == nodes.length) {
                System.out.println("Converged " + counter);
                stop = true;
            }
            if (counter >= threshold) {
                System.out.println("Did not reach the terminating condition. Step: " + counter);
                stop = true;
            }
        }

        int countPositive = 0;
        int countNegative = 0;
        int countUnknown = 0;
        for (int i = 0; i < nodes.length; i++) {
            if (nodesLabels.get(nodes[i]).equals(1)) {
            	countPositive++;
            } else if (nodesLabels.get(nodes[i]).equals(2)) {
            	countNegative++;
            } else {
            	countUnknown++;
            }
        }
        // print final results
        System.out.println("Positive: " + countPositive);
        System.out.println("Negative: " + countNegative);
        System.out.println("Unknown: " + countUnknown);
        printList(mainSpreadOfInfluence.resourcesPathPart2 + "positive_labels_temporal_counter" + type + ".txt", counterMidPositive);
        printList(mainSpreadOfInfluence.resourcesPathPart2 + "negative_labels_temporal_counter" + type + ".txt", counterMidNegative);
        printList(mainSpreadOfInfluence.resourcesPathPart2 + "unknown_labels_temporal_counter" + type + ".txt", counterMidUnknown);
    }

    public static void mainSpreadAnalysis() throws FileNotFoundException, IOException, org.apache.lucene.queryparser.classic.ParseException, InterruptedException {
        // load given graph
        int graphSize = 16815933;
        WeightedDirectedGraph graph = new WeightedDirectedGraph(graphSize + 1);
        LongIntDict mapLong2Int = new LongIntDict();
        GraphReader.readGraphLong2IntRemap(graph, mainSpreadOfInfluence.givenDataResourcesPath + "Official_SBN-ITA-2016-Net.gz", mapLong2Int, false);
        
        // run for M
        int[] seedsPositiveM = loadSeed(mapLong2Int, mainSpreadOfInfluence.resourcesPathPart2 + "input/MPos.txt");
        int[] seedsNegativeM = loadSeed(mapLong2Int, mainSpreadOfInfluence.resourcesPathPart2 + "input/MNeg.txt");
        spreadOfInfluenceAlgorithm(graph, seedsPositiveM, seedsNegativeM, "_M_", 40);
        
        // run for M'
        int[] seedsPositiveMSel = loadSeed(mapLong2Int, mainSpreadOfInfluence.resourcesPathPart2 + "input/M_2Pos.txt");
        int[] seedsNegativeMSel = loadSeed(mapLong2Int, mainSpreadOfInfluence.resourcesPathPart2 + "input/M_2Neg.txt");
        spreadOfInfluenceAlgorithm(graph, seedsPositiveMSel, seedsNegativeMSel, "_M2_", 40);
        
        //run for top k players TODO Change name of file input
//        int[] seedsPositiveTopK = loadSeed(mapLong2Int, mainSpreadOfInfluence.resourcesPathPart2 + "input/M_2Pos.txt");
//        int[] seedsNegativeTopK = loadSeed(mapLong2Int, mainSpreadOfInfluence.resourcesPathPart2 + "input/M_2Neg.txt");
//        spreadOfInfluenceAlgorithm(graph, seedsPositiveTopK, seedsNegativeTopK, "_TopK_", 40);
    }
    
    public static void main(String[] args) throws FileNotFoundException, IOException, org.apache.lucene.queryparser.classic.ParseException, InterruptedException {
    	mainSpreadAnalysis();
    }
}
