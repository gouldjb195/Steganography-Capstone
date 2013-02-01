//package imageInImage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.imageio.ImageIO;

/**
 * Hides an image file within another image, space permitting.
 * Uses up to 3 LSB, and uses one of five possible patterns of pixels.
 */
public class HideImageInImage {
	static Scanner sc = new Scanner(System.in);
	static HashMap<String, Integer> combos = new HashMap<String, Integer>();
	static HashMap<Character, String> input = new HashMap<Character, String>();
	static DecimalFormat df = new DecimalFormat("#.###");

	/**
	 * @param args (unused)
	 * @throws IOException
	 *
	 * Prompts user for file names (start image, image to be hidden).
	 * Verifies each is an actual file, prompts for new name otherwise.
	 * Informs user which combinations of LSB/pixel use are possible.
	 * User chooses one.
	 * Prompts for name of output file.
	 * If file exists, verifies overwrite.
	 * Takes pixel data and puts it into an array.
	 * Turns each int in that array and turns it into a bitstring,
	 * making an array of strings of eight (0/1) characters each.
	 * Turns that into an array list of characters.
	 * Sends this to replaceImage; gets end image back.
	 * Writes image to desired location.
	 */
	public static void main(String[] args) throws IOException  	{

		addCombos();
		addInputs();

		System.out.print("Name of image file to contain hidden image: ");
		String start_image_name = CommonMethods.verifyValidFileName(sc.next(), sc);
		System.out.print("Name of image file to be hidden: ");
		String hide_image_name = CommonMethods.verifyValidFileName(sc.next(), sc);

		BufferedImage start_image = ImageIO.read(new File(start_image_name));
		BufferedImage hidden_image = ImageIO.read(new File(hide_image_name));

		int bitsNeeded = hidden_image.getHeight()*hidden_image.getWidth()*24;
		int maxBitsAvail = start_image.getHeight()*start_image.getWidth()*9;

		System.out.println("Bits needed: " + bitsNeeded);
		System.out.println("Maximum bits available: " + maxBitsAvail);
		double ratio = (double) bitsNeeded / maxBitsAvail;
		System.out.println("Ratio: " + df.format(ratio));
		int whichCombo = comboMenu(whatAreOptions(ratio, start_image.getHeight()*start_image.getWidth()));
		int numLSB = whichCombo%10;
		int iWhichPixels = whichCombo/10;

		System.out.print("Save output to (\".png\" will be auto-appended): ");
		String new_file_name = testForOverwrite(sc.next() + ".png");

		ArrayList<Integer> colors = new ArrayList<Integer>();
		int[] nextThreeBytes = new int[3];
		for (int i = 0; i < hidden_image.getHeight(); i++)
			for (int j = 0; j < hidden_image.getWidth(); j++) {
				nextThreeBytes = CommonMethods.getPixelData(hidden_image, j, i);
				for (int r = 0; r < 3; r++)
					colors.add(nextThreeBytes[r]);
			}
		ArrayList<String> eachColorByteAsStringOfEightBits = getBitsFromIntArray(colors);

		ArrayList<Character> listOfZerosAndOnes = new ArrayList<Character>();
		for (int i = 0; i < eachColorByteAsStringOfEightBits.size(); i++)
			for (int j = 0; j < 8; j++)
				listOfZerosAndOnes.add(eachColorByteAsStringOfEightBits.get(i).charAt(j));

		BufferedImage end_image = replaceImage(start_image, listOfZerosAndOnes, numLSB,
				hidden_image.getWidth(), hidden_image.getHeight(), iWhichPixels);

		ImageIO.write(end_image, "PNG", new File(new_file_name));
		System.out.println("Done!");
	}

	/**
	 * @param howManyToPrint How many from list of combos to display.
	 * @return An integer representing the number of LSB and
	 * which pixel pattern to use.
	 * Prints a menu containing only the combinations that will fit.
	 * Takes input until proper response is input.
	 * Returns the int associated with the user's choice.
	 * Tens digit is the pixel pattern.
	 * Ones digit is how many LSB.
	 */
	private static int comboMenu(int howManyToPrint) {
		for (int i = 0; i < howManyToPrint; i++)
			System.out.println((char) (i+65) + ": " + input.get((char) (i+65)));
		System.out.print("Which combination of LSB and pixels? ");
		char first;
		while (true)
		{
			String response = sc.next();
			first = response.toUpperCase().charAt(0);
			if (first - 'A' < 0 || first - 'A' + 1 > howManyToPrint)
				System.out.print("Not a valid entry. Try again: ");
			else
				break;
		}
		return combos.get(input.get(first));
	}

	/**
	 * @param ratio Ratio of space needed to space available.
	 * @param pixelsTotal Number of pixels available.
	 * @return How many menu options to print
	 *
	 * If the image will not fit, says so. Otherwise, determines which
	 * possible lsb/pixel combos to present to the user.
	 */
	private static int whatAreOptions(double ratio, int pixelsTotal) {
		if (ratio > 1)
		{
			System.out.println("The image will not fit.");
			System.exit(0);
		}

		else if (ratio > 2.0/3)
			return 1;
		else if (ratio > .5)
			return 2;
		else if (ratio > 1.0/3)
			return 4;
		else if (ratio > 2.0/9)
			return 8;
		else if (ratio > 1.0/6)
			return 9;
		else if (ratio > 1.0/9)
			return 11;
		else if (ratio > 1.0/(Math.log(3*pixelsTotal)))
			return 12;
		else if (ratio > 1.0/(Math.log(2*pixelsTotal)))
			return 13;
		else if (ratio > 1.0/(Math.log(pixelsTotal)))
			return 14;
		return 15;
	}

	/**
	 * Populates hashmap with user input and associated lsb/pixel choice.
	 */
	private static void addInputs() {
		input.put('A',"All pixels, three LSB");
		input.put('B', "All pixels, two LSB");
		input.put('C', "Every even pixel, three LSB");
		input.put('D', "Every odd pixel, three LSB");
		input.put('E', "Every pixel, one LSB");
		input.put('F', "Every even pixel, two LSB");
		input.put('G', "Every odd pixel, two LSB");
		input.put('H', "Every third pixel, three LSB");
		input.put('I', "Every third pixel, two LSB");
		input.put('J', "Every even pixel, one LSB");
		input.put('K', "Every odd pixel, one LSB");
		input.put('L', "Every third pixel, one LSB");
		input.put('M', "Every prime-numbered pixel, three LSB");
		input.put('N', "Every prime-numbered pixel, two LSB");
		input.put('O', "Every prime-numbered pixel, one LSB");
	}

	/**
	 * Populates hashmap with lsb/pixel choice and the int to be interpreted
	 * by main.
	 */
	private static void addCombos() {
		combos.put("All pixels, three LSB", 13);
		combos.put("All pixels, two LSB", 12);
		combos.put("Every even pixel, three LSB", 23);
		combos.put("Every odd pixel, three LSB", 33);
		combos.put("Every pixel, one LSB", 11);
		combos.put("Every even pixel, two LSB",22);
		combos.put("Every odd pixel, two LSB", 32);
		combos.put("Every third pixel, three LSB",43);
		combos.put("Every third pixel, two LSB",42);
		combos.put("Every even pixel, one LSB",21);
		combos.put("Every odd pixel, one LSB",31);
		combos.put("Every third pixel, one LSB",41);
		combos.put("Every prime-numbered pixel, three LSB", 53);
		combos.put("Every prime-numbered pixel, two LSB",52);
		combos.put("Every prime-numbered pixel, one LSB",51);


	}

	/**
	 * @param startImage The image to be replaced.
	 * @param bitsToHide Arraylist of bits to hide in image.
	 * @param numLSB Number of LSB to use.
	 * @param width   Width of image being hidden.
	 * @param height Height of image being hidden.
	 * @return Image with some number of LSB replaced with data from hidden image.
	 *
	 * Creates new image size of image in which things will be hidden.
	 * Sets RGB of first three pixels to hide the numLSB, height, and width.
	 * Sets the remainder of the pixels to hold data from bitsToHide.
	 * Returns new image.
	 * @throws IOException
	 */
	private static BufferedImage replaceImage(BufferedImage startImage,
			ArrayList<Character> bitsToHide, int numLSB, int width, int height, int whichPixels) throws IOException	{
		int counter = 0;
		int c2 = 0;
		BufferedImage retval = new BufferedImage(startImage.getWidth(),startImage.getHeight(),1);
		int[] rgb;
		retval.setRGB(0, 0, numLSB + 256*whichPixels);
		retval.setRGB(1, 0, width);
		retval.setRGB(2, 0, height);
		for (int i = 0; i < startImage.getHeight(); i++) // for each row
			for(int j = 0; j < startImage.getWidth(); j++) { // for each column
				if (c2 >= 3) { // ignore first three (metadata) bytes
					rgb = CommonMethods.getPixelData(startImage, j, i);
					if (CommonMethods.useItOrNot (c2, whichPixels)) { // if this byte will get data
						for (int k = 0; k < 3; k++) { // for each color byte
							int newData = 0;
							// if still data left to hide
							if (counter + numLSB < bitsToHide.size())
								for (int l = 0; l < numLSB; l++)
									newData += (bitsToHide.get(counter+l)-'0') * (int) (Math.pow(2, numLSB - l - 1));
							rgb[k] = (rgb[k] - (int) (rgb[k]%Math.pow(2, numLSB)) + newData);
							counter+=numLSB;
						}
					}
					retval.setRGB(j, i, rgb[0]*256*256 + rgb[1]*256 + rgb[2]);
				}
				c2++;
			}
		return retval;
	}

	/**
	 * @param list An array list of integers representing color data.
	 * @return bits An array list of strings.
	 * Turn each int into its binary representation. Add zeroes
	 * to ensure length 8.
	 */
	private static ArrayList<String> getBitsFromIntArray (ArrayList<Integer> list) {
		ArrayList<String> bits = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			String temp = Integer.toBinaryString(list.get(i));
			while (temp.length() < 8)
				temp = "0" + temp;
			bits.add(temp);
		}
		bits.add("00000100");
		return bits;
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
}
