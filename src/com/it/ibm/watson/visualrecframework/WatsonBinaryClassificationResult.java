package com.it.ibm.watson.visualrecframework;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ImageClassification;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassification;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassifier;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassifier.VisualClass;

/**
 * This class contains information regarding a test performed by Watson visual
 * recognition
 * 
 * @author Marco Dondio
 *
 *e aggiunto anche terzo commento
 *
 *QUESTA INVECE � IL MASTER!!!
 */
public class WatsonBinaryClassificationResult {

	public static enum METRIC {
		TP, TN, FP, FN, tp, tn, fp, fn, pp, pn, tpr, fpr, accuracy, recall, fallout, precision, f1, jaccard
	};

	private String label;
	private double threshold;
	private HashMap<Long, Boolean> realValues;
	private HashMap<Long, Boolean> predictedValues;

	int n; // size of vectors

	// Vector of basic measures: TP, TN, FP, FN
	EnumMap<METRIC, Integer> measures = new EnumMap<METRIC, Integer>(METRIC.class);

	public WatsonBinaryClassificationResult(String label, HashMap<Long, Boolean> realValues,
			VisualClassification watsonResult, double threshold) {

		this.n = realValues.size();
		this.label = label;
		this.threshold = threshold;
		this.realValues = new LinkedHashMap<Long, Boolean>(realValues);
		this.predictedValues = buildPredictedValues(watsonResult);

		// build contingency matrix
		computeStats();
	}

	// TODO fare più robusto: devo controllare nome classificatore e nome classe
	// per essere sicuri, ora assumo ce ne sia solo uno...
	private HashMap<Long, Boolean> buildPredictedValues(VisualClassification watsonResult) {

		HashMap<Long, Boolean> values = new LinkedHashMap<Long, Boolean>();

		
		System.out.println("Processed: " + watsonResult.getImagesProcessed() + " images.");
		System.out.println("images Size: " + watsonResult.getImages().size() + " images.");

		// for each image
		for (ImageClassification img : watsonResult.getImages()) {

			// First generate imageID from name
			img.getImage(); // XXX nome dell'immagine? qui estrai ID immagine

			String s = img.getImage().replaceAll(".jpg", "");
			s = s.substring(s.lastIndexOf("/") + 1);
			long imageID = Long.parseUnsignedLong(s);

			// If no classifier found prediction is false...
			if (img.getClassifiers().isEmpty()) {
				values.put(imageID, false); // classifier not found!
				continue;
			}
			// XXX assume we have only one classifier
			VisualClassifier classifier = img.getClassifiers().get(0);

			String classifierName = label + "_classifier";
			if (!classifierName.equals(classifier.getName())) {
				values.put(imageID, false); // classifier not found!
				continue;
			}

			// If no class found prediction is false...
			if (classifier.getClasses().isEmpty()) {
				values.put(imageID, false); // classifier not found!
				continue;

			}

			// XXX assume we have only one class
			VisualClass myClass = classifier.getClasses().get(0);

			if (!label.equals(myClass.getName())) {
				values.put(imageID, false); // classifier not found!
				continue;
			}

			// Classify according to threshold
			double watsonScore = myClass.getScore();

			if (Double.compare(watsonScore, threshold) < 0)
				values.put(imageID, false); // classified as negative!
			else // classified as positive!
				values.put(imageID, true); // classifier not found!

//			System.out.println(Long.toUnsignedString(imageID) + " Watson Score -> " + watsonScore);
		}

		////////////////////////////////////////////////

		// TODO aggiungere quelli non riconosciuti!!
		// XXX assumption: se Watson non ritorna uno score per una immagine su questa classe, significa che per lui lo score è < 0.5
		// per noi score < 0.5 diventa classe negativa!
		
		// ATTENZIONE! Supponiamo di forzare la classe a 0 in questo caso
		for (Long l : realValues.keySet())
			if(!values.containsKey(l))
				values.put(l, false);
		////////////////////////////////////////////////
		
		
		// debug
//		for (Long l : values.keySet())
//			System.out.println(
//					Long.toUnsignedString(l) + " predicted as -> " + values.get(l) + " with threshold = " + threshold);

		return values;
	}

	// generates all needed basic metrics
	private void computeStats() {

		// Compute size of set
		n = realValues.size();

		// TODO fill absMetrics (TP, TN, FP, FN)
		measures.put(METRIC.TP, 1);

		// Gather basic measures...
		int TP = 0, TN = 0, FP = 0, FN = 0;

		// For each image...
		for (Long imageID : realValues.keySet()) {

			boolean realClass = realValues.get(imageID);
			boolean predictedClass = predictedValues.get(imageID);

//			System.out.println(
//					Long.toUnsignedString(imageID) + " - real: " + realClass + " vs predicted: " + predictedClass);

			if (realClass && predictedClass)
				TP++;
			else if (realClass && !predictedClass)
				FN++;
			else if (!realClass && predictedClass)
				FP++;
			else // !realClass && !predictedClass
				TN++;
		}

		measures.put(METRIC.TP, TP);
		measures.put(METRIC.TN, TN);
		measures.put(METRIC.FP, FP);
		measures.put(METRIC.FN, FN);

//		for (METRIC m : measures.keySet())
//			System.out.println(m + " -> " + measures.get(m));

	}

	// TODO
	public double computeMetric(METRIC m) {

		switch (m) {
		case tp:
			return (double) measures.get(METRIC.TP) / n;
		case tn:
			return (double) measures.get(METRIC.TN) / n;
		case fp:
			return (double) measures.get(METRIC.FP) / n;
		case fn:
			return (double) measures.get(METRIC.FN) / n;
		case tpr:
			return (double) computeMetric(METRIC.tp) / (computeMetric(METRIC.tp) +computeMetric(METRIC.fn));
		case fpr:
			return (double) computeMetric(METRIC.fp) / (computeMetric(METRIC.tp) +computeMetric(METRIC.fn));
		}

		return 0;
	}

	// TODO
	// parametri base: TP, TN, FP, FN
	// public int getMeasure()

}
