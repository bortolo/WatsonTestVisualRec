package com.it.ibm.watson.visualrecframework;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Small class that will count label occurrences in Google dataset
 * 
 * @author Maro Dondio
 * 
 * 
 *
 */
public class DatasetAnalyzer {

	private static final String BASEFOLDER = "visualrecframework/";
	private static final String MACHINEANNOTATIONFOLDER = "machine_ann_2016_08/";

	private static HashMap<String, Integer> labelMap = new HashMap<String, Integer>();
	private static HashMap<String, String> dictMap = new HashMap<String, String>();

	public static void main(String[] args) throws IOException {

		System.out.println("[DatasetAnalyzer - " + System.currentTimeMillis() + "] Building dictMap...");

		readDictionary(new File(BASEFOLDER + "/dict.csv"));

		System.out.println("[DatasetAnalyzer - " + System.currentTimeMillis() + "] Calculating labels...");

		countLabelFreq(labelMap, new File(BASEFOLDER + MACHINEANNOTATIONFOLDER + "train/labels.csv"));
		countLabelFreq(labelMap, new File(BASEFOLDER + MACHINEANNOTATIONFOLDER + "validation/labels.csv"));

		labelMap = sortHashMapByValues(labelMap);

		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(BASEFOLDER + "labelDistribution.txt")));

		for (String key : labelMap.keySet())
			out.println(key + " (" + dictMap.get(key) + ") -> " + labelMap.get(key));

		out.close();

		System.out.println("[DatasetAnalyzer - " + System.currentTimeMillis() + "] Done.");

	}

	private static void readDictionary(File file) throws NumberFormatException, IOException {

		// Open file and extract IDs that satisfy constraints
		BufferedReader buf = new BufferedReader(new FileReader(file));

		String s;

		// Skip header
		s = buf.readLine();

		while ((s = buf.readLine()) != null) {

			String label = s.split(",")[0].replaceAll("\"", "");
			String desc = s.split(",")[1].replaceAll("\"", "");

			dictMap.put(label, desc);

		}

		buf.close();
	}

	// Reads index of images that satisfy our needs
	// Format on file is:
	// ImageID,Source,LabelName,Confidence
	// ...
	// 000060e3121c7305,machine,/m/06ht1,0.9
	private static void countLabelFreq(HashMap<String, Integer> labelMap2, File file)
			throws NumberFormatException, IOException {

		// Open file and extract IDs that satisfy constraints
		BufferedReader buf = new BufferedReader(new FileReader(file));

		String s;

		// Skip header
		s = buf.readLine();

		while ((s = buf.readLine()) != null) {

			String label = s.split(",")[2];

			if (!labelMap.containsKey(label))
				labelMap.put(label, 1);

			else
				labelMap.put(label, labelMap.get(label) + 1);

		}

		buf.close();
	}

	private static LinkedHashMap<String, Integer> sortHashMapByValues(HashMap<String, Integer> passedMap) {
		List<String> mapKeys = new ArrayList<String>(passedMap.keySet());
		List<Integer> mapValues = new ArrayList<Integer>(passedMap.values());
		Collections.sort(mapValues);
		Collections.sort(mapKeys);

		LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();

		Iterator<Integer> valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			Integer val = valueIt.next();
			Iterator<String> keyIt = mapKeys.iterator();

			while (keyIt.hasNext()) {
				String key = keyIt.next();
				Integer comp1 = passedMap.get(key);
				Integer comp2 = val;

				if (comp1.equals(comp2)) {
					keyIt.remove();
					sortedMap.put(key, val);
					break;
				}
			}
		}
		return sortedMap;
	}

}
