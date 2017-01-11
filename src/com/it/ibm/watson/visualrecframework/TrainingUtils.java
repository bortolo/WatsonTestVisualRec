package com.it.ibm.watson.visualrecframework;

import java.util.HashMap;
import java.util.List;
import org.json.simple.JSONObject;

/**
 * This class contains methods to add, delete classifier and print status training sets
 * 
 * @author Andrea Bortolossi
 *
 */

public class TrainingUtils {
	
	//This structure pairs label (set training) to attributes
	// 1) String = label (set training)
	// 2) List<String> = list of attributes --> number of positive images | number of negative examples
	public HashMap<String, List<String>> trainSetMap;
	
	public TrainingUtils() {
		//TODO
		// build trainSetMap based on BOX
	}

	// This method trains a classifier
	public boolean add(String apiKey, String label, String n){
		
		boolean check = false;
		
		//TODO
		
		// CHECK
		// check if exist label training set
		// check if there are enough images
		
		// TRAIN CLASSIFIER
		// prepare positive examples zip file
		// prepare negative examples zip file
		// send to visual recognition service
		
		return check;
	}
	
	// This method delete e classifier
	public boolean delete(String apiKey){
		
		boolean check = false;
		
		return check;
	}
	
	// This method produces a Json file which maps the trainin set environments
	public void trainingSetStatus(){
		
		//JSONObject obj = new JSONObject();

		//TODO
		// label + #positivi + #negativi

	}
	

}
