//package imageInImage;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.imageio.ImageIO;

/**
 * Driver for removing an image from a PNG file.
 */
public class RetrieveImageFromImage{
	static Scanner sc = new Scanner(System.in);

	/**
	 * @param args (unused)
	 * @throws IOException
	 *
	 * Scanner takes filename as input, checks its validity.
	 * Scanner takes filename for output file. Checks for existence,
	 * asks if user wants to overwrite.
	 * Opens the image that is hiding data.
	 * Obtains the numLSB used, width, and height from first three pixels.
	 * Calls decode to get list of zeros and ones.
	 * Calls processImage to turn that list into an image, and to
	 * write that image.
	 *
	 */
	public static void main(String[] args) throws IOException
	{
		System.out.print("Enter a file to decode: ");
		String fileName = sc.next();
		fileName = CommonMethods.verifyValidFileName(fileName,sc);

		System.out.print("Enter a destination file name (\".png\" will be appended automatically): ");
		String outputFileName = sc.next() + ".png";
		outputFileName = testForOverwrite(outputFileName);

		System.out.print("Ignorant search (y/n)? ");
		String checkall = sc.next();

		BufferedImage start_image = ImageIO.read(new File(fileName));
		int[] firstRGB = CommonMethods.getPixelData(start_image,0,0);
		int whichBytes = 0;
		int numLSB;
		if (checkall.toLowerCase().charAt(0) == 'y')
		{
			whichBytes = 1;
			numLSB = 1;
		}
		else
		{
			whichBytes = firstRGB[1];
			numLSB = firstRGB[2];
		}

		System.out.println("Detecting the use of " + numLSB + " least significant bits.");
		System.out.println(whichBytes);
		int[] secondRGB = CommonMethods.getPixelData(start_image, 1, 0);
		int width = secondRGB[0]*256*256 + secondRGB[1]*256 + secondRGB[2];
		int[] thirdRGB = CommonMethods.getPixelData(start_image, 2, 0);
		int height = thirdRGB[0]*256*256 + thirdRGB[1]*256 + thirdRGB[2];

		ArrayList<Character> hiddenJunkInBinary = decode(start_image, numLSB, whichBytes);
		processImage(hiddenJunkInBinary,outputFileName, width, height, numLSB);
		System.out.println("Done!");
	}

	/**
	 * @param hiddenTextInBinary Ones and zeros retrieved from image
	 * @param outputFileName The name of the file to be created
	 * @param width Width of the output image
	 * @param height Height of the output image
	 * @param numLSB Number of least significant bytes used.
	 * @throws IOException
	 * Creates new image of appropriate size.
	 * Pulls from list 24 bits at a time (ignoring the H/W/LSB data).
	 * Turns each of these into a color, saved to the appropriate pixel.
	 * Writes image to output file.
	 */
	private static void processImage(ArrayList<Character> hiddenTextInBinary,
			String outputFileName, int width, int height, int numLSB) throws IOException {
		BufferedImage newImage = new BufferedImage(width, height, 1);
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < hiddenTextInBinary.size()-24; i+=24)	{
			String temporary = "";
			for (int j = 0; j < 24; j++)
				temporary += hiddenTextInBinary.get(i + j);
			int newInt = binStringToInt(temporary);
			list.add(newInt);
		}
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				if (i*width+j < list.size())
					newImage.setRGB(j, i, list.get(i*width + j));
		ImageIO.write(newImage, "PNG", new File(outputFileName));
	}

	/**
	 *
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
	 * @param outputFileName
	 * @return outputFileName
	 * Determines if the desired output file exists. If so, asks user if
	 * overwrite is desired. If so, returns same name.
	 * Otherwise, prompts for new file name. Repeats process.
	 */
	private static String testForOverwrite(String outputFileName) {
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
	 * @param startImage
	 * @return ArrayList of characters
	 *
	 * Takes an image file and returns a list of the least significant bits in order.
	 * @throws IOException
	 */
	private static ArrayList<Character> decode(BufferedImage startImage, int numLSB, int whichPixels) throws IOException {
		int[] rgb;
		int counter = 0;
		ArrayList<Character> retval = new ArrayList<Character>();
		for(int i = 0; i < startImage.getHeight(); i++)
			for(int j = 0; j < startImage.getWidth(); j++)
			{
				if (CommonMethods.useItOrNot(counter, whichPixels) && (counter >= 3))
				{
					rgb = CommonMethods.getPixelData(startImage, j, i);
					for (int k = 0; k < 3; k++) {
						String temp = Integer.toBinaryString(rgb[k] % (int) Math.pow(2, numLSB));
						while (temp.length() < numLSB)
							temp = "0" + temp;
						for (int q = 0; q < temp.length(); q++)
							retval.add(temp.charAt(q));
					}
				}
				counter++;
			}
		return retval;
	}

}