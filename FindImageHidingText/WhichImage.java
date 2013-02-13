//package findTheImageWithHiddenText;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import javax.imageio.ImageIO;
import java.util.Arrays;

/**
 * Looks at multiple images. Checks each combination of LSB and pixel patterns
 * to determine which image/LSB/pattern combination(s) are most likely to be
 * text. This is determined by how often the ten most common characters appear in
 * relation to the total number of characters.
 */
public class WhichImage {
	static Scanner sc = new Scanner(System.in);
	static DecimalFormat df = new DecimalFormat("#.###");
	static HashMap<Integer, String> pixelPatterns = new HashMap<Integer, String>();
	static ArrayList<String> bestofthebest = new ArrayList<String>();
	static final int TOPWHAT = 10;
	static final int THRESHOLD = 50;

	/**
	 * @param args (unused)
	 * @throws IOException
	 * Calls getImages to get the files to be searched.
	 * Calls getPercentages to determine which is most likely to have text.
	 * Informs user of results.
	 */
	public static void main (String[] args) throws IOException {
		populatePixelPatterns();
		ArrayList<File> images = getImages();
		getPercentages(images);
		System.out.println("\nBest possibilities (percentage = ratio of top " + TOPWHAT + " characters to total characters): ");
		for (String x : bestofthebest)
			System.out.println(x);
		System.out.println();
	}

	/**
	 * @param images A list of the image files to be searched.
	 * @throws IOException
	 * For each file, for each possible number of LSB, for each pixel pattern,
	 * finds the TOPWHAT-many most found characters and how often they occur,
	 * and the number of total characters. Takes the ratio.
	 * Adds to the list to be printed if the ratio exceeds THRESHOLD.
	 */
	private static void getPercentages(ArrayList<File> images) throws IOException
	{
		for (int iNextImage = 0; iNextImage < images.size(); iNextImage++)
		{
			BufferedImage nextImage = ImageIO.read(images.get(iNextImage));
			for (int howManyLSB = 0; howManyLSB < 4; howManyLSB++)
			{
				for (int pixPat = 1; pixPat < 6; pixPat++)
				{
					ArrayList<Character> bits = decode(nextImage, howManyLSB, pixPat);
					int numChars = 0;
					int[] frequencies = new int[256];
					for (int i = 0; i < bits.size()-8; i+=8)
					{
						String temp = "";
						for (int j = 0; j < 8; j++)
							temp += bits.get(i + j);
						int whichChar = binStringToInt(temp);
						if (whichChar < 256 && whichChar != 0)
							frequencies[whichChar]++;
						if (whichChar !=0)
							numChars++;
					}
					Arrays.sort(frequencies);
					int mostCommonChars = 0;
					for (int i = 0; i < TOPWHAT; i++)
						mostCommonChars += frequencies[255-i];
					double thisScore = (100* mostCommonChars)/(double)numChars;
					String output = "" + images.get(iNextImage).getName() + " " +
					howManyLSB + " LSB and pixel pattern " +  pixPat + " (" + pixelPatterns.get(pixPat) + "): " + df.format(thisScore);
					if (thisScore > THRESHOLD)
						bestofthebest.add(output);
					System.out.println("Analyzed " + images.get(iNextImage).getName() + " " +
							howManyLSB + " LSB and pixel pattern " +  pixPat + " (" + pixelPatterns.get(pixPat) + ").");
				}
			}
			System.out.println();
		}
		System.out.println();
	}

	/**
	 * @param nextImage The image to be mined.
	 * @param numLSB The number of LSB to be ripped.
	 *
	 * Gets pixel data for all pixels but the first.
	 * Pulls data from the appropriate number of least significant bits
	 * and from the appropriate pixels.
	 * Returns an array of characters holding those bits.
	 */
	private static ArrayList<Character> decode(BufferedImage nextImage, int numLSB, int whichPattern) {
		int[] rgb;
		ArrayList<Character> retval = new ArrayList<Character>();
		for(int column = 0; column < nextImage.getHeight(); column++)
			for(int row = 0; row < nextImage.getWidth(); row++)
				if ((column != 0 || row != 0) && useItOrNot(column*nextImage.getWidth() + row, whichPattern)) {
					rgb = getPixelData(nextImage, row, column);
					for (int whichColorByte = 0; whichColorByte < 3; whichColorByte++) {
						String temp = Integer.toBinaryString(rgb[whichColorByte] % (int) Math.pow(2, numLSB));
						while (temp.length() < numLSB)
							temp = "0" + temp;
						for (int i = 0; i < temp.length(); i++)
							retval.add(temp.charAt(i));
					}
				}

		return retval;
	}

	/**
	 * Prompts the user to enter names of files.
	 * Does not accept strings that do not correlate to extant files.
	 * User types \"stop\" to stop entering files.
	 * Doing so before entering a valid string quits the program.
	 * @return
	 */
	private static ArrayList<File> getImages() {
		ArrayList<File> retval = new ArrayList<File>();
		while (true)
		{
			System.out.print("Enter an image file name, or enter \"stop\" to quit: ");
			String temp = sc.next();
			if (temp.toLowerCase().equals("stop") && retval.size() == 0)
			{
				System.out.println("No files added. Goodbye!");
				System.exit(0);
			}
			if (temp.equals("stop"))
				return retval;
			File addToList = new File(temp);
			if (!addToList.exists())
				System.out.println("That file does not exist. Try again.");
			else
				retval.add(addToList);
		}
	}

	/**
	 * @param temp A binary string to be converted into an integer.
	 * @return The integer.
	 */
	private static int binStringToInt(String temp) {
		int retval = 0;
		for (int i = 0; i < temp.length(); i++)	{
			if (temp.charAt(i) == '0')
				retval*=2;
			else
				retval = retval*2 + 1;
		}
		return retval;
	}

	/**
	 * @param img The image to be mined.
	 * @param x A coordinate.
	 * @param y The other coordinate.
	 * @return rgb An array containing the (duh) RGB data.
	 * Gets pixel data and breaks it into separate red/green/blue data
	 */
	public static int[] getPixelData(BufferedImage img, int x, int y) {
		int argb = img.getRGB(x, y);
		int rgb[] = new int[] {
				(argb >> 16) & 0xff, //red
				(argb >>  8) & 0xff, //green
				(argb      ) & 0xff  //blue
		};
		return rgb;
	}

	/**
	 * @param thisPixel The pixel currently being read.
	 * @param whichPixels The int that determines which pixel patterns is being tested.
	 *
	 * Determines whether or not to look for data in this pixel.
	 */
	private static boolean useItOrNot(int thisPixel, int whichPixels) {
		boolean[] even = {true, false};
		boolean[] third = {true, false, false};
		boolean useThisByte = true;
		if (whichPixels == 2)
			return even[thisPixel%2];
		if (whichPixels == 3)
			return !even[thisPixel%2];
		if (whichPixels == 4)
			return third[thisPixel%3];
		if (whichPixels == 5)
			return isItPrime(thisPixel);
		return useThisByte;
	}

	/**
	 * @param n The number of the pixel being considered
	 * @return Whether the number is prime.
	 */
	private static boolean isItPrime (int n) {
		if (n < 2)
			return false;
		if (n == 2 || n == 3)
			return true;
		if(n%2 == 0 || n%3 == 0)
			return false;
		int sqrtN = (int) Math.sqrt(n)+1;
		for (int i = 6; i <= sqrtN; i += 6)
			if(n%(i-1) == 0 || n%(i+1) == 0)
				return false;
		return true;
	}

	/**
	 * What it says on the tin.
	 */
	private static void populatePixelPatterns () {
		pixelPatterns.put(1, "every pixel");
		pixelPatterns.put(2, "every even pixel");
		pixelPatterns.put(3, "every odd pixel");
		pixelPatterns.put(4, "every third pixel");
		pixelPatterns.put(5, "every prime-numbered pixel");
	}

}