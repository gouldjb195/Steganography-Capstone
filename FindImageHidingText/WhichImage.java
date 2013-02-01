//package findTheImageWithHiddenText;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;
import javax.imageio.ImageIO;

/**
 * Looks at multiple images. Determines which are most likely to
 * be hiding text.
 */
public class WhichImage3 {
	static ArrayList<String> results = new ArrayList<String>();
	static Scanner sc = new Scanner(System.in);
	static DecimalFormat df = new DecimalFormat("#.##");

	/**
	 */
	public static void main (String[] args) throws IOException {
		ArrayList<File> images = getImages();
		getPercentages(images);
		System.out.println("The maximum letter/number/punctuation ratio for each image:\n");
		for (String i: results)
			System.out.println(i);
		System.out.println();
	}

	/**
	 */
	private static void getPercentages(ArrayList<File> images) throws IOException {
		for (int i = 0; i < images.size(); i++)
		{
			double thisScore = 0;
			double highScore = 0;
			ArrayList<String> patterns = populatePatterns();
			BufferedImage nextImage = ImageIO.read(images.get(i));
			ArrayList<Character> bits = getBits(nextImage);
			for (int j = 0; j < patterns.size(); j++)
			{
				ArrayList<Character> thisPattern = new ArrayList<Character>();
				for (int k = 0; k < bits.size(); k++)
					if (patterns.get(j).charAt(k%patterns.get(j).length()) == '1')
						thisPattern.add(bits.get(k));
				thisScore = getData(thisPattern)*100;
				if (thisScore > highScore)
					highScore = thisScore;
			}
			results.add(images.get(i).getName() + ": " + df.format(highScore) + "%");

		}
	}

	private static ArrayList<String> populatePatterns() {
		ArrayList<String> patterns = new ArrayList<String>();
		patterns.add("1");
		patterns.add("011");
		patterns.add("001");
		patterns.add("000000000111111111");
		patterns.add("000000000011011011");
		patterns.add("000000000001011001");
		patterns.add("111111111000000000");
		patterns.add("011011011000000000");
		patterns.add("001001001000000000");
		patterns.add("000000000000000000111111111");
		patterns.add("000000000000000000011011011");
		patterns.add("000000000000000000001001001");
		return patterns;
	}

	private static double getData(ArrayList<Character> bits) {
		int numChars = 0;
		int numLetters = 0;
		String commonPunk = " .,/!\"\\()";
		for (int i = 9; i < bits.size()-8; i+=8)
		{
			String next = "";
			for (int j = 0; j < 8; j++)
				next += bits.get(i + j);
			int temp = binStringToInt(next);
			char cNext = Character.toChars(temp)[0];
			if (Character.isLetter(cNext) || cNext == '*' || commonPunk.indexOf(cNext) != -1)
				numLetters++;
			if (temp != 0)
				numChars++;
		}
		double score = (double) numLetters / numChars;
		//System.out.println(score);
		return score;
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

	private static ArrayList<Character> getBits (BufferedImage thisImage)
	{
		ArrayList<Character> bits = new ArrayList<Character>();
		for (int column = 0; column < thisImage.getHeight(); column++)
		{
			for (int row = 0; row < thisImage.getWidth(); row++)
			{
				int[] rgb = getPixelData(thisImage, row , column);
				for (int whichColorByte = 0; whichColorByte < 3; whichColorByte++)
				{
					String inBinary = Integer.toBinaryString(rgb[whichColorByte]%8);
					while (inBinary.length() < 3)
						inBinary = "0" + inBinary;
					for (int whichChar = 0; whichChar < 3; whichChar++)
						bits.add(inBinary.charAt(whichChar));
				}
			}
		}
		return bits;
	}
}
