package com.it.ibm.watson.utils;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Utils {

	/**
	 * This method returns an InputStream, which is implemented by a zipped
	 * stream built from a directory. <br>
	 * 
	 * @param dirName
	 *            the name of the directory containing files to be zipped
	 * @return the zipped InputStream
	 */
	public static InputStream getCompressedStream_old(File dirName)
			throws IOException {

		if (!dirName.isDirectory())
			return null;

		byte data[] = new byte[2048];
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		ZipOutputStream zos = new ZipOutputStream(bos);

		for (File f : dirName.listFiles()) {

			BufferedInputStream entryStream = new BufferedInputStream(
					new FileInputStream(f), 2048);
			ZipEntry entry = new ZipEntry(f.getName());
			zos.putNextEntry(entry);
			int count;
			while ((count = entryStream.read(data, 0, 2048)) != -1) {
				zos.write(data, 0, count);
			}
			entryStream.close();
			zos.closeEntry();

		}

		zos.close();

		return new ByteArrayInputStream(bos.toByteArray());
	}

	
	/**
	 * This method returns a byte[], which is implemented by a zipped
	 * stream built from a directory. <br>
	 * 
	 * @param dirName
	 *            the name of the directory containing files to be zipped
	 * @return the zipped byte[]
	 */
	public static byte[] getCompressedStream(File dirName)
			throws IOException {

		if (!dirName.isDirectory())
			return null;

		byte data[] = new byte[2048];
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		ZipOutputStream zos = new ZipOutputStream(bos);

		for (File f : dirName.listFiles()) {

			BufferedInputStream entryStream = new BufferedInputStream(
					new FileInputStream(f), 2048);
			ZipEntry entry = new ZipEntry(f.getName());
			zos.putNextEntry(entry);
			int count;
			while ((count = entryStream.read(data, 0, 2048)) != -1) {
				zos.write(data, 0, count);
			}
			entryStream.close();
			zos.closeEntry();

		}

		zos.close();

		return bos.toByteArray();
	}

	
	/**
	 * This method returns a byte[], which is implemented by a zipped
	 * stream built from a directory. <br>
	 * 
	 * @param files
	 *            the files to be zipped
	 * @return the zipped byte[]
	 */
	public static byte[] getCompressedStream(List<File> files)
			throws IOException {

		byte data[] = new byte[2048];
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		ZipOutputStream zos = new ZipOutputStream(bos);

		for (File f : files) {

			BufferedInputStream entryStream = new BufferedInputStream(
					new FileInputStream(f), 2048);
			ZipEntry entry = new ZipEntry(f.getName());
			zos.putNextEntry(entry);
			int count;
			while ((count = entryStream.read(data, 0, 2048)) != -1) {
				zos.write(data, 0, count);
			}
			entryStream.close();
			zos.closeEntry();

		}

		zos.close();

		return bos.toByteArray();
	}

	
	
	
	/**
	 * scale image
	 * 
	 * @param sbi image to scale
	 * @param imageType type of image
	 * @param dWidth width of destination image
	 * @param dHeight height of destination image
	 * @param fWidth x-factor for transformation / scaling
	 * @param fHeight y-factor for transformation / scaling
	 * @return scaled image
	 */
	// f = scaling factor
	//	dWidth = sbi.getWidth()*fWidth 
	//  dHeight = sbi.getGeight()*fHeight 

	// http://stackoverflow.com/questions/15558202/how-to-resize-image-in-java
	public static BufferedImage scale(BufferedImage sbi, int imageType, int dWidth, int dHeight, double fWidth, double fHeight) {
	    BufferedImage dbi = null;
	    if(sbi != null) {
	        dbi = new BufferedImage(dWidth, dHeight, imageType);
	        Graphics2D g = dbi.createGraphics();
	        AffineTransform at = AffineTransform.getScaleInstance(fWidth, fHeight);
	        g.drawRenderedImage(sbi, at);
	    }
	    return dbi;
	}
	
	
	public static BufferedImage scaleNew(BufferedImage imageToScale, int dWidth, int dHeight) {
        BufferedImage scaledImage = null;
        if (imageToScale != null) {
            scaledImage = new BufferedImage(dWidth, dHeight, imageToScale.getType());
            Graphics2D graphics2D = scaledImage.createGraphics();

            			graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            
            graphics2D.drawImage(imageToScale, 0, 0, dWidth, dHeight, null);
            graphics2D.dispose();
        }
        return scaledImage;
    }
}
