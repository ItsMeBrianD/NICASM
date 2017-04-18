/**
 * Author: Brian Donald
 * Date: 28/5/15
 * Purpose: Holds useful methods for image manipulation
 */
package site.projectname.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

public class ImageUtil {

	private ImageUtil(){}

	/**
	 *	Imports an image from within the .jar
	 *	@param	filePath	path to the image
	 *	@return				Image from within the jar
	 */
	public static Image importImage(String filePath) {
		Logger log;
		if(!Logger.logs.containsKey("ImageUtil")){
			log = new Logger("ImageUtil");
		} else {
			log = Logger.logs.get("ImageUtil");
		}
		ImageIcon img;
		log.write("Getting image at " + filePath);
		try {
			img = new ImageIcon("img/" + filePath);
			log.write("Success!");
			return img.getImage();
		}
		catch(Exception e) {
			log.write("Failed!");
			log.writeError(e);
			return null;
		}
	}

	/**
	 * Converts an Image to a BufferedImage
	 * @param	img		Image to be converted
	 * @return				Converted image, as a BufferedImage
	 */
	public static BufferedImage toBufferedImage(Image img) {
		BufferedImage result = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D bGr = result.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();
		return result;
	}

	/**
	 * Resizes a BufferedImage
	 * @param	originalImage	The Image to be resized
	 * @param	x				Desired X size of output
	 * @param	y				Desired Y size of output
	 * @return					originalImage reisezed to X by Y
	 */
	public static BufferedImage resizeImage(BufferedImage originalImage, int x, int y) {
		BufferedImage resizedImage = new BufferedImage(x, y, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, x, y, null);
		g.dispose();

		return resizedImage;
	}

}
