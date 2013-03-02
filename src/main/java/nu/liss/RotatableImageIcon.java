/*
 *   An ImageIcon class with rotation.
 *
 *   Hans Liss <Hans@Liss.pp.se> 2006
 *
 *   ... with a grateful nod to Karl Lager for his illuminating
 *   text "A Fast Algorithm For Rotating Bitmaps".
 *
 *   This program is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU General Public License
 *   as published by the Free Software Foundation; either version
 *   2 of the License, or (at your option) any later version.
 *
 */

package nu.liss;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import java.net.*;

/**
 * @author Hans@Liss.nu
 *
 */
public class RotatableImageIcon extends ImageIcon {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Are the attributes initialized?
	 */
	private boolean isInitialized = false;

	/**
	 * Original icon width in pixels
	 */
	private int iconWidth;

	/**
	 * Original icon height in pixels
	 */
	private int iconHeight;

	/**
	 * New icon width in pixels
	 */
	private int newIconWidth;

	/**
	 * New icon height in pixels
	 */
	private int newIconHeight;

	/**
	 * Max X value, with icon midpoint translated to (0,0)
	 */
	private int maxX;

	/**
	 * Min X value, with icon midpoint translated to (0,0)
	 */
	private int minX;

	/**
	 * Max Y value, with icon midpoint translated to (0,0)
	 */
	private int maxY;

	/**
	 * Min Y value, with icon midpoint translated to (0,0)
	 */
	private int minY;

	/**
	 * Direct pixel access original image
	 */
	private int[] iconPixels;

	/**
	 * Direct pixel access to new image
	 */
	private int[] iconPixels2;

	/**
	 * The rotated icon
	 */
	private ImageIcon rotatedIcon = null;

	/** Creates a RotatableImageIcon from an array of bytes which were read from
	 * an image file containing a supported image format, such as GIF or JPEG.
	 * Normally this array is created by reading an image using Class.getResourceAsStream(),
	 * but the byte array may also be statically stored in a class.
	 * If the resulting image has a "comment" property that is a string, then the
     * string is used as the description of this icon.
	 * @param imageData an array of pixels in an image format supported by the AWT Toolkit, such as GIF or JPEG
	 */
	public RotatableImageIcon(byte[] imageData) {
		super(imageData);
		rotatableIconInitialize();
	}

	/** Creates an ImageIcon from an array of bytes which were read from an image
	 * file containing a supported image format, such as GIF or JPEG. Normally this
	 * array is created by reading an image using Class.getResourceAsStream(), but
	 *the byte array may also be statically stored in a class.
	 * @param imageData an array of pixels in an image format supported by the AWT Toolkit, such as GIF or JPEG
	 * @param description a brief textual description of the image
	 */
	public RotatableImageIcon(byte[] imageData, String description) {
		super(imageData, description);
		rotatableIconInitialize();
	}

	/** Creates an ImageIcon from an image object. If the image has a "comment"
	 * property that is a string, then the string is used as the description of this icon.
	 * @param image the image
	 */
	public RotatableImageIcon(Image image) {
		super(image);
		rotatableIconInitialize();
	}

	/** Creates an ImageIcon from the image.
	 * @param image the image
	 * @param description a brief textual description of the image
	 */
	public RotatableImageIcon(Image image, String description) {
		super(image, description);
		rotatableIconInitialize();
	}

	/** Creates an ImageIcon from the specified file. The image will be preloaded by using
     * MediaTracker to monitor the loading state of the image. The specified String can be
     * a file name or a file path. When specifying a path, use the Internet-standard
     * forward-slash ("/") as a separator. (The string is converted to an URL, so the
     * forward-slash works on all systems.) For example, specify:
     *  {@literal new ImageIcon("images/myImage.gif")} 
	 * The description is initialized to the {@literal filename} string.
	 * @param filename a String specifying a filename or path
	 */
	public RotatableImageIcon(String filename) {
		super(filename);
		rotatableIconInitialize();
	}

	/** Creates an ImageIcon from the specified file.
	 * The image will be preloaded by using MediaTracker to monitor the loading state of the image.
	 * @param filename the name of the file containing the image
	 * @param description a brief textual description of the image
	 */
	public RotatableImageIcon(String filename, String description) {
		super(filename, description);
		rotatableIconInitialize();
	}

	/** Creates an ImageIcon from the specified URL. The image will be preloaded by using MediaTracker
	 *  to monitor the loaded state of the image. The icon's description is initialized to be a string
	 *  representation of the URL.
	 * @param location the URL for the image
	 */
	public RotatableImageIcon(URL location) {
		super(location);
		rotatableIconInitialize();
	}

	/** Creates an ImageIcon from the specified URL. The image will be preloaded by using
	 *  MediaTracker to monitor the loaded state of the image.
	 * @param location the URL for the image
	 * @param description a brief textual description of the image
	 */
	public RotatableImageIcon(URL location, String description) {
		super(location, description);
		rotatableIconInitialize();
	}

	private void rotatableIconInitialize() {
		Image iconI = getImage();
		ImageObserver io = getImageObserver();
		int iconW = iconI.getWidth(io);
		int iconH = iconI.getHeight(io);
		BufferedImage iconBuffer = new BufferedImage(iconW, iconH,
				BufferedImage.TYPE_INT_ARGB);
		Graphics iconBufferGraphics = iconBuffer.getGraphics();
		paintIcon((Component) io, iconBufferGraphics, 0, 0);
		iconWidth = getIconWidth();
		iconHeight = getIconHeight();
		newIconWidth = (int) (Math.sqrt(iconWidth * iconWidth + iconHeight * iconHeight) + 0.5) & ~1;
		newIconHeight = newIconWidth;
		maxX = (int) (newIconWidth / 2 + 0.5);
		minX = -maxX;
		maxY = (int) (newIconHeight / 2 + 0.5);
		minY = -maxY;
		BufferedImage iconBuffer2 = new BufferedImage(newIconWidth, newIconHeight, BufferedImage.TYPE_INT_ARGB);
		rotatedIcon = new ImageIcon(iconBuffer2);
		WritableRaster iconRaster = iconBuffer.getRaster();
		iconPixels = ((DataBufferInt) iconRaster.getDataBuffer()).getData();
		WritableRaster iconRaster2 = iconBuffer2.getRaster();
		iconPixels2 = ((DataBufferInt) iconRaster2.getDataBuffer()).getData();
		isInitialized = true;
	}

	/** Create a rotated version of this RotatableImageIcon and return it in the form of an ImageIcon
	 *
	 * Uses Karl Lager's algorithm with some extra oomph in the form of pure integer
	 * arithmetic (and only one multiplication) inside the loop.
	 * 
	 * This also makes use of the direct raster access functionality of the
	 * BufferedImage class. See RI_Initialize(). A hint: don't ever use setRGB()
	 * for more than isolated pixels, unless you absolutely have to.
	 *
	 * @param angle The rotation angle 
	 * @return An {@link ImageIcon} containing a rotated version of this icon
	 */
	public ImageIcon getRotatedIcon(double angle) {
		if (!isInitialized) {
			rotatableIconInitialize();
		}
		double cosT = Math.cos(angle);
		double sinT = Math.sin(angle);
		double xStartPosition = (double) minY * sinT + (double) minX * cosT + 0.5 + iconWidth
				/ 2;
		double yStartPosition = (double) minY * cosT - (double) minX * sinT + 0.5 + iconHeight
				/ 2;
		int numericScalingBits = 16;
		int numericScaling = (1 << numericScalingBits);
		int intXStartPosition = (int) (xStartPosition * numericScaling);
		int intYStartPosition = (int) (yStartPosition * numericScaling);
		int intSinT = (int) (sinT * numericScaling);
		int intCosT = (int) (cosT * numericScaling);
		int intXPosition, intYPosition, xPosition, yPosition;

		int oldXPosition = 0, oldYPosition = 0;
		int tmpValue = 0;
		int line, column;
		int linemax = newIconHeight * newIconWidth;

		for (line = 0; line < linemax; line += newIconWidth) {
			intXPosition = intXStartPosition;
			intYPosition = intYStartPosition;
			for (column = 0; column < newIconWidth; column++) {
				xPosition = intXPosition >> numericScalingBits;
				yPosition = intYPosition >> numericScalingBits;
				if (xPosition >= 0 && xPosition < iconWidth && yPosition >= 0 && yPosition < iconHeight) {
					/* Avoid unnecessary accesses to the same pixel */
					if (xPosition != oldXPosition || yPosition != oldYPosition) {
						tmpValue = iconPixels[xPosition + yPosition * iconWidth];
						oldXPosition = xPosition;
						oldYPosition = yPosition;
					}
				} else
					tmpValue = 0xffffff; // Note: this is actually transparent!
				iconPixels2[line + column] = tmpValue;
				intXPosition += intCosT;
				intYPosition -= intSinT;
			}
			intXStartPosition += intSinT;
			intYStartPosition += intCosT;
		}
		return rotatedIcon;
	}
}
