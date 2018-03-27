
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Parser {
	static String[][] parseTable;
	static int varColStart = 27;
	static ArrayList<String> tableColumnNames;
	static String[] temp_for_table = { "EOF", "int", "ID", ";", "[", "NUM", "]", "(", ")", "void", ",", "{", "}",
			"output", "=", "if", "else", "while", "return", "&&", "==", "<", "+", "-", "*", "/", "$", "Program",
			"Save2", "DeclarationList", "Declaration", "VarDeclaration", "SetScope", "FunDeclaration", "Set1", "Jmp1",
			"Params", "ParamList", "AddParam", "Param", "Pid1", "CompoundStmt", "AddScope", "SubScope",
			"LocalDeclarations", "StatementList", "Statement", "ExpressionStmt", "Assign1", "SelectionStmt", "Save1",
			"Ifjpf1", "Ifjp1", "IfJpfSave1", "IterationStmt", "Label1", "WhileFunc1", "ReturnStmt", "Return1",
			"ReturnFunc1", "Var", "FindAddr", "GetArrayPointer1", "GenExpression", "RelExpression", "RelTerm", "Equal1",
			"Less1", "Expression", "Add", "Sub1", "Term", "Mult1", "Div1", "Factor", "Pval1", "Call", "Call1",
			"LastFuncCalled1", "Args", "AssignParams1", "ArgList"};
	static ArrayList<String> stack = new ArrayList<>();
	static ArrayList<Rule> rules = new ArrayList<Rule>();
	static ArrayList<Variable> variables;
	static boolean inPanicMode = false;

	static void FilltableColumnNames() {
		// TODO you must call it
		tableColumnNames = new ArrayList<>();
		for (int i = 0; i < temp_for_table.length; i++) {
			tableColumnNames.add(temp_for_table[i]);
		}
	}

	static void Fillrules() throws IOException {
		// TODO you must call it
		BufferedReader br = new BufferedReader(new FileReader("grammar.txt"));
		BufferedReader br2 = new BufferedReader(new FileReader("grammarV3.txt"));
		try {
			String line = br.readLine();
			String line2 = br2.readLine();
			while (line != null) {
				String left = "";
				int rightnum = 0;
				int tmp = 0;
				for (int j = 0; j < line.length(); j++) {
					if (line.charAt(j) != ' ') {
						left = left + line.charAt(j);
					} else {
						j = j + 3;
						tmp = j;
						break;
					}
				}
				// if(left.equals("Save1"))
				// System.out.println(line);
				boolean seeSpace = false;
				for (int j = tmp; j < line.length(); j++) {
					if (line.charAt(j) == ' ' || line.charAt(j) == '\'') {
						seeSpace = true;
					} else if (seeSpace) {
						seeSpace = false;
						rightnum++;
					}
				}
				String[] line2Tokens = line2.split(" ");
				String func = null;
				for (String str : line2Tokens)
					if (str.startsWith("#"))
						func = str.substring(1, str.length());
				Rule rule = new Rule(rightnum, left, func);
				rules.add(rule);
				line = br.readLine();
				line2 = br2.readLine();
			}
		} finally {
			br.close();
		}
	}

	static void fillVariables() throws IOException {
		variables = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader("variablesAndFollows.txt"));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				String[] tokens = line.split("\t");
				tokens[2] = tokens[2].substring(1, tokens[2].length() - 1);
				String[] follows = tokens[2].split(",");
				ArrayList<String> fllw = new ArrayList<>();
				boolean addedSimicalon = false;
				for (String str : follows) {
					if (str.equals("") && !addedSimicalon) {
						fllw.add(",");
						addedSimicalon = true;
					} else if(!str.equals(""))
						fllw.add(str);
				}
				variables.add(new Variable(tokens[0], fllw));
				line = br.readLine();
			}
		} finally {
			br.close();
		}
	}

	static void parse(Token token) {
		System.out.println("Token pass to parser: " + token.type);
		if (stack.size() == 0) {
			stack.add("0");
		}
		int col = tableColumnNames.indexOf(token.type);
		int row = Integer.valueOf(stack.get(stack.size() - 1));
		if (inPanicMode) {
			ArrayList<String> follow = new ArrayList<>();
			for (Variable var : variables)
				if (var.name.equals(stack.get(stack.size() - 2)))
					follow = var.follow;
			if (follow.contains(token.type)) {
				inPanicMode = false;
				parse(token);
			}
		} else if (parseTable[row][col] == null) {
			System.out.println(row + " " + col);
			System.out.println("start panic mode!****");
			String panicVar = null;
			String n = null;
			for (int i = stack.size() - 1; i > 0; i -= 2) {
				boolean notEmpty = false;
				int state = Integer.valueOf(stack.get(i));
				for (int j = varColStart; j < tableColumnNames.size(); j++)
					if (parseTable[state][j] != null) {
						notEmpty = true;
						panicVar = tableColumnNames.get(j);
						n = parseTable[state][j];
						// System.out.println("n" + n);
						break;
					}
				if (notEmpty) {
					while (stack.size() > i + 1)
						stack.remove(stack.size() - 1);
					break;
				}
			}
			inPanicMode = true;
			// System.out.println("panicVar: " + panicVar + " n: " + n);
			stack.add(panicVar);
			stack.add(n);
			printStack();
		} else if (parseTable[row][col].charAt(0) == 'a') {
			stack.clear();
			System.out.println("Accepted");
		} else if (parseTable[row][col].charAt(0) == 's') {
			String newState = parseTable[row][col].substring(1, parseTable[row][col].length());
			stack.add(token.type);
			stack.add(newState);
			printStack();
		} else if (parseTable[row][col].charAt(0) == 'r') {
			int usedRuleIndex = Integer.valueOf(parseTable[row][col].substring(1, parseTable[row][col].length()));
			Rule usedRule = rules.get(usedRuleIndex);
			// System.out.println("rull: " + usedRule.leftSideLength + " " +
			// usedRule.rightSide);
			for (int i = 0; i < 2 * usedRule.leftSideLength; i++)
				stack.remove(stack.size() - 1);
			stack.add(usedRule.rightSide);
			int col2 = tableColumnNames.indexOf(usedRule.rightSide);
			int row2 = Integer.valueOf(stack.get(stack.size() - 2));
			stack.add(parseTable[row2][col2]);
			printStack();
			// TODO call proper function to generate code
			if (usedRule.callFunction != null)
				IntermediateCodeGenerator.generateCode(usedRule.callFunction);
			parse(token);
		}

	}

	static void printStack() {
		System.out.print("Stack: ");
		for (String str : stack)
			System.out.print(str + " ");
		System.out.println();
	}

}

class Rule {
	int leftSideLength;
	String rightSide;
	String callFunction;

	public Rule(int leftSideLength, String rightSide, String callFunction) {
		this.leftSideLength = leftSideLength;
		this.rightSide = rightSide;
		this.callFunction = callFunction;
	}
}

class Variable {
	String name;
	ArrayList<String> follow;

	public Variable(String name, ArrayList<String> follow) {
		this.name = name;
		this.follow = follow;
	}
}