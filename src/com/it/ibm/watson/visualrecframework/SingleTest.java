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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

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
	public static final int STOP_TEST = 10; // num of pos or neg elems

	// XXX importante: test max 10 è significativo, perchè in una chiamata
	// classify al max ne prende 20.. capire meglio

	public static final String BASEFOLDER = "visualrecframework/watsondatasetsnew/";
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
//		 Load training set
//		 HashMap<Long, Boolean> trainingSet = loadSet(BASEFOLDER + label +
//		 "/training", STOP_TRAINING);
//		
//		// // Prepare positive class for training set
//		 byte[] positiveClassZip = buildZipStream(BASEFOLDER + label +
//		 "/training/positive/", new
//		 ArrayList<Long>(trainingSet.keySet()).subList(0, STOP_TRAINING));
//		
//		 // Prepare negative class for training set
//		 byte[] negativeClassZip = buildZipStream(BASEFOLDER + label +
//		 "/training/negative/", new
//		 ArrayList<Long>(trainingSet.keySet()).subList(STOP_TRAINING,
//		 trainingSet.size()));
//		
//		 // Train my classifier
//		 WatsonBinaryClassifier classifier = new WatsonBinaryClassifier(label,
//		 positiveClassZip, negativeClassZip);
		

		// ----------------------------------------------------------------------------------------------------------

//		 Retrieve our classifier and get detail
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
		byte[] testImages = buildZipStreamAll(BASEFOLDER + label + "/test", testSet);
		VisualClassification watres = classifier.classify(testImages, 0.0);

		// -------------------------------------------------

		// Compute results and metrics
		double minThreshold = 0.05;
		double maxThreshold = 0.6;
		double step = 0.05;

		PrintWriter out = new PrintWriter(new BufferedWriter(
				new FileWriter(label + "_training-" + 2 * STOP_TRAINING + "_test-" + 2 * STOP_TEST + ".csv")));

		out.println("threshold, tpr, fpr");

		DecimalFormat df = new DecimalFormat("#.###");

		List<Double> tprs = new ArrayList<Double>();
		List<Double> fprs = new ArrayList<Double>();

		for (double threshold = minThreshold; threshold <= maxThreshold; threshold += step) {

			WatsonBinaryClassificationResult result = new WatsonBinaryClassificationResult(label, testSet, watres,
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

			out.println(df.format(threshold) + ", " + df.format(curTpr) + ", " + df.format(curFpr));

		}
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

	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
}
