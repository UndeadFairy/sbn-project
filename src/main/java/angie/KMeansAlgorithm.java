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
        // TODO - vetsima promennych uz prejmenovana
        this.ci = charInt;
        this.ic = intChar;
        this.saxCluster = new HashMap<>();
        this.k = k;
        this.alphabetSize = alphabetSize;
        this.saxStrings = saxStrings;
      }

    public char calculateAvgChar(char[] chars) {
        int avgChar = 0;
        for (int i = 0; i < chars.length; i++) {
        	avgChar += this.ci.get(chars[i]);
        }
        avgChar = Math.round(avgChar / chars.length);

        return this.ic.get(avgChar);
    }

    /*
    Calculate the distance between two vector of chars. The total distance is a summation
    of the distance of each char in the vectors. A distance from two chars is their
    difference in ASCII value
    */
    /*
    public int calculateDistance(char[] chars1, char[] chars2) throws Exception {
        if (chars1.length != chars2.length) {
            throw new Exception("Sizes of SAX strings do not match! Something wicked!");
        }

        int totalDistance = 0;
        for (int i = 0; i < chars1.length; i++) {
            totalDistance += Math.abs(chars1[i] - chars2[i]);
        }

        return totalDistance;
    }
    */
    /*
    private String generateRandomCentroids() {
        char maxAlphabetLetter = (char) (97 + this.alphabetSize); //97 is the 'a' value in ASCII
        int saxStringSize = this.saxStrings.get(0).length();

        RandomStringGenerator randomStringGenerator
                = new RandomStringGenerator.Builder()
                        .withinRange('a', maxAlphabetLetter)
                        .filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS)
                        .build();
        return randomStringGenerator.generate(saxStringSize);
    }
    */
    private String generateRandomStrings() {
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
        String[] centroids = new String[this.k];
        for (int i = 0; i < this.k; i++) {
            centroids[i] = generateRandomStrings();
        }
        return centroids;
    }
/*
    private String getCentroid(LinkedHashSet<String> cluster) {
        ArrayList<String> clusterList = new ArrayList<>(cluster);  // convert to list to allow indexed
        String centroid = "";

        // eventually after the first iteration, maybe there isn't any item in a
        // specific cluster (because a random generated centroid could have been 
        // bad localized). In this case, there isn't any update in the baricenter
        if (cluster.isEmpty()) {
            return generateRandomStrings();
        }

        int saxStringSize = 0;
        for (String saxString : cluster) {
        	// because it has no get first method
            saxStringSize = saxString.length();
            break;
        }

        // iterate through all the SAX strings of the cluster, calculating the average
        // char value for each position of the strings.
        for (int i = 0; i < saxStringSize; i++) {
            char[] chars = new char[cluster.size()];  // chars carry all the i-th characters being read, that will be averaged
            for (int j = 0; j < clusterList.size(); j++) {
                chars[j] = clusterList.get(j).charAt(i);
            }
            // TODO prepsat komenty a nazvy promennych, nic jineho nejde
            centroid += calculateAvgChar(chars);
        }

        return centroid;
    }
*/
	    /* Calculate the centroid "position" by the datapoints of the cluster i
	    The centroid will have the length of saxStringSize and composed by characters from 'a' to lastAlphabetLetter
	     */
    private String[] getCentroids(ArrayList<LinkedHashSet<String>> clusters) {
        String[] centroids = new String[this.k];
        for (int i = 0; i < this.k; i++) {
        	LinkedHashSet<String> cluster = clusters.get(i);
        	ArrayList<String> clusterArr = new ArrayList<>(cluster);
            String centroid = "";
            // TODO smazat
            // eventually after the first iteration, maybe there isn't any item in a
            // specific cluster (because a random generated centroid could have been 
            // bad localized). In this case, there isn't any update in the baricenter
            
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
            // save it
            centroids[i] = centroid;
        }

        return centroids;
    }

	    /*
	    receive a list of clusters, the saxString to be added and the clusterID that
	    the saxString need to be added. It updates the auxiliary structure 'clusters' 
	    and the structure hmSaxCluster, that contains all the saxStrings and the
	    cluster they belong to.
	     */
    private void updateCluster(ArrayList<LinkedHashSet<String>> clusters, String saxString, int newClusterID) {
        // check if the sax string was already put in another cluster. If so, remove it
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

	    /*
	    returns an array with clusters of saxStrings
	     */
    public HashMap<String, Integer> getClusters() throws Exception {
        int listSize = saxStrings.size();
        int totalErrorOld = 0;
        int totalErrorNew = 0;
        String[] centroids;

        // create empty arraylist to fill later
        ArrayList<LinkedHashSet<String>> clusters = new ArrayList<>();
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

                // iterate through all centroids
                //int newDist;
                int newClusterID = 0;
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
                	
                	
                    //newDistance = calculateDistance(saxString.toCharArray(), centroids[clusterID].toCharArray());
                    //newDist = distanceCalculator.apply(saxString, centroids[clusterID]);

                    // the sax string will be added to the cluster that has the mininum distance
                    // from its centroid to the sax String
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


