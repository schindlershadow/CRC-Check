/* 
 * Checker.java
 * Copyright (C) 2014 Youssef Al Hindi <schindlershadow@gmail.com>
 * 
 * Checker is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Checker will be published to the public
 * one (1) week after the due date of 11:59 pm November 7, 2014
 * 
 * Checker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Checker {
	private static String polynomial = "10100101010001101";

	public static void main(String[] args) {

		// Check if inputs are valid
		if (args.length == 1
				&& (args[0].equals("--help") || args[0].equals("help") || args[0]
						.equals("?"))) {
			System.out
					.println("\nChecker is a program that calculates the CRC­-16 "
							+ "value for a given file \nand which can also verify the correctness"
							+ " of a given file that already has a \nCRC-­16 value appended to it");
			printERR();
			System.out
					.println("FLAGS:	\n\tc: Calculate a CRC\n\tv: Verify a CRC");
			System.out
					.println("\nOPTIONS: \n\t--help: Print this help screen\n\thelp: Print this help screen\n\t"
							+ "?: Print this help screen\n");
			System.out
					.println("REQUREMENTS:\n\tFILENAME: must be plaintext and hex values only");
			return;
		}
		if (args.length != 2) {
			printERR();
			return;
		}
		String filename = args[1];
		// Check input file
		if (!isValidTxt(filename)) {
			System.out.println("\nInput file is not Vaild\n");
			printERR();
			return;
		}

		System.out.println("The input file (hex):\n"
				+ getHex(filename).toUpperCase()
				+ "\n\nThe input file (bin):\n"
				+ format(toBinary(getHex(filename))) + "\n");
		System.out
				.println("The polynomial that was used (binary bit string): 1010 0101 0100 0110 1");
		System.out.println("We will append 16 zeroes at the end of the binary input.");

		// Check Flags
		if (args[0].equals("-c") || args[0].equals("c")) {
			calcCRC(getHex(filename), true);
		} else if (args[0].equals("-v") || args[0].equals("v")) {
			boolean check = verifyCRC(getHex(filename));
			System.out.print("\nDid the CRC check pass? (Yes or No): ");

			if (check) {
				System.out.print("Yes");
			} else {
				System.out.print("No");
			}
			System.out.println();
		} else {
			printERR();
			return;
		}

	}

	public static void printERR() {
		System.out
				.println("\nUSEAGE:\n\tjava Checker [FLAG c|v] [FILENAME]\n\tjava Checker -[FLAG c|v] [FILENAME]"
						+ "\n\tjava Checker --help\n\tjava Checker help\n\tjava Checker ?\n");
	}

	public static String toBinary(String hex) {
		String bin = "", binPart = "";
		long numHex;

		for (int i = 0; i < hex.length(); i++) {
			numHex = Long.parseLong(hex.charAt(i) + "", 16);
			binPart = Long.toBinaryString(numHex);

			while (binPart.length() < 4) {
				binPart = "0" + binPart;
			}
			bin += binPart;
		}
		return bin;
	}

	public static String toHex(String binary) {
		return Long.toHexString(Long.parseLong(binary, 2));
	}

	public static boolean isValidTxt(String filename) {
		File f = new File(filename);
		if (f.exists()) {
			String pattern = ".[a-f]|[A-F]|[0-9]";
			Pattern pat = Pattern.compile(pattern);

			try {
				List<String> list = Files.readAllLines(Paths.get(filename),
						Charset.defaultCharset());
				for (String line : list) {
					for (int i = 0; i < line.length(); i++) {
						Matcher match = pat.matcher(line.toUpperCase().charAt(i) + "");
						if (!(Character.isDigit(line.charAt(i))
								|| Character.isLetter(line.charAt(i)) )) {
							return false;
						}
						if(!match.matches()){
							return false;
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			System.out.println("\nFile " + filename
					+ " does not exist or is unreadable");
			return false;
		}
		return true;
	}

	public static String XOR(String poly, String bin, int len) {

		String out = "";
		int diff = bin.indexOf('1');

		for (int i = 0; i < diff; i++) {
			out += "0";
		}

		if (poly.length() + diff < len) {
			while (poly.length() + diff != len) {
				poly += '0';
			}
		}
		
		if(diff < 0){
			return bin;
		}

		// System.out.println("bin " + bin + " diff " + diff + "\n" );

		for (int i = 0; i < poly.length(); i++) {
//			 System.out.println("i: " + i + " diff " + diff + " i+diff"
//			 + (i + diff) + " bin: " + bin + " len " + bin.length()
//			 + " \n");
			if (i + diff < len) {
				out = out
						+ intToStr(Character.getNumericValue(poly.charAt(i))
								^ Character.getNumericValue(bin
										.charAt(i + diff)));
			}
		}

		// add 0's at the end
		while (out.length() < len) {
			out += "0";
		}

		return out.substring(0, len);
	}

	public static String calcCRC(String hex, boolean mode) {
		if (mode)
			System.out
					.println("\nThe binary string difference after each XOR step of the CRC calculation\n");
		// convert hex to binary
		String bin = toBinary(hex);

		// add 16 0's at the end
		for (int i = 0; i < 16; i++) {
			bin = bin + '0';
		}

		printFormatted(bin);
		int len = bin.length();

		// calculate crc
		if (mode)
			printFormatted(XOR(polynomial, bin, len));
		String crc = XOR(polynomial, bin, len);
		while (crc.indexOf('1') > 0 && crc.substring(crc.indexOf('1')).length() > 16) {
			// if (mode)
			// System.out.println("\n" + crc + "\n");
			String temp = XOR(polynomial, crc, len);
			printFormatted(temp);
			crc = XOR(polynomial, crc, len);
		}
		if (mode)
			System.out.println("\nThe computed CRC for the input file is: \n"
					+ format(crc) + " (bin) = " + toHex(crc).toUpperCase()
					+ " (hex)");
		return crc;
	}

	public static boolean verifyCRC(String hex) {
		String bin = toBinary(hex);
		String crc = bin.substring(bin.length() - 16);

		System.out.println("The 16-bit CRC observed at the end of the file: "
				+ format(crc) + " (bin) = " + toHex(crc).toUpperCase()
				+ " (hex)\n");

		String check = calcCRC(hex.substring(0, hex.length() - 4), false);
		check = check.substring(check.indexOf('1'));
		System.out.println("\nThe computed CRC for the input file is: "
				+ format(check) + " (bin) = " + toHex(check).toUpperCase()
				+ " (hex)");
		// System.out.println("\n\n" + crc + "\n" + check);

		if (crc.equals(check)) {
			return true;
		} else {
			return false;
		}
	}

	private static void printFormatted(String input) {
		for (int i = 0; i < input.length(); i++) {
			if (i % 4 == 0 && i > 3) {
				System.out.print(" ");
			}
			if (i % 64 == 0 && i > 63) {
				System.out.print("\n");
			}
			if (i < input.length()) {
				System.out.print(input.charAt(i));
			} else {
				System.out.print("0");
			}
		}
		System.out.print("\n");
	}

	private static String format(String input) {
		String output = "";
		for (int i = 0; i < input.length(); i++) {
			if (i % 4 == 0 && i > 3) {
				output += " ";
			}
			if (i % 64 == 0 && i > 63) {
				output += '\n';
			}
			if (i < input.length()) {
				output += (input.charAt(i));
			} else {
				output += "0";
			}
		}
		return output;
	}

	private static String getHex(String filename) {
		File f = new File(filename);
		if (f.exists()) {
			try {
				List<String> list = Files.readAllLines(Paths.get(filename),
						Charset.defaultCharset());
				for (String line : list) {
					return line;
				}
			} catch (IOException e) {
				e.printStackTrace();

			}
		} else {
			System.out.println("File " + filename
					+ " does not exist or is unreadable");
		}
		return "";

	}

	private static String intToStr(int num) {
		return Integer.toString(num);
	}

}
