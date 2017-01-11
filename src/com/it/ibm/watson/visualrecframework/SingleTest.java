package com.it.ibm.watson.visualrecframework;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassification;
import com.it.ibm.watson.utils.Utils;
import com.it.ibm.watson.visualrecframework.WatsonBinaryClassificationResult.METRIC;

/**
 * Piccolo tester per classificatore esistente
 * 
 * @author Marco Dondio
 *
 */
public class SingleTest {

	public static final int STOP_TRAINING = 500; // num of pos or neg elems
	public static final int STOP_TEST = 100; // num of pos or neg elems

	public static final int CLASSIFYMAXIMAGES = 20;	// max number of images per call
	
	public static final double WATSONMINSCORE = 0.0;	// minimum score for watson result
	

	// XXX importante: test max 10 è significativo, perchè in una chiamata
	// classify al max ne prende 20.. capire meglio

	public static final String BASEFOLDER = "C:/Users/IBM_ADMIN/Box Sync/WATSON experiments/Datasets di riferimento/Ready/";
	//public static final String BASEFOLDER = "visualrecframework/watsondatasetsnew/";
	public static final String label = "watch";

	// Classificatore Luca: watch_classifier_1872144544

	public static void main(String[] args) throws IOException {

		// ----------------------------------------------------------------------------------------------------------

		// Sezione di delete

		// WatsonBinaryClassifier classifier = new WatsonBinaryClassifier();
		// classifier.setClassifierId("watch_classifier_195755765");
		// classifier.setLabel(label);
		//
		// classifier.deleteModel();
		//
		// System.exit(1);
		// ----------------------------------------------------------------------------------------------------------

		// Sezione di training
		// Load training set
		// HashMap<Long, Boolean> trainingSet = loadSet(BASEFOLDER + label +
		// "/training", STOP_TRAINING);
		//
		// // // Prepare positive class for training set
		// byte[] positiveClassZip = buildZipStream(BASEFOLDER + label +
		// "/training/positive/", new
		// ArrayList<Long>(trainingSet.keySet()).subList(0, STOP_TRAINING));
		//
		// // Prepare negative class for training set
		// byte[] negativeClassZip = buildZipStream(BASEFOLDER + label +
		// "/training/negative/", new
		// ArrayList<Long>(trainingSet.keySet()).subList(STOP_TRAINING,
		// trainingSet.size()));
		//
		// // Train my classifier
		// WatsonBinaryClassifier classifier = new WatsonBinaryClassifier(label,
		// positiveClassZip, negativeClassZip);

		// ----------------------------------------------------------------------------------------------------------

//		 //Retrieve our classifier and get detail
//		 WatsonBinaryClassifier classifier = new WatsonBinaryClassifier();
//		 classifier.setClassifierId("watch_classifier_1872144544");
//		 classifier.setLabel(label);
//		
//		 System.out.println(classifier.getModelDetail());

		 // ----------------------------------------------------------------------------------------------------------
		// Sezione di test

		WatsonBinaryClassifier classifier = new WatsonBinaryClassifier();
		classifier.setClassifierId("watch_classifier_1872144544");
		classifier.setLabel(label);

		// Load test set: these are the real values
		HashMap<Long, Boolean> testSet = loadSet(BASEFOLDER + label + "/test", STOP_TEST);

		System.out.println("loaded: ");
		for (Long l : testSet.keySet())
			System.out.println(Long.toUnsignedString(l) + " -> " + testSet.get(l));

		// -------------------------------------------------
		// TODO: questo pezzo andrebbe spezzato! Max 20 immagini per chiamate di
		// clasificazione
		// Prepare positive class for
		// test set and classify!

		// Prepare test sets
		List<byte[]> testImageSets = buildZipStreamBlocks(BASEFOLDER + label + "/test", testSet);

		// Classify all zips
		List<VisualClassification> watsonres = classifier.classify(testImageSets, WATSONMINSCORE);


		 // Compute results and metrics
		 double minThreshold = 0.01;
		 double maxThreshold = 0.99;
		 double step = 0.01;
		
		 PrintWriter out = new PrintWriter(new BufferedWriter(
		 new FileWriter("C:/Users/IBM_ADMIN/Box Sync/WATSON experiments/Output/" + label + "_training-" + 2 * STOP_TRAINING + "_test-" +
		 2 * STOP_TEST + ".json")));
		
		// out.println("threshold, tpr, fpr");
		
		 DecimalFormat df = new DecimalFormat("#.###");
		
		 List<Double> tprs = new ArrayList<Double>();
		 List<Double> fprs = new ArrayList<Double>();
		
		 for (double threshold = minThreshold; threshold <= maxThreshold;
		 threshold += step) {
		
		 WatsonBinaryClassificationResult result = new
		 WatsonBinaryClassificationResult(label, testSet, watsonres,
		 threshold);
		
		 double curTpr = result.computeMetric(METRIC.tpr);
		 double curFpr = result.computeMetric(METRIC.fpr);
		 tprs.add(curTpr);
		 fprs.add(curFpr);
		
		 System.out.println("-------------------------------");
		 System.out.println("threshold: " + df.format(threshold));
		 System.out.println("tpr: " + df.format(curTpr));
		 System.out.println("fpr: " + df.format(curFpr));
		 System.out.println("-------------------------------");
		
		
		 }
		 
		 		 
		 System.out.println("*********************************");
		 System.out.println("AUC: " + df.format(TrepezoidiInteger(tprs,fprs)));
		 System.out.println("*********************************");
		 
		 out.println(buildJSON(tprs, fprs));
		
		 out.close();
	}

	private static HashMap<Long, Boolean> loadSet(String path, int stop) {

		HashMap<Long, Boolean> set = new LinkedHashMap<Long, Boolean>();

		// Load positive samples
		int i = 0;
		File dir = new File(path + "/positive");
		for (File img : dir.listFiles()) {
			long imageID = Long.parseUnsignedLong(img.getName().replaceAll(".jpg", ""));
			set.put(imageID, true);

			// System.out.println(i);
			if ((++i) >= stop)
				break;
		}

		// Load negative samples
		i = 0;
		dir = new File(path + "/negative");
		for (File img : dir.listFiles()) {
			long imageID = Long.parseUnsignedLong(img.getName().replaceAll(".jpg", ""));
			set.put(imageID, false);

			if ((++i) >= stop)
				break;
		}

		return set;
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

	// Questo costruisce lo zip per il test, unione di positivi e negativi
	private static byte[] buildZipStreamAll(String path, HashMap<Long, Boolean> imageList) throws IOException {

		// First, build a list containing all files indicated by this set
		List<File> files = new ArrayList<File>();

		for (long imageID : imageList.keySet()) {
			files.add(new File(path + (imageList.get(imageID) ? "/positive/" : "/negative/")
					+ Long.toUnsignedString(imageID) + ".jpg"));

			// files.add(new File(path[0] + Long.toUnsignedString(imageID) +
			// ".jpg"));
			// System.out.println(path[0] + Long.toUnsignedString(imageID) +
			// ".jpg");

		}

		return Utils.getCompressedStream(files);
	}

	// // Questo costruisce lo lista zip per il test, unione di positivi e
	// negativi
	// private static List<byte[]> buildZipStreamBlocks(String path,
	// HashMap<Long, Boolean> imageList) throws IOException {
	//
	// System.out.println("crippareee");
	// // List of zips, each one containing CLASSIFYMAXIMAGES images
	// List<byte[]> results = new LinkedList<byte[]>();
	//
	// // Get all keys
	// List<Long> keys = new ArrayList<Long>(imageList.keySet());
	//
	// int count = 0;
	// List<File> curFiles = new ArrayList<File>();
	// while(!keys.isEmpty()){
	//
	// // every CLASSIFYMAXIMAGES (except first) create new list
	// if(count != 0 && (count % CLASSIFYMAXIMAGES) == 0){
	//
	// // Add to zips
	// results.add(Utils.getCompressedStream(curFiles));
	// System.out.println("Adding a block");
	// curFiles = new ArrayList<File>();
	// }
	//
	// // extract one elem
	// Long k = keys.remove(0);
	//
	// // add to current list
	// curFiles.add(new File(path + (imageList.get(k) ? "/positive/" :
	// "/negative/")+ Long.toUnsignedString(k) + ".jpg"));
	//
	// count++;
	// }
	//
	//
	// return results;
	// }

	// Questo costruisce lo lista zip per il test, unione di positivi e negativi
	private static List<byte[]> buildZipStreamBlocks(String path, HashMap<Long, Boolean> imageList) throws IOException {

		System.out.println("crippareee");
		// List of zips, each one containing CLASSIFYMAXIMAGES images
		List<byte[]> results = new LinkedList<byte[]>();

		// Number of blocks we split our sets
		int numBlocks = (int) Math.ceil((double) imageList.size() / CLASSIFYMAXIMAGES);

		// Get all keys
		List<Long> keys = new ArrayList<Long>(imageList.keySet());

		// For every block...
		for (int i = 0; i < numBlocks; i++) {

			// Extract sublist
				List<Long> subKeys = keys.subList(i*CLASSIFYMAXIMAGES, Math.min(i*CLASSIFYMAXIMAGES + CLASSIFYMAXIMAGES, keys.size()));

				
				System.out.println("da " + i*CLASSIFYMAXIMAGES +" a "+  Math.min(i*CLASSIFYMAXIMAGES + CLASSIFYMAXIMAGES, keys.size()));
				
				// Build sub hashmap
				HashMap<Long, Boolean> imageSubList = new HashMap<Long, Boolean>();
				
				for(Long l : subKeys)
					imageSubList.put(l, imageList.get(l));
				
				// Add to zip files
				results.add(buildZipStreamAll(path, imageSubList));
			}

		

		return results;
	}

	private static JSONObject buildJSON(List<Double> tprs, List<Double> fprs) throws IOException {

		JSONObject obj = new JSONObject();

		obj.put("label", "ROC");

		// Build x series (fprs)
		// Build x series (tprs)

		JSONArray fprsArray = new JSONArray();
		JSONArray tprsArray = new JSONArray();

		for (Double d : fprs)
			fprsArray.add(d);

		for (Double d : tprs)
			tprsArray.add(d);

		obj.put("x", fprsArray);
		obj.put("y", tprsArray);

		return obj;

	}
	
	private static double TrepezoidiInteger(List<Double> tprs, List<Double> fprs){
		
		double AUC = 0.0;
		
		for (int i = 0; i< (fprs.size()-1) ; i++)
		{
			double x_a = fprs.get(i);
			double x_b = fprs.get(i+1);
			double a = tprs.get(i);
			double b = tprs.get(i+1);
			
			AUC+=((b+a)*(x_b-x_a)/2);
		}
				
		return AUC;		
	}
	
}
