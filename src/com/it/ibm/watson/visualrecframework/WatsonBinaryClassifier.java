package com.it.ibm.watson.visualrecframework;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifierOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifierOptions.Builder;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyImagesOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassification;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassifier;
import com.it.ibm.watson.config.ImageRecognitionConfig;

/**
 * This class represents a Watson based classifier
 * 
 * @author Marco Dondio
 *
 */
public class WatsonBinaryClassifier {

	private VisualRecognition service;
	private VisualClassifier classifier;

	private String label;
	private String lastClassifierName;

	private String classifierId; // returned by watson

	// keep history of training set
	// too much memory wasted?
	// private List<byte[]> trainingPositiveClassZip = new ArrayList<byte[]>();
	// private List<byte[]> trainingNegativeClassZip = new ArrayList<byte>[]();

	/**
	 * Creates an empty classifier
	 * 
	 * @throws IOException
	 */
	public WatsonBinaryClassifier() throws IOException {

		// First, we instantiate the service.. we need to setup on bluemix first
		// and obtain credentials

		service = new VisualRecognition(VisualRecognition.VERSION_DATE_2016_05_20);
		service.setApiKey(ImageRecognitionConfig.api_key);
	}

	/**
	 * Creates a classifier and trains it
	 * 
	 * @param label
	 *            - the label of classifier to be created
	 * @param positiveClassZip
	 *            - the zip containing positive class
	 * @param negativeClassZip
	 *            - the zip containing negative class
	 * @throws IOException
	 */
	public WatsonBinaryClassifier(String label, byte[] positiveClassZip, byte[] negativeClassZip) throws IOException {

		this(); // Call base constructor to instantiate service

		// Set options
		this.label = label;
		this.lastClassifierName = label + "_classifier";
		// this.initialTrainingPositiveClassZip = positiveClassZip.clone();
		// this.trainingNegativeClassZip = negativeClassZip.clone();

		// Then train model
		trainNewModel(positiveClassZip, negativeClassZip);
	}

	// XXX solo per test
	public void setClassifierId(String classifier_id) {
		this.classifierId = classifier_id;
	}

	// XXX per test
	public void setLabel(String label) {
		this.label = label;

	}

	/**
	 * 
	 * @param negativeClassZip
	 * @param positiveClassZip
	 * @param label
	 *            - the label of classifier to be created
	 * @param positiveClassZip
	 *            - the zip containing positive class
	 * @param negativeClassZip
	 *            - the zip containing negative class
	 * @throws IOException
	 */
	private void trainNewModel(byte[] positiveClassZip, byte[] negativeClassZip) throws IOException {

		System.out.println("[WatsonBinaryClassifier] Creating temporary zip files..");

		// XXX soluzione da trovare migliore.. per ora mi tocca scriverli...
		File posFile = new File("temp_pos.zip");
		FileOutputStream posZip = new FileOutputStream(posFile);
		posZip.write(positiveClassZip);
		posZip.close();

		File negFile = new File("temp_neg.zip");
		FileOutputStream negZip = new FileOutputStream(negFile);
		negZip.write(negativeClassZip);
		negZip.close();

		// CHE PALLE
		// non posso dare lo zip on the fly.. sembra mancare il metodo in
		// addclass...
		// c'era solo images, ok per classificare!

		System.out.println("[WatsonBinaryClassifier] Creating a classifier with positive and negative images...");
		lastClassifierName = label + "_classifier";
		ClassifierOptions createOptions = new ClassifierOptions.Builder().classifierName(lastClassifierName)
				.addClass(label, posFile).negativeExamples(negFile).build();
		classifier = service.createClassifier(createOptions).execute();

		// store id
		classifierId = classifier.getId();

		System.out.println(classifier);

		System.out.println(classifier.getExplanation());

		// Remove temporary files..
		posFile.delete();
		negFile.delete();
	}

	/**
	 * Deletes current classifier
	 */
	public void deleteModel() {

		// NB attenzione:
		// deer2_classifier_1075569631
		System.out.println("[WatsonBinaryClassifier] Deleting model with id: " + classifierId);

		service.deleteClassifier(classifierId).execute();
	}

	/**
	 * Classifies using the current model
	 * 
	 * @param images
	 *            - the zip of images to be classified
	 */
	public VisualClassification classify(byte[] images, double minThreshold) {

		System.out.println("[WatsonBinaryClassifier] Classify images using classifier with id: " + classifierId);

		ClassifyImagesOptions options = new ClassifyImagesOptions.Builder().images(images, "images.zip")
				.classifierIds(classifierId).threshold(minThreshold).build();

		VisualClassification result = service.classify(options).execute();

		 System.out.println(result);
		return result;
	}
	
	
	/**
	 * Classifies many image sets using the current model
	 * 
	 * @param images
	 *            - the zip of images to be classified
	 */
	public List<VisualClassification> classify(List<byte[]> imageSets, double minThreshold) {

		System.out.println("[WatsonBinaryClassifier] Classify set of images using classifier with id: " + classifierId);

		List<VisualClassification> results = new LinkedList<VisualClassification>();
		
		for(byte[] images : imageSets)
			results.add(classify(images, minThreshold));	
		
		return results;
	}

	// XXX forse non va con API key free!
	public void updateClassifier(File posFile, File negFile) throws IOException {

		System.out.println("[WatsonBinaryClassifier] Updating model with id: " + classifierId);

		// Update class
		Builder b = new ClassifierOptions.Builder();

		if (posFile != null) {
			System.out.println("[WatsonBinaryClassifier] Adding positive examples to label " + label + "...");
			b = b.addClass(label, posFile);

		}

		if (negFile != null) {
			System.out.println("[WatsonBinaryClassifier] Adding negative examples to label " + label + "...");
			b = b.negativeExamples(negFile);

		}

		ClassifierOptions updateOptions = b.build();

		classifier = service.updateClassifier(classifierId, updateOptions).execute();
		System.out.println(classifier);

		System.out.println("[WatsonBinaryClassifier] Classifier updated.");

	}

	// XXX Unable to do it with free account
	public void updateClassifier(byte[] positiveClassZip, byte[] negativeClassZip) throws IOException {

		// XXX soluzione da trovare migliore.. per ora mi tocca scriverli...

		File posFile = null, negFile = null;

		if (positiveClassZip != null) {
			posFile = new File("temp_pos.zip");
			FileOutputStream posZip = new FileOutputStream(posFile);
			posZip.write(positiveClassZip);
			posZip.close();

		}

		if (negativeClassZip != null) {
			negFile = new File("temp_neg.zip");
			FileOutputStream negZip = new FileOutputStream(negFile);
			negZip.write(negativeClassZip);
			negZip.close();
		}

		updateClassifier(posFile, negFile); // call other

		if (positiveClassZip != null)
			posFile.delete();
		if (negativeClassZip != null)
			negFile.delete();
	}

	public VisualClassifier getModelDetail() {

		VisualClassifier result = service.getClassifier(classifierId).execute();
		return result;
	}
}
