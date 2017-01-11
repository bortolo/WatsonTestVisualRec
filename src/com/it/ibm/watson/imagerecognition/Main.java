package com.it.ibm.watson.imagerecognition;
// https://github.com/watson-developer-cloud/java-sdk

import java.io.File;
import java.io.IOException;

import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyImagesOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassification;
import com.it.ibm.watson.config.ImageRecognitionConfig;
import com.it.ibm.watson.utils.Utils;

public class Main {

	public static void main(String[] args) throws IOException {

		
//		System.out.println("swag mattarella");
		

		// First, we instantiate the service.. we need to setup on bluemix first and obtain credentials
		VisualRecognition service = new VisualRecognition(VisualRecognition.VERSION_DATE_2016_05_20);
		service.setApiKey(ImageRecognitionConfig.api_key);

		// Use single image
		System.out.println("Classify an image");
		ClassifyImagesOptions options = new ClassifyImagesOptions.Builder()
				.images(new File("imageDir/no_ok.jpg")).build();
		
				
		VisualClassification result = service.classify(options).execute();
		System.out.println(result);
		

		// Use zipped files		
//		 options = new ClassifyImagesOptions.Builder().images(new File("images.zip")).build();
//			VisualClassification result = service.classify(options).execute();
//			System.out.println(result);
		
		// Compress image folder on the fly and classify
	//	 options = new ClassifyImagesOptions.Builder().images(Utils.getCompressedStream(new File("imageDir")), "imgs.zip").build();
		//	VisualClassification result = service.classify(options).execute();
			//System.out.println(result);

		
	}

}
