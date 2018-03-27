import java.lang.reflect.Method;
import java.text.BreakIterator;
import java.util.ArrayList;

public class IntermediateCodeGenerator {
	static ArrayList<String> stack = new ArrayList<>();
	static int lastTempAddress = 501;
	static int returnAddress = 500;
	static int returnValueAddress = 501;
	static ArrayList<Integer> returnAdresses = new ArrayList<>();
	static ArrayList<Code> PB = new ArrayList<>();
	static Token lastFunctionToken;
	static Token lastFuncCalledToken;
	static int lastFuncCalledParameters = 0;
	static boolean addReturn = false;
	static int scope = 0;

	static int getTemp() {
		lastTempAddress++;
		return lastTempAddress;
	}

	static int addReturnAddress() {
		int t = getTemp();
		returnAdresses.add(t);
		return t;
	}

	static int removeReturnAddress() {
		int t = returnAdresses.get(returnAdresses.size() - 1);
		returnAdresses.remove(returnAdresses.size() - 1);
		return t;
	}

	static void generateCode(String func) {
		try {
			System.out.println("F: " + func);
			Class c = Class.forName("IntermediateCodeGenerator");
			Method m = c.getDeclaredMethod(func, null);
			m.invoke(null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void saveJmpToMain() {
		// stack.add(String.valueOf(PB.size() - 1));
		PB.add(null);
	}

	static void setFunctionAddr() {
		Scanner_class.tokens.get(Scanner_class.tokens.size() - 2).address = PB.size();
		lastFunctionToken = Scanner_class.tokens.get(Scanner_class.tokens.size() - 2);
		lastFunctionToken.scope = scope;
		if (Scanner_class.tokens.get(Scanner_class.tokens.size() - 3).type.equals("int"))
			Scanner_class.tokens.get(Scanner_class.tokens.size() - 2).intFunc = true;

		// System.out.println(lastFunctionToken.type + " " +
		// lastFunctionToken.value + " " + lastFunctionToken.address);
	}

	static void jmpToMain() {
		if (Scanner_class.tokens.get(Scanner_class.tokens.size() - 2).value.equals("main")) {
			PB.set(0, new Code("JP", String.valueOf(PB.size()), null, null));
			// stack.remove(stack.size() - 1);
		}
	}

	static void pid() {
		stack.add(String.valueOf(Scanner_class.tokens.get(Scanner_class.tokens.size() - 2).address));
		Scanner_class.tokens.get(Scanner_class.tokens.size() - 2).scope = scope + 1;
		
		//semantic
		for (int i = 0 ; i < Scanner_class.tokens.size() ; i ++){
			if(Scanner_class.tokens.get(i).scope == scope && i != Scanner_class.tokens.size() - 2){
//				System.out.println("^" + Scanner_class.tokens.get(i).value + " " + Scanner_class.tokens.get(Scanner_class.tokens.size() - 2).value);
				if (Scanner_class.tokens.get(i).value.equals(Scanner_class.tokens.get(Scanner_class.tokens.size() - 2).value)){
					System.err.println("Multiple declaration of variable in a scope.");
					System.exit(1);
				}
			}
		}
		
		
		// System.out.println(Scanner_class.tokens.get(Scanner_class.tokens.size()
		// - 2).type + " " +
		// Scanner_class.tokens.get(Scanner_class.tokens.size() - 2).value + " "
		// +
		// Scanner_class.tokens.get(Scanner_class.tokens.size() - 2).address);
	}

	static void setScope() {
		Scanner_class.tokens.get(Scanner_class.tokens.size() - 2).scope = scope;
		
		//semantic
				for (int i = 0 ; i < Scanner_class.tokens.size() ; i ++){
					if(Scanner_class.tokens.get(i).scope == scope && i != Scanner_class.tokens.size() - 2){
//						System.out.println("^" + Scanner_class.tokens.get(i).value + " " + Scanner_class.tokens.get(Scanner_class.tokens.size() - 2).value);
						if (Scanner_class.tokens.get(i).value.equals(Scanner_class.tokens.get(Scanner_class.tokens.size() - 2).value)){
							System.err.println("Multiple declaration of variable in a scope.");
							System.exit(1);	
						}
					}
				}
	}

	static void assign() {
//		System.out.println("$$$$");
//		for(int i = 0; i < stack.size(); i++)
//			System.out.print(stack.get(i) + " ");
		try {
			PB.add(new Code("ASSIGN", String.valueOf(stack.get(stack.size() - 1)),
					String.valueOf(stack.get(stack.size() - 2)), null));
			
			//semantic
			semantic_for_type("Assign");
					
			stack.remove(stack.size() - 1);
			stack.remove(stack.size() - 1);
		} catch (Exception e) {
			// TODO: handle exception
			System.err.println("Type Mismatch");
			System.exit(1);
		}
		
		
	}

	static void addParam() {
		lastFunctionToken.parameters.add(stack.get(stack.size() - 1));
		stack.remove(stack.size() - 1);
		// System.out.println(lastFunctionToken.value + " " +
		// lastFunctionToken.parameters.get(0));
	}

	static void lastFuncCalled() {
		lastFuncCalledParameters = 0;
		for (Token token : Scanner_class.tokens)
			if (token.address != null
					&& token.value.equals(Scanner_class.tokens.get(Scanner_class.tokens.size() - 2).value)
					&& token.scope != -1)
				lastFuncCalledToken = token;
	}

	static void assignParams() {
//		System.out.println("^^" + lastFuncCalledToken.parameters.size() + " " + lastFuncCalledParameters);
		if(lastFuncCalledToken.parameters.size() != lastFuncCalledParameters){
			System.err.println("Wrong in functions inputs");
//			System.exit(1);
		}
//		System.out.println(lastFuncCalledToken);
//		System.out.println(lastFuncCalledToken.parameters);
		for (int i = lastFuncCalledToken.parameters.size() - 1; i >= 0; i--) {
//			System.out.println("$$$$");
			PB.add(new Code("ASSIGN", String.valueOf(stack.get(stack.size() - 1)),
					String.valueOf(lastFuncCalledToken.parameters.get(i)), null));
//			System.out.println(PB.get(PB.size() - 1).command + " " + PB.get(PB.size() - 1).first + " " + PB.get(PB.size() - 1).second);
			stack.remove(stack.size() - 1);
		}
		lastFuncCalledParameters = 0;
	}

	static void add() {
		int tmp = getTemp();
		PB.add(new Code("ADD", String.valueOf(stack.get(stack.size() - 1)), String.valueOf(stack.get(stack.size() - 2)),
				String.valueOf(tmp)));
		
		//semantic
		semantic_for_type("Add");
				
		stack.remove(stack.size() - 1);
		stack.remove(stack.size() - 1);
		stack.add(String.valueOf(tmp));
	}

	static void sub() {
		int tmp = getTemp();
		PB.add(new Code("SUB", String.valueOf(stack.get(stack.size() - 1)), String.valueOf(stack.get(stack.size() - 2)),
				String.valueOf(tmp)));
		
		//semantic
		semantic_for_type("Sub");
				
		stack.remove(stack.size() - 1);
		stack.remove(stack.size() - 1);
		stack.add(String.valueOf(tmp));
	}

	static void mult() {
		int tmp = getTemp();
		PB.add(new Code("MULT", String.valueOf(stack.get(stack.size() - 1)),
				String.valueOf(stack.get(stack.size() - 2)), String.valueOf(tmp)));
		
		//semantic
		semantic_for_type("Mult");
		
		stack.remove(stack.size() - 1);
		stack.remove(stack.size() - 1);
		stack.add(String.valueOf(tmp));
	}

	static void div() {
		int tmp = getTemp();
		PB.add(new Code("DIV", String.valueOf(stack.get(stack.size() - 1)), String.valueOf(stack.get(stack.size() - 2)),
				String.valueOf(tmp)));
		
		//semantic
		semantic_for_type("Div");
		
		stack.remove(stack.size() - 1);
		stack.remove(stack.size() - 1);
		stack.add(String.valueOf(tmp));
	}

	static void equal() {
		int tmp = getTemp();
		PB.add(new Code("EQ", String.valueOf(stack.get(stack.size() - 1)), String.valueOf(stack.get(stack.size() - 2)),
				String.valueOf(tmp)));
				
		//semantic
		semantic_for_type("Equal");
		
		stack.remove(stack.size() - 1);
		stack.remove(stack.size() - 1);
		stack.add(String.valueOf(tmp));
	}
	
	static void logicalAnd(){
		int tmp = getTemp();
		PB.add(new Code("AND", String.valueOf(stack.get(stack.size() - 1)), String.valueOf(stack.get(stack.size() - 2)),
				String.valueOf(tmp)));
				
		//semantic
		semantic_for_type("LogicalAnd");
		
		stack.remove(stack.size() - 1);
		stack.remove(stack.size() - 1);
		stack.add(String.valueOf(tmp));
	}

	static void less() {
		int tmp = getTemp();
		PB.add(new Code("LT", String.valueOf(stack.get(stack.size() - 2)),String.valueOf(stack.get(stack.size() - 1)),
				String.valueOf(tmp)));
		
		//semantic
		semantic_for_type("Less");
				
		stack.remove(stack.size() - 1);
		stack.remove(stack.size() - 1);
		stack.add(String.valueOf(tmp));
	}

	static void getArrayPointer() {
		int tmp = getTemp();
		PB.add(new Code("ADD", "@" + String.valueOf(stack.get(stack.size() - 2)),
				String.valueOf(stack.get(stack.size() - 1)), String.valueOf(tmp)));
		stack.remove(stack.size() - 1);
		stack.remove(stack.size() - 1);
		stack.add(String.valueOf(tmp));
	}

	static void call() {
		int tmp = addReturnAddress();
		PB.add(new Code("ASSIGN", String.valueOf(returnAddress), String.valueOf(tmp), null));
		PB.add(new Code("ASSIGN", String.valueOf(PB.size() + 2), String.valueOf(returnAddress), null));
		PB.add(new Code("JP", String.valueOf(lastFuncCalledToken.address), null, null));
		PB.add(new Code("ASSIGN", String.valueOf(tmp), String.valueOf(returnAddress), null));
		if (lastFuncCalledToken.intFunc)
			stack.add(String.valueOf(returnValueAddress));
	}

	static void pval() {
		lastFuncCalledParameters++;
		stack.add("#" + Scanner_class.tokens.get(Scanner_class.tokens.size() - 2).value);
		// System.out.println(stack.get(stack.size() -1));
	}

	static void returnFunc() {
		if (stack.size() != 0) {
//			System.out.println("^^" + stack.get(stack.size() - 1));
			PB.add(new Code("ASSIGN", stack.get(stack.size() - 1), String.valueOf(returnValueAddress), null));
			stack.remove(stack.size() - 1);
		}
		semantic_for_return_type(lastFunctionToken.intFunc);
		PB.add(new Code("JP", "@" + String.valueOf(returnAddress), null, null));
		addReturn = true;
		
		// removeReturnAddress();
	}

	static void save() {
		stack.add(String.valueOf(PB.size()));
//		System.out.println("# " + stack.get(stack.size() - 1));
		PB.add(null);
	}

	static void ifJpfSave() {
		PB.set(Integer.valueOf(stack.get(stack.size() - 1)),
				new Code("JPF", String.valueOf(stack.get(stack.size() - 2)), String.valueOf(PB.size() + 1), null));
		stack.remove(stack.size() - 1);
		stack.remove(stack.size() - 1);
		stack.add(String.valueOf(PB.size()));
		System.out.println("$ifJpfSave: " + stack.get(stack.size() - 1));
		PB.add(null);
	}

	static void ifJp() {
		for(int i = 0; i < stack.size(); i++)
		System.out.print("$$$$" + stack.get(i) +" ");
		PB.set(Integer.valueOf(stack.get(stack.size() - 1)), new Code("JP", String.valueOf(PB.size()), null, null));
		stack.remove(stack.size() - 1);
	}

	static void ifJpf() {
		System.out.println("stack size: " + stack.size());
		PB.set(Integer.valueOf(stack.get(stack.size() - 1)),
				new Code("JPF", stack.get(stack.size() - 2), String.valueOf(PB.size()), null));
		stack.remove(stack.size() - 1);
		stack.remove(stack.size() - 1);
	}

	static void label() {
		stack.add(String.valueOf(PB.size()));
//		System.out.println("## " + stack.get(stack.size() - 1));
	}

	static void whileFunc() {
//		for(int i = 0; i < stack.size(); i++)
//			System.out.print("^^^^" + stack.get(i) +" ");
		PB.set(Integer.valueOf(stack.get(stack.size() - 1)),
				new Code("JPF", String.valueOf(stack.get(stack.size() - 2)), String.valueOf(PB.size() + 1), null));
		PB.add(new Code("JP", String.valueOf(stack.get(stack.size() - 3)), null, null));
		stack.remove(stack.size() - 1);
		stack.remove(stack.size() - 1);
		stack.remove(stack.size() - 1);
	}

	static void output() {
		PB.add(new Code("PRINT", stack.get(stack.size() - 1), null, null));
		stack.remove(stack.size() - 1);
	}

	static void addScope() {
		scope++;
		addReturn = false;
	}

	static void subScope() {
		if (!addReturn && scope == lastFunctionToken.scope + 1)
			PB.add(new Code("JP", "@" + String.valueOf(returnAddress), null, null));
		for (Token token : Scanner_class.tokens)
			if (token.scope == scope)
				token.scope = -1;
		scope--;
	}

	static void findAddr() {
		lastFuncCalledParameters++;
		String current = Scanner_class.tokens.get(Scanner_class.tokens.size() - 2).value;
		int address = -1;
		int semantic_error = 0 ;
		System.out.println("Scope: " + scope);
		label1: for (int s = scope; s >= 0; s--) {
			for (Token token : Scanner_class.tokens)
				if (token.scope == s && token.value.equals(current)) {
					address = token.address;
					semantic_error = 1;
					break label1;
				}
		}
		if (semantic_error == 0){
			System.err.println("Undeclared variable Or Accessing an out of scope variable");
		}
		stack.add(String.valueOf(address));
	}

	static void printPB() {
		System.out.println("3 Address Code is: ");
		int i = 0;
		for (Code code : PB){
			System.out.print(i + " ");
			i++;
			if (code == null)
				System.out.println("null");
			else
				System.out.println(code.command + " " + code.first + " " + code.second + " " + code.third);
		}
	}
	
	static void semantic_for_type(String string){
		//semantic
		Token t1 = null , t2 = null;
		
		if (String.valueOf(stack.get(stack.size() - 1)).charAt(0) == '#' 
				|| Character.isDigit(String.valueOf(stack.get(stack.size() - 1)).charAt(0))){
			return;
		}
		
		for (int i = 0; i < Scanner_class.tokens.size(); i++) {
			if (String.valueOf(Scanner_class.tokens.get(i).address).equals( String.valueOf(stack.get(stack.size() - 1)))){
				t1 = Scanner_class.tokens.get(i);
			}
			if (String.valueOf(Scanner_class.tokens.get(i).address).equals(String.valueOf(stack.get(stack.size() - 2)))){
				t2 = Scanner_class.tokens.get(i);
			}
		}
		//System.err.println(t1 + "    " + t2 + "   " + String.valueOf(stack.get(stack.size() - 1)));
		if (t1.type2 != t2.type2){
			System.err.println("Type Mismatch At " + string);
		}
	}
	
	static void semantic_for_return_type(boolean intFunc){
		boolean hasReturned = PB.get(PB.size() - 1) != null && PB.get(PB.size() - 1).second != null && PB.get(PB.size() - 1).second.equals("501");
		if(!((intFunc &&  hasReturned )|| (!intFunc && !hasReturned))){
			System.err.println("Type Mismatch At return for function");
//			System.exit(1);
		}
		
		
			
		//semantic
//		Token t1 = null , t2 = null;
//		System.out.println("%%%1" + Scanner_class.tokens.get(Scanner_class.tokens.size() - 1).type);
//		if ((String.valueOf(stack.get(stack.size() - 1)).charAt(0) == '#' 
//				|| Character.isDigit(String.valueOf(stack.get(stack.size() - 1)).charAt(0))) && intFunc){
//			return;
//		}System.out.println("%%%2");
//		
//		for (int i = 0; i < Scanner_class.tokens.size(); i++) {
//			if (String.valueOf(Scanner_class.tokens.get(i).address) == String.valueOf(stack.get(stack.size() - 1))){
//				t1 = Scanner_class.tokens.get(i);
//			}
//			if (String.valueOf(Scanner_class.tokens.get(i).address) == String.valueOf(returnValueAddress)){
//				t2 = Scanner_class.tokens.get(i);
//			}
//		}
//		System.err.println(t1.type2 + "  ###    " +intFunc);
//		if ((!t1.type2.equals("ID") && intFunc) || (t1.type2.equals("ID") && !intFunc) ){
//			System.err.println("Type Mismatch At return for function");
//		}
	}
}

class Code {
	String command;
	String first;
	String second;
	String third;

	public Code(String command, String first, String second, String third) {
		this.command = command;
		this.first = first;
		this.second = second;
		this.third = third;
	}
}