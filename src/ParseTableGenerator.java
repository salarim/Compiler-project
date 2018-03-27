import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ParseTableGenerator {
	static ArrayList<Item> rules = new ArrayList<>();
	static ArrayList<State> states = new ArrayList<>();
	static ArrayList<Variable> variables;
	static String[] temp_for_table = { "EOF", "int", "ID", ";", "[", "NUM", "]", "(", ")", "void", ",", "{", "}",
			"output", "=", "if", "else", "while", "return", "&&", "==", "<", "+", "-", "*", "/", "$", "Program",
			"Save2", "DeclarationList", "Declaration", "VarDeclaration", "SetScope", "FunDeclaration", "Set1", "Jmp1",
			"Params", "ParamList", "AddParam", "Param", "Pid1", "CompoundStmt", "AddScope", "SubScope",
			"LocalDeclarations", "StatementList", "Statement", "ExpressionStmt", "Assign1", "SelectionStmt", "Save1",
			"Ifjpf1", "Ifjp1", "IfJpfSave1", "IterationStmt", "Label1", "WhileFunc1", "ReturnStmt", "Return1",
			"ReturnFunc1", "Var", "FindAddr", "GetArrayPointer1", "GenExpression", "RelExpression", "RelTerm", "Equal1",
			"Less1", "Expression", "Add", "Sub1", "Term", "Mult1", "Div1", "Factor", "Pval1", "Call", "Call1",
			"LastFuncCalled1", "Args", "AssignParams1", "ArgList" };

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
					} else if (!str.equals(""))
						fllw.add(str);
				}
				variables.add(new Variable(tokens[0], fllw));
				line = br.readLine();
			}
		} finally {
			br.close();
		}
	}

	static String[][] createParseTable() {
		String[][] parseTable = new String[states.size()][temp_for_table.length];
		for (int i = 0; i < states.size(); i++) {
			for (Item item : states.get(i).items)
				if (item.dotPosition == item.rightSide.size()) {
					Variable var = null;
					for (Variable var2 : variables)
						if (item.leftSide.equals(var2.name)) {
							var = var2;
							break;
						}
					for (String flw : var.follow) {
						int index = -1;
						for (int j = 0; j < temp_for_table.length; j++)
							if (flw.equals(temp_for_table[j])) {
								index = j;
								break;
							}
						if (index == -1)
							System.out.println(var.name + " &" + flw + "&");
						parseTable[i][index] = "r" + item.ruleNum;
						if (parseTable[i][index].equals("r0"))
							parseTable[i][index] = "acc";
					}
				}
			for (Transition trans : states.get(i).transitions) {
				int index = -1;
				for (int j = 0; j < temp_for_table.length; j++)
					if (trans.value.equals(temp_for_table[j])) {
						index = j;
						break;
					}
				parseTable[i][index] = String.valueOf(trans.to);
				if (index < 27)
					parseTable[i][index] = "s" + parseTable[i][index];
			}
		}
		return parseTable;
	}

	public static void main(String[] args) {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("grammar.txt"));
			String line = br.readLine();
			while (line != null) {
				String[] tokens = line.split(" ");
				String leftSide = tokens[0];
				ArrayList<String> rightSide = new ArrayList<>();
				for (int i = 2; i < tokens.length; i++)
					if (!tokens[i].equals("''"))
						rightSide.add(tokens[i]);
				rules.add(new Item(leftSide, rightSide, 0, rules.size()));
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<Item> temp = new ArrayList<>();
		temp.add(rules.get(0));
		State state = new State(temp);
		states.add(state);
		for (int i = 0; i < states.size(); i++)
			states.get(i).makeTransition();
		// System.out.println(states.size());

		try {
			fillVariables();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[][] parseTable = createParseTable();
		String output = "";
		for (int i = 0; i < states.size(); i++) {
			output += "#" + i;
			for (int j = 0; j < temp_for_table.length; j++) {
				if (parseTable[i][j] == null)
					output += "# ";
				else if (!parseTable[i][j].equals("r0"))
					output += ("#" + parseTable[i][j]);
				else
					output += "#acc";
			}
		}
		System.out.println(output);
		try (Writer writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream("output.txt"), "utf-8"))) {
			writer.write(output);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class Item {
	String leftSide;
	ArrayList<String> rightSide;
	int dotPosition;
	int ruleNum;

	public Item(String leftSide, ArrayList<String> rightSide, int dotposition, int ruleNum) {
		this.leftSide = leftSide;
		this.rightSide = rightSide;
		this.dotPosition = dotposition;
		this.ruleNum = ruleNum;
	}

	@Override
	public boolean equals(Object obj) {
		Item item = (Item) obj;
		if (!this.leftSide.equals(item.leftSide))
			return false;
		if (this.dotPosition != item.dotPosition)
			return false;
		if (this.rightSide.size() != item.rightSide.size())
			return false;
		for (int i = 0; i < this.rightSide.size(); i++)
			if (!this.rightSide.get(i).equals(item.rightSide.get(i)))
				return false;
		return true;
	}

}

class State {
	ArrayList<Item> items = new ArrayList<>();
	ArrayList<Transition> transitions = new ArrayList<>();

	public State(ArrayList<Item> items) {
		this.items = items;
		closure();
	}

	void closure() {
		for (int i = 0; i < items.size(); i++)
			for (int j = 0; j < ParseTableGenerator.rules.size(); j++)
				if (items.get(i).dotPosition != items.get(i).rightSide.size() && items.get(i).rightSide
						.get(items.get(i).dotPosition).equals(ParseTableGenerator.rules.get(j).leftSide)) {
					if (!items.contains(ParseTableGenerator.rules.get(j)))
						items.add(ParseTableGenerator.rules.get(j));
				}
	}

	void makeTransition() {
		Map<String, ArrayList> map = new HashMap<>();
		ArrayList<String> afterDots = new ArrayList<>();
		for (Item item : items) {
			if (item.dotPosition != item.rightSide.size() && map.get(item.rightSide.get(item.dotPosition)) == null) {
				afterDots.add(item.rightSide.get(item.dotPosition));
				ArrayList<Item> tmp = new ArrayList<>();
				tmp.add(item);
				map.put(item.rightSide.get(item.dotPosition), tmp);
			} else if (item.dotPosition != item.rightSide.size()) {
				map.get(item.rightSide.get(item.dotPosition)).add(item);
			}
		}
		for (String afterDot : afterDots) {
			ArrayList<Item> newItems = new ArrayList<>();
			ArrayList<Item> mapGet = map.get(afterDot);
			for (Item item : mapGet)
				newItems.add(new Item(item.leftSide, item.rightSide, item.dotPosition + 1, item.ruleNum));
			int stateNumber = -1;
			State newState = new State(newItems);
			if (!ParseTableGenerator.states.contains(newState)) {
				ParseTableGenerator.states.add(newState);
				stateNumber = ParseTableGenerator.states.size() - 1;
			} else {
				for (int i = 0; i < ParseTableGenerator.states.size(); i++)
					if (newState.equals(ParseTableGenerator.states.get(i))) {
						stateNumber = i;
						break;
					}
			}
			transitions.add(new Transition(afterDot, stateNumber));
		}
	}

	@Override
	public boolean equals(Object arg0) {
		State state = (State) arg0;
		if (this.items.size() != state.items.size())
			return false;
		for (int i = 0; i < this.items.size(); i++)
			if (!this.items.get(i).equals(state.items.get(i)))
				return false;
		return true;
	}
}

class Transition {
	String value;
	int to;

	public Transition(String value, int to) {
		this.value = value;
		this.to = to;
	}
}
