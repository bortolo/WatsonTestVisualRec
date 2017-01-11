package com.it.ibm.watson.visualrecframework;

import java.util.HashMap;
import java.util.List;
import org.json.simple.JSONObject;

/**
 * This class contains information regarding all the Visual Recognition instances available
 * 
 * @author Andrea Bortolossi
 *
 */

public class ClassifierMap {

	//This structure pairs api_key to attributes
	// 1) String = api_key
	// 2) List<String> = list of attributes --> status (free/VR_XX) | owner (Bortolossi) | status (ready/training/...) | kind (label) | n (#img training) | p (% of positive) | engine (Watson/Google)
	public HashMap<String, List<String>> apiKeyMap;
	
	public ClassifierMap() {
		//TODO
		//Read from file api_key and define first attributes free | owner
	}

	public ClassifierMap(String apiKeyFile){
		//TODO
		//Read from file api_key CUSTOM and define first attributes free | owner		
	}
		
	public void updatestatus(){
		
		//JSONObject obj = new JSONObject();

		//TODO
		//Define JSON output for apiKeyMap
		// Define where put this file

	}
	

}
