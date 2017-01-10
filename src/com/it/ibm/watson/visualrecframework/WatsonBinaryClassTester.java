package com.it.ibm.watson.visualrecframework;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import com.it.ibm.watson.utils.Utils;

/**
 * Classe di test: prende in input una classe e due cartelle (positivi e
 * negativi) e fa una simulazione. XXX un bel refactor lo farò.. ho buttato giù
 * abbastanza di getto.. TODO: la prima ottimizzazione, rinuncia alle hashmap..
 * è meglio un arraylist con un un array di indici che sono gli indici di inizio
 * classe sucessiva
 * 
 * @author Marco Dondio
 *
 */
public class WatsonBinaryClassTester {

	public static final String BASEFOLDER = "visualrecframework/watsondatasets/";
	public static final String label = "deer2";

	// soglie di taglio Watson
	public static final double[] T = { 0.95, 0.90, 0.85, 0.8, 0.75 };

	// Our sets
	public static HashMap<Long, Boolean> db_b = new LinkedHashMap<Long, Boolean>();
	public static int numPos;

	public static HashMap<Long, Boolean> db_tv = new LinkedHashMap<Long, Boolean>();
	public static int db_tv_numPos, db_tv_numNeg;
	public static HashMap<Long, Boolean> db_test = new LinkedHashMap<Long, Boolean>();
	public static int db_test_numPos, db_test_numNeg;

	// Data structure used to build efficiently other sets
	public static List<Long> images = new ArrayList<Long>();
	public static int firstNegElemIndex; // index of 1st neg element

	public static void main(String[] args) throws IOException {

		// Load db_b and values
		loadDb_b(BASEFOLDER + label);
		// System.out.println(db_b.size() + " pos: " + numPos + " neg: " +
		// numNeg);

		// Split db into t+v and test, 80/20%
		splitDb();

		// Do the loop on thresholds and on k = 10
		// cross validate model.. this will return the optimal value of thresold
		double t_opt = trainAndEvaluate();

		// Step 3:
		// XXX capire che training serve prima di lanciare la simulazione finale
		// sopra il dataset db_test con la soglia t_opt

	}

	// This loads all images in our datasets
	private static void loadDb_b(String path) {
		db_b = new LinkedHashMap<Long, Boolean>();

		// Load positive samples
		File dir = new File(path + "/positive");
		for (File img : dir.listFiles()) {
			long imageID = Long.parseUnsignedLong(img.getName().replaceAll(".jpg", ""));
			db_b.put(imageID, true);
		}
		numPos = dir.listFiles().length;
		firstNegElemIndex = numPos;

		// Load negative samples
		dir = new File(path + "/negative");
		for (File img : dir.listFiles()) {
			long imageID = Long.parseUnsignedLong(img.getName().replaceAll(".jpg", ""));
			db_b.put(imageID, false);
		}

		// Copy values from hashMap
		images = new ArrayList<Long>(db_b.keySet());

	}

	// Splits DB into training + validation and test sets
	// split is 80% / 20%
	private static void splitDb() {
		db_tv = new LinkedHashMap<Long, Boolean>();
		db_test = new LinkedHashMap<Long, Boolean>();

		System.out.println("db_b_numPos: " + numPos + ",  db_b_numNeg: " + (db_b.size() - numPos) + ", tot: "
				+ db_b.size() + "\n");

		// First we calculate the size of our sets
		// Calculate size of pos and neg samples in db_tv (80% of original set)
		db_tv_numPos = (int) Math.ceil((double) numPos * 0.8);
		db_tv_numNeg = (int) Math.ceil((double) (db_b.size() - numPos) * 0.8);

		// ... and size of pos and neg samples in db_test (20% of original set)
		db_test_numPos = numPos - db_tv_numPos;
		db_test_numNeg = (db_b.size() - numPos) - db_tv_numNeg;

		// Now randomly pick elems from db_b set
		populateDb(db_tv, db_tv_numPos, db_tv_numNeg);
		System.out.println(
				"db_tv_numPos: " + db_tv_numPos + ", db_tv_numNeg: " + db_tv_numNeg + ", db_tv tot: " + db_tv.size());
		for (long l : db_tv.keySet())
			System.out.println(Long.toUnsignedString(l) + " -> " + db_tv.get(l));

		populateDb(db_test, db_test_numPos, db_test_numNeg);
		System.out.println("\ndb_test_numPos: " + db_test_numPos + ", db_test_numNeg: " + db_test_numNeg
				+ ", db_test tot: " + db_test.size());
		for (long l : db_test.keySet())
			System.out.println(Long.toUnsignedString(l) + " -> " + db_test.get(l));

	}

	// Method to fill db_tv and db_test
	// we keep same positive / negative ratio of original db_b
	private static void populateDb(HashMap<Long, Boolean> dbToPopulate, int posLeft, int negLeft) {

		// Extract first positive values
		while (posLeft > 0) {
			Random r = new Random();
			int i = r.nextInt(firstNegElemIndex);
			long selected = images.remove(i);
			firstNegElemIndex--;

			dbToPopulate.put(selected, true);
			posLeft--;
		}

		// Extract negative values
		while (negLeft > 0) {
			Random r = new Random();
			int i = r.nextInt((images.size() - firstNegElemIndex)) + firstNegElemIndex;
			long selected = images.remove(i);

			dbToPopulate.put(selected, false);
			negLeft--;
		}

	}

	// This method performs the cross validation and returns t_opt
	private static double trainAndEvaluate() throws IOException {

		int k = 10; // 10-fold cross validation XXX
		double t_opt = 0;

		// db_tv is already sorted and we know how many pos and neg we have

		// db_tv_numPos // positivi in tv
		// db_tv_numNeg // negativi in tv
		// la somma = size del tv.. dividila in 10 e ottieni la dim blocco
		// se divido pos e neg?

		// now we extract all keys (keeping sorting)
		// this structure is needed to get an element using an index
		List<Long> db_tv_images = new ArrayList<Long>(db_tv.keySet());

		// Calculate how many pos and neg elements should go in validation set
		// XXX Note: use floor not ceil otherwise offset goes beyond size!
		int validationSet_numPos = (int) Math.floor((double) db_tv_numPos / k);
		int validationSet_numNeg = (int) Math.floor((double) db_tv_numNeg / k);

		System.out.println("\ntrainingSet_numPos: " + (db_tv_numPos - validationSet_numPos) + ",  trainingSet_numNeg: "
				+ (db_tv_numNeg - validationSet_numNeg) + ", tot size: "
				+ ((db_tv_numPos - validationSet_numPos) + (db_tv_numNeg - validationSet_numNeg)));
		System.out.println("validationSet_numPos: " + validationSet_numPos + ",  validationSet_numNeg: "
				+ validationSet_numNeg + ", tot size: " + (validationSet_numPos + validationSet_numNeg));

		// For each iteration
		for (int i = 0; i < k; i++) {

			System.out.println("------------------------------Iteration num = " + i);

			// offset is the "shift" among iterations
			int offsetPos = i * validationSet_numPos;
			int offsetNeg = i * validationSet_numNeg;

			// First divide training set and validation set
			// we start by copying the db_tv into training set
			HashMap<Long, Boolean> trainingSet = new LinkedHashMap<Long, Boolean>(db_tv);

			// Now we build validation set:
			HashMap<Long, Boolean> validationSet = new LinkedHashMap<Long, Boolean>();

			// positive values
			for (int j = 0; j < validationSet_numPos; j++) {
				long imageID = db_tv_images.get(offsetPos + j);
				validationSet.put(imageID, true);
				trainingSet.remove(imageID); // remove from training
			}
			// negative values
			for (int j = 0; j < validationSet_numNeg; j++) {

				long imageID = db_tv_images.get(db_tv_numPos + offsetNeg + j);
				validationSet.put(imageID, false);
				trainingSet.remove(imageID); // remove from training
			}

			System.out.println("Training set: tot size " + trainingSet.size());
			for (long l : trainingSet.keySet())
				System.out.println(Long.toUnsignedString(l) + " -> " + trainingSet.get(l));

			System.out.println("Validation set: tot size " + validationSet.size());
			for (long l : validationSet.keySet())
				System.out.println(Long.toUnsignedString(l) + " -> " + validationSet.get(l));

			// Now prepare zip files for Watson processing...

			// Prepare positive class for training set
			byte[] positiveClassZip = buildZipStream(BASEFOLDER + label + "/positive/",
					new ArrayList<Long>(trainingSet.keySet()).subList(0, (db_tv_numPos - validationSet_numPos)));

			// Prepare negative class for training set
			byte[] negativeClassZip = buildZipStream(BASEFOLDER + label + "/negative/",
					new ArrayList<Long>(trainingSet.keySet()).subList((db_tv_numPos - validationSet_numPos),
							trainingSet.size()));

			// Finally we can create our classifier:
			// we train with positive and negative classes!
			// Note: we use index in the name
//			WatsonBinaryClassifier classifier_i = new WatsonBinaryClassifier(label + "_" + i, positiveClassZip,
//					negativeClassZip);

			// TODO dopo averlo creato.. aspettare che sia ready!!!

			//------------------------------
			// XXX solo per test
			WatsonBinaryClassifier classifier_i = new WatsonBinaryClassifier();
			classifier_i.setClassifierId("deer2_0_classifier_2045756466");	
			System.out.println(classifier_i.classify(positiveClassZip, 0.5));
			
			
			// XXX solo per test
			// classifier_i.deleteModel();

			System.out.println("DEBUG: EXIT AT LINE 235!!!!!!!!!!!!!!!!");
			System.exit(1);
			//------------------------------

		}

		return t_opt;
	}

	private static byte[] buildZipStream(String path, List<Long> imageList) throws IOException {

		// First, build a list containing all files indicated by this set

		List<File> files = new ArrayList<File>();

		for (long imageID : imageList) {
			files.add(new File(path + Long.toUnsignedString(imageID) + ".jpg"));
			System.out.println(path + Long.toUnsignedString(imageID) + ".jpg");

		}

		return Utils.getCompressedStream(files);
	}

}
