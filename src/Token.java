import java.util.ArrayList;

public class Token {
	String type;
	String value;
	Integer address;
	ArrayList<String> parameters;
	int scope;
	boolean intFunc;
	String type2;
	
	public Token(String type, String value, Integer address) {
		this.type = type;
		this.value = value;
		this.address = address;
		parameters = new ArrayList<>();
		scope = -1;
		intFunc = false;
	}
}
