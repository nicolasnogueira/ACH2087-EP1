import java.util.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

class MemoryWord {
    public boolean rused;  		//true = reg, false = memory
    public boolean evaluated;	//passou por atribuicao
    public int address;


    public MemoryWord(boolean rused, boolean evaluated, int address) { 
    	this.rused = rused; 
    	this.evaluated = evaluated;
    	this.address = address;
    }

}

public class CmenosMipsGenerator extends CmenosBaseListener {
	int outputLine;
	
	// Tabela de simbolos [varname, used, address] 
	Map<String, MemoryWord> varRegs = new HashMap<String, MemoryWord>();

	// Para retorno de valores
	Stack<Integer> stack = new Stack<Integer>();

	ParseTreeProperty<Integer> values = new ParseTreeProperty<Integer>();
	int ifcount;
	int whilecount;

	// REGISTRADORES

	/*********************************************
	*
	*	0 		- zero 		- valor 0
	*	1 		- $at 		- reservado
	*	2-3		- $v0-$v1	- valores de retorno
	*	4-7		- $a0-$a3	- argumentos
	*	8-15 	- $t0-$t7	- temporarios 
	*	16-23	- $s0-$s7	- valores salvos
	*	24-25	- $t8-$t9	- temporarios
	*	26-27	- $k0-$k1	- reservados
	*	28		- $gp 		- global pointer
	*	29		- $sp 		- stack pointer
	*	30 		- $s8/$fp 	- salvo/frame pointer
	*	31		- $ra 		- return address (PC)
	*
	*********************************************/

	int timesRegAlloc;										// quantidade de vezes que um registrador foi associado a variavel
	int[] tregs = {8, 9, 10, 11, 12, 13, 14, 15, 24, 25}; 	// temporaries (usado para aritmetica intermediaria)
	int tregsUsados;
	int[] sregs = {16, 17, 18, 19, 20, 21, 22, 23};			// saved (usado para variaveis)
	boolean[] regsUsed = new boolean[32];					// estados dos registradores


	public void guardarRegToMem(String variavel, int regpos) {
		System.out.println("Guardando " + variavel + " de " + regpos + " em ???");
	}

	public void guardarMemToReg(String variavel, int regpos) {
		System.out.println("Guardando " + variavel + " de ??? em " + regpos);
	}


	/**
	 * {@inheritDoc}
	 *
	 *	Funcao para obter registrador para variavel ja declarada
	 *
	 *	@param variavel		nome da variável a ser utilizada no registrador 
	 *	@return 			indice do registrador em sregs
	 *
	 */	
	public int getReg(String variavel) {
		MemoryWord m = varRegs.get(variavel);
		int pos;
		if (m == null) { // declaracao
			pos = timesRegAlloc % sregs.length;
			if (regsUsed[sregs[pos]] == true) {
				// colocar a variavel correspondente na memoria **EM MIPS**
				for (Map.Entry<String, MemoryWord> e : varRegs.entrySet()) {
					if (e.getValue().rused == true && e.getValue().address == sregs[pos]) {
						guardarRegToMem(e.getKey(), sregs[pos]/*, memoria */);
					}
				}
			}

			// associar variavel ao registrador livre
			regsUsed[sregs[pos]] = true;
			varRegs.put(variavel, new MemoryWord(true, false, sregs[pos])); // nao sofreu atribuicao ainda
			timesRegAlloc++;
		} else {
			if (m.rused) {
				pos = m.address;
			} else {
				pos = timesRegAlloc % sregs.length;
				if (regsUsed[sregs[pos]] == true) {
					// colocar a variavel correspondente na memoria **EM MIPS**
					for (Map.Entry<String, MemoryWord> e : varRegs.entrySet()) {
						if (e.getValue().rused == true && e.getValue().address == sregs[pos]) {
							guardarRegToMem(e.getKey(), sregs[pos]/*, memoria */);
						}
					}					
				}
				// colocar variavel da memória no registrador **EM MIPS**
				guardarMemToReg(variavel, sregs[pos]/*, memoria */);

				// seria interessante apagar da memoria? mantemos um registro salvo para a variavel?

				// associar variavel a este registrador
				regsUsed[sregs[pos]] = true;
				varRegs.put(variavel, new MemoryWord(true, true, sregs[pos]));
				timesRegAlloc++;
			}
		}
		return pos;
	}

	/**
	 * {@inheritDoc}
	 *
	 */
	@Override public void enterProg(CmenosParser.ProgContext ctx) {
		this.outputLine = 0;
		this.ifcount = 0;
		this.whilecount = 0;
		this.tregsUsados = 0;
		this.timesRegAlloc = 0;

		System.out.println("input:");
		System.out.println("\tli $v0, 5\t# leitura de inteiro");
		System.out.println("\tsyscall\t\t# valor lido vai ser registrado em $v0");
		System.out.println("\tjr $ra");
		System.out.println();
		System.out.println("output:");
		System.out.println("\tli $v0, 1");
		System.out.println("\tsyscall\t\t# o valor deve estar em $a0 antes de acessar a func");
		System.out.println("\tjr $ra");
		System.out.println();
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
		if (!ctx.getChild(1).getText().equals("main")){
			System.out.println("\tjr\t$ra");	
		}
	}




}