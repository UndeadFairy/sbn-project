package angie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.lang.StringBuilder;

public class KMeansAlgorithm {

    private final HashMap<Character, Integer> ci;
    private final HashMap<Integer, Character> ic;
    private final HashMap<String, Integer> saxCluster;
    private final ArrayList<String> saxStrings;
    private int k = mainTemporalAnalysis.clusterCount;
    private final int alphabetSize;
    
	public KMeansAlgorithm(int k, int alphabetSize, ArrayList<String> saxStrings) {
    	// constructor of the class
    	HashMap<Character, Integer> charInt = new HashMap<Character, Integer>();
	    HashMap<Integer, Character> intChar = new HashMap<Integer, Character>();
	    int value = 0;
    	for (int c = 97; c < 123; c++) {
    		charInt.put((char) c, value);
    		intChar.put(value, (char) c);
    		value++;
        }

        this.ci = charInt;
        this.ic = intChar;
        this.saxCluster = new HashMap<>();
        this.k = k;
        this.alphabetSize = alphabetSize;
        this.saxStrings = saxStrings;
      }

    public char calculateAvgChar(char[] chars) {
    	// distance is a difference between ascii value
    	// get average value of a given word
        int avgChar = 0;
        for (int i = 0; i < chars.length; i++) {
        	avgChar += this.ci.get(chars[i]);
        }
        avgChar = Math.round(avgChar / chars.length);
        return this.ic.get(avgChar);
    }

    private String generateRandomStrings() {
    	// helper function creating random strings from given alphabet
    	String lettersToChooseFrom = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int stringSize = this.saxStrings.get(0).length();
        int usedCharRange = this.alphabetSize;
        if (usedCharRange > lettersToChooseFrom.length()) {
        	// if exceed, set to max size
        	usedCharRange = lettersToChooseFrom.length();
        }
    	StringBuilder stringBuilder = new StringBuilder();
    	for(int i = 0; i < stringSize; i++) {
    		int character = (int) Math.round(Math.random() * usedCharRange);
    		stringBuilder.append(lettersToChooseFrom.charAt(character));
    	}
    	return stringBuilder.toString();
    }

    private String[] generateRandomCentroids() {
    	// for each cluster, create a random centroid
        String[] centroids = new String[this.k];
        for (int i = 0; i < this.k; i++) {
            centroids[i] = generateRandomStrings();
        }
        return centroids;
    }

    private String[] getCentroids(ArrayList<LinkedHashSet<String>> clusters) {
    	// for each cluster, creates a centroid
        String[] centroids = new String[this.k];
        for (int i = 0; i < this.k; i++) {
        	LinkedHashSet<String> cluster = clusters.get(i);
        	ArrayList<String> clusterArr = new ArrayList<>(cluster);
            String centroid = "";
            // If cluster is empty (bad random centroid), no update, continue with a loop.
            if (cluster.isEmpty()) {
            	centroids[i] = generateRandomStrings();
            	continue;
            }

            int saxStringSize = 0;
            for (String saxString : cluster) {
            	// because it has no get first method
                saxStringSize = saxString.length();
                break;
            }
            // iterate through all the SAX strings, get the average char for each position 
            for (int j = 0; j < saxStringSize; j++) {
                char[] chars = new char[cluster.size()];
                // characters being read, then avg'd
                for (int k = 0; k < clusterArr.size(); k++) {
                    chars[k] = clusterArr.get(k).charAt(j);
                }
                centroid += calculateAvgChar(chars);
            }
            centroids[i] = centroid;
        }
        return centroids;
    }

    private void updateCluster(ArrayList<LinkedHashSet<String>> clusters, String saxString, int newClusterID) {
        // check if the sax string was already put in another cluster - if yes, remove it and add it into new with new id
    	// this is done in order to avoid that the algorithm would stuck
        if (this.saxCluster.containsKey(saxString)) {
            int oldClusterID = this.saxCluster.get(saxString);
            if (oldClusterID != newClusterID) {
                clusters.get(oldClusterID).remove(saxString);
            }
        }
        // add saxString to the new cluster
        LinkedHashSet<String> cluster = clusters.get(newClusterID);  // load the cluster 'newClusterID'
        cluster.add(saxString);  // add the sax string in position i
        this.saxCluster.put(saxString, newClusterID); //update the cluster ID of the sax string in the map
    }

    public HashMap<String, Integer> getClusters() throws Exception {
    	// returns an array with clusters of saxStrings
        int listSize = saxStrings.size();
        int totalErrorOld = 0;
        int totalErrorNew = 0;
        String[] centroids;
        ArrayList<LinkedHashSet<String>> clusters = new ArrayList<>();
        // create empty holding stucture
        for (int i = 0; i < k; i++) {
            clusters.add(new LinkedHashSet<String>());
        }
        centroids = generateRandomCentroids();
        do {
            totalErrorOld = totalErrorNew;
            totalErrorNew = 0;

            // go through all saxStrings checking the distance
            for (int i = 0; i < listSize; i++) {
                String saxString = this.saxStrings.get(i);
                Integer minDistance = Integer.MAX_VALUE;
                int newClusterID = 0;
                // iterate through all centroids
                for (int clusterID = 0; clusterID < k; clusterID++) {
                	char[] charsSax = saxString.toCharArray();
                	char[] centroidSax = centroids[clusterID].toCharArray();
                	if (charsSax.length != centroidSax.length) {
                        throw new Exception("Sizes of saxStrings do not match! Something wicked!");
                    }

                    int newDistance = 0;
                    for (int j = 0; j < charsSax.length; j++) {
                    	newDistance += Math.abs(centroidSax[j] - charsSax[j]);
                    }
                    // sax string will be added to the cluster with minimum distance from its centroid to the SAX String
                    if (newDistance < minDistance) {
                        minDistance = newDistance;
                        newClusterID = clusterID;
                    }
                }
                totalErrorNew += minDistance; // update the total error
                updateCluster(clusters, saxString, newClusterID);
            }
            centroids = getCentroids(clusters); // calculate the centroids for the next iteration
        } while (totalErrorOld != totalErrorNew); // nothing to change further

        return saxCluster;
    }

}


