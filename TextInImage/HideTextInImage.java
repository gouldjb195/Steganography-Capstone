import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import javax.imageio.ImageIO;

/**
 * Takes a text file and hides it in an image using the x-least significant
 * bits (as chosen by the user).
 */
public class HideTextInImage{
	static Scanner sc = new Scanner(System.in);
	static DecimalFormat df = new DecimalFormat("#.##");
	static HashMap<String, Integer> combos = new HashMap<String, Integer>();
	static HashMap<Character, String> input = new HashMap<Character, String>();

	/**
	 * @params args (unused)
	 * output: An image file containing the text chosen to be hidden by the user.
	 *
	 * Prompts user for file names.
	 * Opens image file to be altered.
	 * Converts desired text file into a series of strings of zeroes and ones
	 * of length eight. Turns these into one long arraylist of characters.
	 * Determines appropriate number of LSB, and whether this text file will fit
	 * in this image.
	 * Calls replaceImage to store bit data in new output image.
	 * Saves output image.
	 */
	public static void main(String[] args) throws IOException {

		addCombos();
		addInputs();

		System.out.print("Image file to host hidden text: ");
		String start_image_name = CommonMethods.verifyValidFileName(sc.next(), sc);
		System.out.print("Text file to be hidden: ");
		String text_file_name = CommonMethods.verifyValidFileName(sc.next(), sc);

		System.out.print("Encode text (y/n): ");
		boolean encrypt = getYesNo(sc.next(), sc);

		ArrayList<String> eachCharAsStringOfEightBits = getBitsFromFilename(text_file_name, encrypt);
		ArrayList<Character> listOfZerosAndOnes = new ArrayList<Character>();
		for (int i = 0; i < eachCharAsStringOfEightBits.size(); i++)
			for (int j = 0; j < 8; j++)
				listOfZerosAndOnes.add(eachCharAsStringOfEightBits.get(i).charAt(j));
		int bitsRequired = listOfZerosAndOnes.size();

		BufferedImage start_image = ImageIO.read(new File(start_image_name));
		int maxBitsAvailable = start_image.getHeight() * start_image.getWidth()*9;
		double ratio = (double) bitsRequired / maxBitsAvailable;
		System.out.println("Bits required: " + bitsRequired);
		System.out.println("Max bits available: " +maxBitsAvailable);
		System.out.println("Ratio: " + df.format(ratio));
		int whichCombo = comboMenu(whatAreOptions(ratio, start_image.getHeight()*start_image.getWidth()));
		int numLSB = whichCombo%10;
		int iWhichPixels = whichCombo/10;

		System.out.print("Output image name (.png will be auto-appended): ");
		String new_file_name = CommonMethods.testForOverwrite(sc.next() + ".png",sc);

		BufferedImage end_image = replaceImage(start_image, listOfZerosAndOnes, numLSB, iWhichPixels);
		ImageIO.write(end_image, "PNG", new File(new_file_name));
		System.out.println("Done!");
	}

	/**
	 * @param Image to be replaced with similar image hiding text,
	 * array list of characters containing zeros and ones to be hidden,
	 * number of least significant bits to mask over.
	 *
	 * For each pixel except the first, pulls rgb data.
	 * (First pixel gets the LSB data.)
	 * Grabs the next numLSB-many bits, converts them to an integer.
	 * Replaces the numLSB-many LSB with this data.
	 */
	private static BufferedImage replaceImage(BufferedImage startImage,
			ArrayList<Character> ZeroesAndOnes, int numLSB, int iWhichPixels) {
		int offsetIntoZeroesAndOnes = 0;
		int currentPixel = 0;
		BufferedImage retval = new BufferedImage(startImage.getWidth(),startImage.getHeight(),1);
		int[] rgb;
		for (int column = 0; column < startImage.getHeight(); column++)
			for(int row = 0; row < startImage.getWidth(); row++)
			{
				rgb = CommonMethods.getPixelData(startImage, row, column);
				if ((column != 0 || row != 0) &&  CommonMethods.useItOrNot (currentPixel, iWhichPixels))
					for (int whichColorByte = 0; whichColorByte < 3; whichColorByte++) {
						int newData = 0;
						if (offsetIntoZeroesAndOnes < ZeroesAndOnes.size())
							for (int i = 0; i < numLSB; i++)	{
								newData += (ZeroesAndOnes.get(offsetIntoZeroesAndOnes+i)-'0') 
										* (int) (Math.pow(2, numLSB - i - 1));
							}
						rgb[whichColorByte] = (rgb[whichColorByte] - (int) (rgb[whichColorByte]%Math.pow(2, numLSB)) + newData);
						offsetIntoZeroesAndOnes+=numLSB;
					}
				int final_color = rgb[0]*256*256 + rgb[1]*256 + rgb[2];
				retval.setRGB(row, column, final_color);
				currentPixel++;
			}
		int[] oldFirstPixel = CommonMethods.getPixelData(startImage, 0, 0);
		int blue =  oldFirstPixel[2];
		blue = numLSB + 16*iWhichPixels;
		retval.setRGB(0, 0, 256*256*oldFirstPixel[0] + 256*oldFirstPixel[1] + blue);
		return retval;
	}

	/**
	 * @params A string representing a (known extant) file from which text will be pulled.
	 * Scans text file line by line. Turns each character into the binary string
	 * representation of its ASCII value.
	 * Adds these to an arraylist of strings.
	 * Adds endlines as appropriate and EOF marker at end.
	 */
	private static ArrayList<String> getBitsFromFilename (String fileName, boolean encrypt) 
			throws FileNotFoundException	{
		File textFile = new File(fileName);
		Scanner sc = new Scanner(textFile);
		ArrayList<String> bits = new ArrayList<String>();
		while (sc.hasNextLine()) {
			String nextLine = sc.nextLine();
			for (int i = 0; i < nextLine.length(); i++)	{
				int nextChar = (int)nextLine.charAt(i);
				if (encrypt) // Atbash cipher
				{
					if (nextChar >=65 && nextChar <=90)
						nextChar = 155 - nextChar;
					if (nextChar >=97 && nextChar <=122)
						nextChar = 219 - nextChar;
				}
				if (nextChar <=255)	{
					String temp = Integer.toBinaryString(nextChar);
					while (temp.length() < 8)
						temp = "0" + temp;
					bits.add(temp);
				}
			}
			bits.add("00001010");
		}
		for (int i = 0; i < 8; i++)
			bits.add("00001010");
		bits.add("00000100");
		return bits;
	}

	/**
	 * @param ratio Ratio of space needed to space available.
	 * @param pixelsTotal Number of pixels available.
	 * @return How many menu options to print
	 *
	 * If the text will not fit, says so. Otherwise, determines which
	 * possible lsb/pixel combos to present to the user.
	 */
	private static int whatAreOptions(double ratio, int pixelsTotal) {
		if (ratio > 1)
		{
			System.out.println("The text will not fit.");
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

	private static boolean getYesNo(String response, Scanner sc)
	{
		String test = "yn";
		while (test.indexOf(response.toLowerCase().charAt(0)) == -1)
		{
			System.out.print("Response not recognized. Try again: ");
			response = sc.next();
		}
		if (response.toLowerCase().charAt(0) == 'y')
			return true;
		return false;
	}

}