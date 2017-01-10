package com.it.ibm.watson.visualrecframework;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;

import com.it.ibm.watson.utils.CSVUtils;
import com.it.ibm.watson.utils.Utils;

// https://github.com/openimages/dataset

/**
 * Small class that will build our dataset by retrieving images in the
 * openimages dataset
 * 
 * @author Maro Dondio
 *
 */
public class DatasetBuilder {
	

	private static final String BASEFOLDER = "visualrecframework/";
	private static final String MACHINEANNOTATIONFOLDER = "machine_ann_2016_08/";

	private static final String DBFOLDER = "images_2016_08/";

	private static final String MYDATASETFOLDER = "mydataset/";

	private static String label;
	private static String labelDesc;
	private static String labelDirName;

	private static double threshold;

	private static HashSet<Long> highTrainIndex = new HashSet<Long>();
	private static HashSet<Long> highValidationIndex = new HashSet<Long>();

	private static HashSet<Long> lowTrainIndex = new HashSet<Long>();
	private static HashSet<Long> lowValidationIndex = new HashSet<Long>();

	private static HashSet<ImageInfo> highTrainSet = new HashSet<ImageInfo>();
	private static HashSet<ImageInfo> highValidationSet = new HashSet<ImageInfo>();

	private static HashSet<ImageInfo> lowTrainSet = new HashSet<ImageInfo>();
	private static HashSet<ImageInfo> lowValidationSet = new HashSet<ImageInfo>();

	public static void main(String[] args) throws IOException {

		// Read label and threshold
		readInput();

		System.out.println("[DatasetBuilder - " + System.currentTimeMillis() + "] Retrieving " + label + " ("
				+ labelDesc + ") looking for images with min threshold = " + threshold + "...");

		// Check if we already have the folder
		labelDirName = labelDesc.replaceAll("\\s+", "_");
		labelDirName += threshold;

		// Now build indexes: images that satisfy our constraints for the
		// speciefied label
		// XXX we are using machine annotated only now
		System.out.println("[DatasetBuilder - " + System.currentTimeMillis() + "] Building indexes...");

		readIndex(highTrainIndex, lowTrainIndex, new File(BASEFOLDER + MACHINEANNOTATIONFOLDER + "train/labels.csv"));
		readIndex(highValidationIndex, lowValidationIndex,
				new File(BASEFOLDER + MACHINEANNOTATIONFOLDER + "validation/labels.csv"));

		System.out.println("[DatasetBuilder - " + System.currentTimeMillis() + "] Dataset " + label + " (" + labelDesc
				+ ") highTrainIndex contains " + highTrainIndex.size() + " elements.");
		System.out.println("[DatasetBuilder - " + System.currentTimeMillis() + "] Dataset " + label + " (" + labelDesc
				+ ") lowTrainIndex contains " + lowTrainIndex.size() + " elements.");
		System.out.println("[DatasetBuilder - " + System.currentTimeMillis() + "] Dataset " + label + " (" + labelDesc
				+ ") highValidationIndex contains " + highValidationIndex.size() + " elements.");
		System.out.println("[DatasetBuilder - " + System.currentTimeMillis() + "] Dataset " + label + " (" + labelDesc
				+ ") lowValidationIndex contains " + lowValidationIndex.size() + " elements.");

		// Now we can process our database
		System.out.println("[DatasetBuilder - " + System.currentTimeMillis() + "] Reading image database...");

		// Now create dataset
		// createDataset(trainSet, trainIndex, new File(BASEFOLDER + DBFOLDER +
		// "train/images.csv"));
		// createDataset(validationSet, validationIndex, new File(BASEFOLDER +
		// DBFOLDER + "validation/images.csv"));

		createDataset(highTrainSet, highTrainIndex, lowTrainSet, lowTrainIndex,
				new File(BASEFOLDER + DBFOLDER + "train/images.csv"));
		createDataset(highValidationSet, highValidationIndex, lowValidationSet, lowValidationIndex,
				new File(BASEFOLDER + DBFOLDER + "validation/images.csv"));

		System.out.println("[DatasetBuilder - " + System.currentTimeMillis() + "] Dataset " + label + " (" + labelDesc
				+ ") highTrainSet contains " + highTrainSet.size() + " elements.");
		System.out.println("[DatasetBuilder - " + System.currentTimeMillis() + "] Dataset " + label + " (" + labelDesc
				+ ") lowTrainSet contains " + lowTrainSet.size() + " elements.");
		System.out.println("[DatasetBuilder - " + System.currentTimeMillis() + "] Dataset " + label + " (" + labelDesc
				+ ") highValidationSet contains " + highValidationSet.size() + " elements.");
		System.out.println("[DatasetBuilder - " + System.currentTimeMillis() + "] Dataset " + label + " (" + labelDesc
				+ ") lowValidationSet contains " + lowValidationSet.size() + " elements.");

		// Create folder structure
		File highTrainDir = new File(BASEFOLDER + MYDATASETFOLDER + labelDirName + "/high/train");
		highTrainDir.mkdirs();
		File lowTrainDir = new File(BASEFOLDER + MYDATASETFOLDER + labelDirName + "/low/train");
		lowTrainDir.mkdirs();
		File highValidationDir = new File(BASEFOLDER + MYDATASETFOLDER + labelDirName + "/high/validation/");
		highValidationDir.mkdirs();
		File lowValidationDir = new File(BASEFOLDER + MYDATASETFOLDER + labelDirName + "/low/validation/");
		lowValidationDir.mkdirs();

		System.out.println("[DatasetBuilder - " + System.currentTimeMillis() + "] Downloading images...");

		// Download images
		getImages(highTrainSet, highTrainDir);
		getImages(lowTrainSet, lowTrainDir);
		getImages(highValidationSet, highValidationDir);
		getImages(lowValidationSet, lowValidationDir);
	}

	// Reads configuration file
	private static void readInput() throws IOException {

		BufferedReader buf = new BufferedReader(new FileReader(new File(BASEFOLDER + "input.txt")));

		String s;

		while ((s = buf.readLine()) != null) {
			if (s.startsWith("#"))
				continue;

			String[] ss = s.split(" ");

			label = ss[0];
			labelDesc = ss[1];

			threshold = Double.parseDouble(buf.readLine());

		}

		buf.close();

	}

	// Reads index of images that satisfy our needs
	// Format on file is:
	// ImageID,Source,LabelName,Confidence
	// ...
	// 000060e3121c7305,machine,/m/06ht1,0.9
	private static void readIndex(HashSet<Long> highIndex, HashSet<Long> lowIndex, File file)
			throws NumberFormatException, IOException {

		// Open file and extract IDs that satisfy constraints
		BufferedReader buf = new BufferedReader(new FileReader(file));

		String s;

		// Skip header
		s = buf.readLine();

		while ((s = buf.readLine()) != null) {
			String[] attributes = s.split(",");

			// Only keep our label
			if (!label.equals(attributes[2]))
				continue;

			// Divide images according to threshold
			if (Double.compare(threshold, Double.parseDouble(attributes[3])) >= 0)
				lowIndex.add(Long.parseUnsignedLong(attributes[0], 16));
			else
				highIndex.add(Long.parseUnsignedLong(attributes[0], 16));
		}

		buf.close();
	}

	private static void createDataset(HashSet<ImageInfo> highImageSet, HashSet<Long> highIndex,
			HashSet<ImageInfo> lowImageSet, HashSet<Long> lowIndex, File file) throws FileNotFoundException {

		Scanner scanner = new Scanner(file);
		// Skip header
		scanner.nextLine();

		while (scanner.hasNext()) {
			List<String> attributes = CSVUtils.parseLine(scanner.nextLine());

			long imageID = Long.parseUnsignedLong(attributes.get(0), 16);

			// Divide images
			if (highIndex.contains(imageID))
				highImageSet.add(new ImageInfo(attributes));

			else if (lowIndex.contains(imageID))
				lowImageSet.add(new ImageInfo(attributes));
		}

		scanner.close();
	}

	// This downloads images of imageSet in destDir
	private static void getImages(HashSet<ImageInfo> imageSet, File destDir) throws MalformedURLException, IOException {
		for (ImageInfo info : imageSet) {

			long imageID = info.getimageID();
			File imgOutput = new File(destDir.getPath() + "/" + Long.toUnsignedString(imageID) + ".jpg");

			if (imgOutput.exists()) {
				System.out.println("[DatasetBuilder - " + System.currentTimeMillis() + "] Image "
						+ Long.toUnsignedString(imageID) + " exists, skipping.");
				continue;
			}

			// Get URL
			String imageURL = (info.hasThumbnail() ? info.getThumbnailURL() : info.getOriginalURL());

			System.out.println("[DatasetBuilder - " + System.currentTimeMillis() + "] Downloading image "
					+ Long.toUnsignedString(imageID) + "...");

			// get image, resized if needed
			BufferedImage img;

			try {
				img = downloadImage(new URL(imageURL), !info.hasThumbnail());
			} catch (Exception e) {
				System.out.println("[DatasetBuilder - " + System.currentTimeMillis() + "] Error while retrieving image "
						+ Long.toUnsignedString(imageID) + ", skipping image.");
				continue;
			}

			// XXX e se non fossero tutte jpg?
			// write down image
			ImageIO.write(img, "jpg", imgOutput);
		}
	}

	private static BufferedImage downloadImage(URL imageURL, boolean resize) throws IOException {

		// Exception in thread "main" java.lang.IllegalArgumentException:
		// Numbers of source Raster bands and source color space components do
		// not match

		// First get image
		BufferedImage img = ImageIO.read(imageURL);

		// XXX readme...
		// http://stackoverflow.com/questions/24745147/java-resize-image-without-losing-quality/36367652#36367652
		if (resize) {
			// System.out.println("resizing " + imageURL);
			// double xFactor = 640 / img.getWidth();
			// double yFactor = 480 / img.getHeight();
			// img = Utils.scale(img, img.getType(), 640, 480, xFactor,
			// yFactor);

			// alternativa
			img = Utils.scaleNew(img, 640, 480);

		}

		return img;
	}
}
