import java.util.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

//import java.util.LinkedList;

class MemoryWord{
    public boolean used;  //true = reg, false = memory
    public int address;

    public MemoryWord(boolean used,int address){ this.used = used; this.address = address;}

}

class Environment{
	

}

public class CmenosMipsGenerator extends CmenosBaseListener {

	int nRegs = 10; // mips = 32
	int activeRegs;
	int outputLine;
	int tMemory = 256;
	int stackMemory = 0;

	// Dicionario para chamada de funcoes [funcname, label]
	//Map<String, Integer> funcLabels = new HashMap<String, Integer>();
	
	// Tabela de simbolos [varname, indiceregistrador] 
	Map<String, MemoryWord> varRegs = new HashMap<String, MemoryWord>();

	// Para retorno de valores
	Stack<Integer> stack = new Stack<Integer>();

	// Controle dos Registradores


	int register[] = new int[nRegs];
	//boolean status[] = new boolean[nRegs];
	LinkedList<Integer> memory = new LinkedList<>();

	ParseTreeProperty<Integer> values = new ParseTreeProperty<Integer>();
	int ifcount;
	int whilecount;


	
	int obtemReg(String s){
		MemoryWord m = varRegs.get(s);

		if(m == null){ //declaração de s
			int pos = varRegs.size() % nRegs;
			for (Map.Entry<String,MemoryWord> e : varRegs.entrySet()) {
				if(e.getValue().used == true && e.getValue().address == pos){
					//mandar register[pos] pra memoria
					Integer i = new Integer(register[pos]);
					memory.add(i);
					e.getValue().used = false;
					e.getValue().address = memory.indexOf(i);
					varRegs.put(s,new MemoryWord(true,pos));
					System.out.println(pos);
					return pos;
				}
			}

			System.out.println(pos);
			
			for (int i = 0;i < nRegs ;i++ ) {
				System.out.println(register[i]);
			}
			System.out.println("------------");
			for (int j = 0;j < varRegs.size() ; j++ ) {
				System.out.println(varRegs)
			}

		}else{ //ta na memória
			if(m.used == false){
				int pos = m.address % nRegs;
				for (Map.Entry<String,MemoryWord> e : varRegs.entrySet()) {
					if(e.getValue().used == true && e.getValue().address == pos){
						//fazer swap com alguma variavel em registrador
						Integer i = new Integer(register[pos]);
						memory.add(i);
						e.getValue().used = false;
						e.getValue().address = memory.indexOf(i);
						memory.get(m.address); //remover da memória
						varRegs.put(s,new MemoryWord(true,pos)); //por no registrador
						return pos;
					}
				}
			}else{ //já está em registrador
				return m.address;
			}
		}

		if(varRegs.size() < nRegs){
			int pos = activeRegs;
			varRegs.put(s,new MemoryWord(true,pos));
			activeRegs += 1;
		}else{

		}

		return -1;
	}


	/**
	 * {@inheritDoc}
	 *
	 */
	@Override public void enterProg(CmenosParser.ProgContext ctx) {
		this.outputLine = 0;
		this.activeRegs = 0; 
		this.ifcount = 0;
		this.whilecount = 0;

		System.out.println(obtemReg("a"));
		System.out.println(obtemReg("b"));

	}

	// IF
	
	/**
	 * {@inheritDoc}
	 *	<p>Entra no IF</p>
	 */
	@Override public void enterSeldecl(CmenosParser.SeldeclContext ctx) {
		// colocando o index do if correpondente a este nó
		values.put(ctx, ifcount);
		System.out.println();
		System.out.println();
		System.out.println("If_" + ifcount + ":");
		ifcount++;

	}

	/**
	 * {@inheritDoc}
	 *	<p>Sai do IF</p>
	 */
	@Override public void exitSeldecl(CmenosParser.SeldeclContext ctx) {
		int id = values.get(ctx);
		System.out.println("EndIf_ " + id + ":");
	}


	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterIffirstpartcond(CmenosParser.IffirstpartcondContext ctx) {
		System.out.println("\tResolve condição.");
		ParserRuleContext ctxGParent = ctx.getParent().getParent();
		int id = values.get(ctxGParent);
		if (ctxGParent.getChildCount() == 2) {
			// estamos em um if com else
			// isto é apenas uma simplificação, deve ser feito depois na parte da condição
			System.out.println("\tbeq\t reg1, reg2, ElseIf_" + id  + " # se falso vai para else");
		} else {
			// estamos em um if sem else
			System.out.println("\tbeq\t reg1, reg2, EndIf_" + id + "# se falso vai para saida");
		}	
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterIfsecondpart(CmenosParser.IfsecondpartContext ctx) { 
		ParserRuleContext ctxParent = ctx.getParent();
		int id = values.get(ctxParent);
		System.out.println("ElseIf_" + id + ":");		
	}



	// WHILE

	/**
	 * {@inheritDoc}
	 *
	 * <p>Entra no while.</p>
	 */
	@Override public void enterIterdecl(CmenosParser.IterdeclContext ctx) { 
		// colocando o index do if correpondente a este nó
		values.put(ctx, whilecount);
		System.out.println();
		System.out.println();
		System.out.println("While_" + whilecount + ":");
		whilecount++;
	}
	/**
	 * {@inheritDoc}
	 *
	 * <p>Sai do while.</p>
	 */
	@Override public void exitIterdecl(CmenosParser.IterdeclContext ctx) { 
		int id = values.get(ctx);
		System.out.println("\tj While_" + id);
		System.out.println();
		System.out.println();
		System.out.println("EndWhile_ " + id + ":");
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitWhilefirstpart(CmenosParser.WhilefirstpartContext ctx) {
		System.out.println("\tResolve condição.");
		ParserRuleContext ctxParent = ctx.getParent();
		int id = values.get(ctxParent);
		System.out.println("\tbeq\t reg1, reg2, EndWhile_" + id  + " # se falso vai para o fim do while");
	}


	// FUNCAO

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterFundecl(CmenosParser.FundeclContext ctx) {
		System.out.println();
		System.out.println();
		//System.out.println("\t jal " + ctx.getChild(1).getText());
		System.out.println(ctx.getChild(1).getText() + ":");
		//varRegs.put(ctx.getChild(1).getText(),new MemoryWord())
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitFundecl(CmenosParser.FundeclContext ctx) { 
		System.out.println("\tjr\t$ra");
	}

	// VARIAVEL

	@Override public void enterVardecl(CmenosParser.VardeclContext ctx) { 

	}

	@Override public void exitVardecl(CmenosParser.VardeclContext ctx) { 

	}

	@Override public void enterAtiv(CmenosParser.AtivContext ctx) { 
		
	}
	
	@Override public void exitAtiv(CmenosParser.AtivContext ctx) { 
		System.out.println("\tjal " + ctx.getChild(0).getText());	
	}

	// OPERACAO ARITMETICA

	@Override public void enterSomaexpr(CmenosParser.SomaexprContext ctx) { }
	
	@Override public void exitSomaexpr(CmenosParser.SomaexprContext ctx) { }

}