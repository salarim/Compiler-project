
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.print.attribute.standard.NumberUp;
import javax.xml.stream.events.Comment;

public class Scanner_class {
	static ArrayList<Token> tokens = new ArrayList<Token>();
	private static Scanner scanner;

	static int look_ahead = 0, counter = 100;
	static int last_look_ahead = 0;
	static String input;

	static ArrayList<String> reserve = new ArrayList<String>();
	static ArrayList<Character> letters = new ArrayList<Character>();

	static boolean operator_detector = false;
	
	public static void main(String[] args) {

		try {
			CreateParseTable.createParseTable();
			Parser.FilltableColumnNames();
			Parser.Fillrules();
			Parser.fillVariables();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		scanner = new Scanner(System.in);
		// while((input = scanner.next()) != null){
		// Token token = new Token(input, null, null);
		// Parser.parse(token);
		// }
		// input = scanner.nextLine();
		//
		// input = input + "$";

		reserve.add("EOF");
		reserve.add("int");
		reserve.add("void");
		reserve.add("if");
		reserve.add("else");
		reserve.add("while");
		reserve.add("return");
		reserve.add("output");

		letters.add('[');
		letters.add(';');
		letters.add(']');
		letters.add('(');
		letters.add(')');
		letters.add(',');
		letters.add('{');
		letters.add('}');
		letters.add('=');
		letters.add('<');
		letters.add('+');
		letters.add('-');
		letters.add('*');
		letters.add('/');
		letters.add('&');
		while ((input = scanner.nextLine()) != null) {
			look_ahead = 0;
			input = input + "$";
			while (look_ahead < input.length()) {

				if (input.charAt(look_ahead) == ' ') {

					look_ahead++;
				} else if (Character.isLetter(input.charAt(look_ahead))) {
					ID();
				} else if (Character.isDigit(input.charAt(look_ahead)) || (operator_detector == false
						&& (input.charAt(look_ahead) == '+' || input.charAt(look_ahead) == '-')
						&& Character.isDigit(input.charAt(look_ahead + 1)))) {
					Num();
				} else if (input.charAt(look_ahead) == '/' && input.charAt(look_ahead + 1) == '*') {
					Comment();
				} else if (input.charAt(look_ahead) == '=') {
					equal();
				} else if (input.charAt(look_ahead) == '&' && input.charAt(look_ahead + 1) == '&') {
					operator_detector = false;
					tokens.add(new Token("&&", null, null));
					Parser.parse(tokens.get(tokens.size() - 1));
					look_ahead+=2;
				} else if (letters.contains(input.charAt(look_ahead))) {
					operator_detector = false;
					tokens.add(new Token(Character.toString(input.charAt(look_ahead)), null, null));
					Parser.parse(tokens.get(tokens.size() - 1));
					look_ahead++;
				} else {
					
					System.out.println("scanner panic mode");
					look_ahead++;
				}
				// System.out.println(look_ahead);
			}
			if(input.contains("EOF")){
				tokens.add(new Token("$", null, null));
				Parser.parse(tokens.get(tokens.size() - 1));
				break;
			}
		}
//		tokens.add(new Token("$", null, null));
//		Parser.parse(tokens.get(tokens.size() - 1));
		
		// System.out.println(tokens.size());
		// for (int i = 0; i < tokens.size(); i++) {
		// System.out.println(tokens.get(i).type);
		// }
		IntermediateCodeGenerator.printPB();
	}

	static public void ID() {
		String current = "";
		current = current + input.charAt(look_ahead);
		while (true) {
			look_ahead++;
			if (letters.contains(input.charAt(look_ahead)) || look_ahead == input.length() - 1
					|| input.charAt(look_ahead) == ' ') {
				if (reserve.contains(current)) {
					tokens.add(new Token(current, null, null));
					Parser.parse(tokens.get(tokens.size() - 1));
				} else {
					tokens.add(new Token("ID", current, counter));
					tokens.get(tokens.size() - 1).type2 = tokens.get(tokens.size() - 2).type;
					Parser.parse(tokens.get(tokens.size() - 1));
					counter++;
				}
				operator_detector = true;
				break;
			} else if (Character.isLetter(input.charAt(look_ahead)) || Character.isDigit(input.charAt(look_ahead))) {
				current = current + input.charAt(look_ahead);
			} else {
				System.out.println("scanner panic mode");
				// panic
				look_ahead++;
				break;
			}
		}
	}

	static public void Num() {
		String current = "";
		current = current + input.charAt(look_ahead);
		while (true) {
			look_ahead++;
			if (Character.isDigit(input.charAt(look_ahead))) {
				current = current + input.charAt(look_ahead);
			} else if (Character.isLetter(input.charAt(look_ahead))) {
				// panic
				look_ahead++;
				break;
			} else {
				operator_detector = true;
				tokens.add(new Token("NUM", current, null));
				Parser.parse(tokens.get(tokens.size() - 1));
				break;
			}
		}
	}

	static public void Comment() {
		look_ahead++;
		while (look_ahead + 1 < input.length()) {
			look_ahead++;
			if (input.charAt(look_ahead) == '*' && input.charAt(look_ahead + 1) == '/') {
				look_ahead++;
				look_ahead++;
				break;
			}
		}
	}

	static public void equal() {
		operator_detector = false;
		if (input.charAt(look_ahead + 1) == '=') {
			look_ahead = look_ahead + 2;
			tokens.add(new Token("==", null, null));
			Parser.parse(tokens.get(tokens.size() - 1));
		} else {
			look_ahead++;
			tokens.add(new Token("=", null, null));
			Parser.parse(tokens.get(tokens.size() - 1));
		}
	}

}
