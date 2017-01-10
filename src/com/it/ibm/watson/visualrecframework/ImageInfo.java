package com.it.ibm.watson.visualrecframework;

import java.util.List;
import java.util.Objects;

/**
 * This class contains info regarding an image
 * @author Marco Dondio
 *
 */
public class ImageInfo {

	private long imageID; // 64 bit
	private String originalURL;
	private String thumbnailURL;

	/*
	 * ImageID,Subset,OriginalURL,OriginalLandingURL,License,AuthorProfileURL,
	 * Author,Title,\ OriginalSize,OriginalMD5,Thumbnail300KURL
	 */

	// Example
	/*
	 * 000060e3121c7305, train,
	 * https://c1.staticflickr.com/5/4129/5215831864_46f356962f_o.jpg,
	 * https://www.flickr.com/photos/brokentaco/5215831864,
	 * https://creativecommons.org/licenses/by/2.0/,
	 * "https://www.flickr.com/people/brokentaco/", "David",
	 * "28 Nov 2010 Our new house.", 211079, 0Sad+xMj2ttXM1U8meEJ0A==,
	 * https://c1.staticflickr.com/5/4129/5215831864_ee4e8c6535_z.jpg
	 */

	public ImageInfo(String[] attributes) {

		imageID = Long.parseUnsignedLong(attributes[0], 16);
		originalURL = attributes[2];
		thumbnailURL = (attributes.length > 10 ? attributes[10] : "");

//		System.out.println(imageID + " - " + thumbnailURL);
	}

	public ImageInfo(List<String> attributes) {

		imageID = Long.parseUnsignedLong(attributes.get(0), 16);
		originalURL = attributes.get(2);
		thumbnailURL = (attributes.size() > 10 ? attributes.get(10) : "");

//		System.out.println(imageID + " - " + thumbnailURL);
	}

	
	public long getimageID() {
		return imageID;
	}

	public String getOriginalURL() {
		return originalURL;
	}

	public String getThumbnailURL() {
		return thumbnailURL;
	}

	public boolean hasThumbnail() {

		return !thumbnailURL.isEmpty();
	}

	// See
	// https://www.mkyong.com/java/java-how-to-overrides-equals-and-hashcode/
	@Override
	public boolean equals(Object o) {

		if (o == this)
			return true;
		if (!(o instanceof ImageInfo)) {
			return false;
		}
		ImageInfo imginfo = (ImageInfo) o;
		return Objects.equals(imageID, imginfo.getimageID());
	}

	@Override
	public int hashCode() {
		return Objects.hash(imageID);
	}

}
