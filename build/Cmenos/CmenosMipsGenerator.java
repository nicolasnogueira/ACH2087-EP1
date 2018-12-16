import java.util.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

public class CmenosMipsGenerator extends CmenosBaseListener {
	int nRegs = 10; // mips = 32
	int activeRegs;
	int outputLine;
	int tMemory = 256;

	// Dicionario para chamada de funcoes [funcname, label]
	Map<String, Integer> funcLabels = new HashMap<String, Integer>();
	
	// Tabela de simbolos [varname, indiceregistrador] 
	Map<String, Integer> varRegs = new HashMap<String, Integer>();

	// Para retorno de valores
	Stack<Integer> stack = new Stack<Integer>();

	int register[] = new int[nRegs];
	boolean status[] = new boolean[nRegs];
	int memory[] new int[tMemory];

	ParseTreeProperty<Integer> values = new ParseTreeProperty<Integer>();
	int ifcount;
	int whilecount;

	/**
	 * {@inheritDoc}
	 *
	 */
	@Override public void enterProg(CmenosParser.ProgContext ctx) {
		this.outputLine = 0;
		this.activeRegs = 0; 
		this.ifcount = 0;
		this.whilecount = 0;
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
		System.out.println(ctx.getChild(1).getText() + ":");
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitFundecl(CmenosParser.FundeclContext ctx) { 
		System.out.println("\tjr\t$ra");
	}


}