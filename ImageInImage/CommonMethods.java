//package imageInImage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Scanner;

public class CommonMethods {

	/**
	 * @param img An image file.
	 * @param x Which column of pixels.
	 * @param y Which row of pixels.
	 * @return rgb A three-tuple containing the RGB values of the designated pixel.
	 * Gets pixel data and breaks it into separate red/green/blue data
	 */
	public static int[] getPixelData(BufferedImage img, int x, int y)
	{
		int argb = img.getRGB(x, y);
		int rgb[] = new int[] {
				(argb >> 16) & 0xff, //red
				(argb >>  8) & 0xff, //green
				(argb      ) & 0xff  //blue
		};
		return rgb;
	}

	/**
	 *
	 * @param someString The proposed name of the file to open.
	 * @param sc Scanner for keyboard.
	 * @param prompt String passed to use as error message to user.
	 * @return someString Now known to correlate to an extant file.
	 *
	 * If the file name passed by the arguments does not correspond to a real file,
	 * prompts the user for new file names until a valid one is found.
	 */
	public static String verifyValidFileName(String someString, Scanner sc)
	{
		File someFileName = new File(someString);
		while (!someFileName.exists())
		{
			System.out.print("Not a valid file name. Please try again: ");
			someString = sc.nextLine();
			someFileName = new File(someString);
		}
		return someString;
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

	/**
	 * @param thisPixel Which pixel is being evaluated
	 * @param whichPixels The pattern of pixels being used
	 * @return Whether this pixel contains/should contain masked data.
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

}