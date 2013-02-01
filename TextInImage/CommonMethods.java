//package textInImage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Scanner;

public class CommonMethods {
	/** Gets pixel data and breaks it into separate red/green/blue data */
	public static int[] getPixelData(BufferedImage img, int x, int y) {
		int argb = img.getRGB(x, y);
		int rgb[] = new int[] {
				(argb >> 16) & 0xff, //red
				(argb >>  8) & 0xff, //green
				(argb      ) & 0xff  //blue
		};
		return rgb;
	}

	/** If the file name passed by the arguments does not correspond to a real file,
	 * prompts the user for new file names until a valid one is found. */
	public static String verifyValidFileName(String someString, Scanner sc) {
		File someFileName = new File(someString);
		while (!someFileName.exists()) {
			System.out.print("That is not a valid file name. ");
			System.out.print("Please try again: ");
			someString = sc.nextLine();
			someFileName = new File(someString);
		}
		return someString;
	}

	public static String testForOverwrite(String outputFileName, Scanner sc) {
		boolean loop = true;
		while (loop) {
			File potentialOutputFile = new File(outputFileName);
			if (potentialOutputFile.exists()) {
				System.out.print("That file exists. Overwrite (Y/N)? ");
				String response = sc.next();
				if (Character.toLowerCase(response.charAt(0)) == 'n') {
					System.out.print("Enter a new destination file name: ");
					outputFileName = sc.next();
				}
				else loop = false;
			}
			else loop = false;
		}
		return outputFileName;
	}

	/**
	 * @param thisPixel Which pixel is being evaluated
	 * @param whichPixels The pattern of pixels being used
	 * @return Whether this pixel should contain masked data.
	 */
	public static boolean useItOrNot(int thisPixel, int whichPixels) {
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
	public static boolean isItPrime (int n) {
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

}
