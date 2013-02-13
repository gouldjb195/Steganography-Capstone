import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.imageio.ImageIO;

/**
 * Removes text hidden in image by ReplacesLSBWithText.
 */
public class RetrieveTextFromImage {
	static Scanner sc = new Scanner(System.in);
	/**
	 * Scanner takes filename as input, checks its validity.
	 * Scanner takes filename for output file. Checks for existence.
	 * If it already exists, asks if user wants to overwrite.
	 * Pulls RGB on first pixel to get number of LSB used.
	 * Calls decode and decodeBinary to create appropriate text.
	 * Writes text to verified output file.
	 */
	public static void main(String[] args) throws IOException
	{
		System.out.print("Enter a file to decode: ");
		String fileName = CommonMethods.verifyValidFileName(sc.next(),sc);

		System.out.print("Enter a destination file name (\".txt\" will be appended automatically): ");
		String outputFileName = CommonMethods.testForOverwrite(sc.next() + ".txt", sc);
		FileWriter writer = new FileWriter(outputFileName);

		BufferedImage start_image = ImageIO.read(new File(fileName));

		int numLSB = 0;
		int iWhichPixels = 0;
		System.out.print("Ignorant search (y/n)? ");
		boolean response = CommonMethods.getYesNo(sc.next(), sc);
		if (response)
		{
			numLSB = 1;
			iWhichPixels = 1;
		}
		else
		{
			int[] firstPixel = CommonMethods.getPixelData(start_image, 0, 0);
			numLSB = firstPixel[2]%16;
			iWhichPixels = firstPixel[2]/16;
		}

		System.out.println("Checking " + numLSB + " least-significant bits.");
		System.out.println("Checking pixel pattern #" + iWhichPixels);

		ArrayList<Character> hiddenJunkInBinary = decode(start_image, numLSB, iWhichPixels);
		ArrayList<Character> hiddenText = decodeBinary(hiddenJunkInBinary);
		for (int i = 0; i < hiddenText.size(); i++)
			writer.write(hiddenText.get(i));
		writer.close();
		System.out.println("Done!");
	}

	/**
	 * @param hiddenText (an arraylist of characters (zeroes and
	 * ones) forming the binary representation of the encoded text.)
	 * @return an arraylist of ASCII characters.
	 * @throws IOException
	 */
	private static ArrayList<Character> decodeBinary(ArrayList<Character> hiddenText) 
			throws IOException {
		ArrayList<Character> retval = new ArrayList<Character>();
		for (int i = 0; i < hiddenText.size()-8; i+=8) {
			String nextCharBinString = "";
			for (int j = 0; j < 8; j++)
				nextCharBinString = nextCharBinString + hiddenText.get(i + j);
			if (nextCharBinString.equals("00000100")) // end of file marker
				return retval;
			char nextChar = (char) Integer.parseInt(nextCharBinString, 2);
			retval.add(nextChar);
		}
		return retval;
	}


	private static ArrayList<Character> decode(BufferedImage startImage, 
			int numLSB, int iWhichPixels) {
		int[] rgb;
		ArrayList<Character> retval = new ArrayList<Character>();
		for(int column = 0; column < startImage.getHeight(); column++)
			for(int row = 0; row < startImage.getWidth(); row++)
				if (column != 0 || row != 0) {
					if (CommonMethods.useItOrNot(column*startImage.getWidth()+row, iWhichPixels))
					{
						rgb = CommonMethods.getPixelData(startImage, row, column);
						for (int whichColorByte = 0; whichColorByte < 3; whichColorByte++) {
							String temp = Integer.toBinaryString(rgb[whichColorByte] 
									% (int) Math.pow(2, numLSB));
							while (temp.length() < numLSB)
								temp = "0" + temp;
							for (int q = 0; q < temp.length(); q++)
								retval.add(temp.charAt(q));
						}
					}
				}

		return retval;
	}
}
