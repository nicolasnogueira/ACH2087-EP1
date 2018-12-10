import java.util.*;

public static class CmenosMipsGenerator extends CmenosBaseListener {
	int nRegs = 10;
	int activeRegs;
	int outputLine;

	// Dicionario para chamada de funcoes
	Map<String, Integer> funcLabels = new HashMap<String, Integer>();
	Map<String, Integer> varRegs = new OrderedHashMap<String, Integer>();

	// Para retorno de valores
	Stack<Integer> stack = new Stack<Integer>();
	
	public void enterProg(CmenosParser.ProgContext ctx) { 
		this.outputLine = 0;
		this.activeRegs = 0;
	}

}