
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class CreateParseTable {
	// static String[][] parseTable;
	static int colsNum = 27 + 55;
	static int rowsNum = 142;

	static void createParseTable() throws IOException {
		int ii = 0;
		int jj = 0;

		Scanner scanner = new Scanner(System.in);
		Parser.parseTable = new String[rowsNum][colsNum];
		// String input;
		int col = 0;
		int row = 0;
		// String inputt = scanner.nextLine();
		// System.out.println(inputt);
		BufferedReader br = new BufferedReader(new FileReader("output_of_parser.txt"));
		try {
			StringBuilder sb = new StringBuilder();
			String inputt = br.readLine();
			for (int i = 0; i < inputt.length(); i++) {
				if (inputt.charAt(i) == '#') {
					continue;
				} else {
					String tmp = "";
					boolean changed = false;
					for (; i < inputt.length() && inputt.charAt(i) != '#'; i++) {
						if (i == inputt.length()) {
							break;
						}
						if (Character.isDigit(inputt.charAt(i)) || Character.isLetter(inputt.charAt(i)))
							changed = true;
						tmp = tmp + inputt.charAt(i);
					}
					// System.out.println(ii + " "+ jj);
					if (jj != 0) {
						if (changed)
							Parser.parseTable[ii][jj - 1] = tmp;
						else
							Parser.parseTable[ii][jj - 1] = null;
//						System.out.println(ii + " " + (jj - 1) + " " + Parser.parseTable[ii][jj - 1]);
					}
					jj++;
					if (jj == colsNum + 1) {
						jj = 0;
						ii++;
					}
				}
			}
		} finally {
			br.close();
		}
//		System.out.println(Parser.parseTable[1][9]);

	}
}
