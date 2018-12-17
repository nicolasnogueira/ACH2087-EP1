import java.util.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;


// contem informacoes sobre uma variavel na tabela de simbolos
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

	Map<String, String> varDecl = new HashMap<String, String>(); // so pra checar se uma var ta sendo usada apos declaracao

	// Para armazenamento de argumentos e retorno
	Map<String, Stack<Integer>> funcstack = new HashMap<String, Stack<Integer>>();

	// Ajuda a compartilhar informacao entre enterNode e exitNode para uma subarvore
	ParseTreeProperty<Integer> values = new ParseTreeProperty<Integer>();

	// Stack para variaveis temporarias
	private Stack<Integer> stack = new Stack<Integer>();
	int nargsRead = 0;
	int nargsWrite = 0;

	// Contador de labels condicionais
	int ifcount;
	int whilecount;

	// REGISTRADORES

	/****************************************************
	*
	*  0 			- 0 		- valor 0
	*  1 			- $at 		- reservado
	*  2-3			- $v0-$v1	- valores de retorno
	*  4-7			- $a0-$a3	- argumentos
	*  8-15 		- $t0-$t7	- temporarios 
	*  16-23		- $s0-$s7	- valores salvos
	*  24-25		- $t8-$t9	- temporarios
	*  26-27		- $k0-$k1	- reservados
	*  28			- $gp 		- global pointer
	*  29			- $sp 		- stack pointer
	*  30 			- $s8/$fp 	- salvo/frame pointer
	*  31			- $ra 		- return address (PC)
	*
	****************************************************/

	int timesRegAlloc;										// quantidade de vezes que um registrador foi associado a variavel
	int[] tregs = {8, 9, 10, 11, 12, 13, 14, 15, 24, 25}; 	// temporaries (usado para aritmetica intermediaria)
	int tregsUsados;
	int[] sregs = {16, 17, 18, 19, 20, 21, 22, 23};			// saved (usado para variaveis)
	int smax = 0;
	String[] nomes = {"zero", "at", "v0", "v1", "a0", "a1", "a2", "a3", "t0", "t1", 
					 "t2", "t3", "t4", "t5", "t6", "t7", "s0", "s1", "s2", "s3", 
					 "s4", "s5", "s6", "s7", "t8", "t9", "k0", "k1", "gp", "sp", 
					 "s8", "ra"};
	boolean[] regsUsed = new boolean[32];					// estados dos registradores


	// Nao funcional
	public void guardarRegToMem(String variavel, int regpos) {
		//System.out.println("Guardando " + variavel + " de " + regpos + " em ???");
	}

	public void guardarMemToReg(String variavel, int regpos) {
		//System.out.println("Guardando " + variavel + " de ??? em " + regpos);
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
			varRegs.put(variavel, new MemoryWord(true, true, sregs[pos])); // nao sofreu atribuicao ainda
			timesRegAlloc++;
		} else {
			if (m.rused) {
				pos = m.address;
				return pos;
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
		return sregs[pos];
	}

	 /*
	 *
	 *	As funcoes a seguir de nome enter.* e exit.* sao dedicadas a geracao de
	 *  codigo a partir da exploracao da arvore, todas sobrescrevem metodos de 
	 *  CmenosBaseListener.
	 *	
	 *
	 */	

	/**
	 * {@inheritDoc}
	 *	<p>Entra no programa</p>
	 */
	@Override public void enterProg(CmenosParser.ProgContext ctx) {
		this.outputLine = 0;
		this.ifcount = 0;
		this.whilecount = 0;
		this.tregsUsados = 0;
		this.timesRegAlloc = 0;
		System.out.println(".text");
		System.out.println("\tj main");
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

	// Funcoes acerca da estrutura IF
	
	/**
	 * {@inheritDoc}
	 *	<p>Entra no IF</p>
	 */
	@Override public void enterSeldecl(CmenosParser.SeldeclContext ctx) {
		// colocando o index do if correpondente a este nó
		values.put(ctx, ifcount);
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
		System.out.println("EndIf_" + id + ":");
	}

	/**
	 * {@inheritDoc}
	 *	<p>Sai da condicao do IF</p>
	 */
	@Override public void exitIffirstpartcond(CmenosParser.IffirstpartcondContext ctx) {
		
		ParserRuleContext ctxGParent = ctx.getParent().getParent();
		int id = values.get(ctxGParent);
		int regresult = stack.pop();
		if (ctxGParent.getChildCount() == 2) {
			// estamos em um if com else
			System.out.println("\tbeq $" + nomes[regresult] + ", 1, ElseIf_" + id  + " # se falso vai para else");
		} else {
			// estamos em um if sem else
			System.out.println("\tbeq $" + nomes[regresult] + ", 1, EndIf_" + id + "# se falso vai para saida");
		}	
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>Entra no else de um if, para estruturas if-else</p>
	 */
	@Override public void enterIfsecondpart(CmenosParser.IfsecondpartContext ctx) { 
		ParserRuleContext ctxParent = ctx.getParent();
		int id = values.get(ctxParent);
		System.out.println("ElseIf_" + id + ":");		
	}



	// Funcoes acerca da estrutura WHILE

	/**
	 * {@inheritDoc}
	 *
	 * <p>Entra no while.</p>
	 */
	@Override public void enterIterdecl(CmenosParser.IterdeclContext ctx) { 
		// colocando o index do if correpondente a este nó
		values.put(ctx, whilecount);
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
		System.out.println("EndWhile_ " + id + ":");
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Sai da condicao do while.</p>
	 */
	@Override public void exitWhilefirstpart(CmenosParser.WhilefirstpartContext ctx) {
		System.out.println("\t# Resolve condição.");
		ParserRuleContext ctxParent = ctx.getParent();
		int id = values.get(ctxParent);
		int regresult = stack.pop();
		System.out.println("\tbeq $" + nomes[regresult] + ", 1, ElseIf_" + id  + " # se falso vai para o fim do while");


	}


	// Funcoes acerca do tratamento de FUNCAO

	/**
	 * {@inheritDoc}
	 *
	 * <p>Visita argumento.</p>
	 */
	@Override public void enterParam(CmenosParser.ParamContext ctx) { 
		int varreg = getReg(ctx.getChild(1).getText());
		System.out.println("\taddi $" + nomes[varreg] + ", $a" + nargsRead + ", 0");
		nargsRead++;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Sai de argumentos.</p>
	 */
	@Override public void exitParams(CmenosParser.ParamsContext ctx) { 
		nargsRead = 0;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Entra na funcao.</p>
	 */
	@Override public void enterFundecl(CmenosParser.FundeclContext ctx) {
		System.out.println();
		System.out.println(ctx.getChild(1).getText() + ":");

		if (!ctx.getChild(1).getText().equals("main")) {
			System.out.println("\taddi $sp, $sp, -" + (sregs.length + 1)*4);
			System.out.println("\tsw $ra, 0($sp)");
			int count = 4;
			for (int i = 0; i < sregs.length; i++) {
				System.out.println("\tsw $" + nomes[sregs[i]] + ", " + count + "($sp)");
				count += 4;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Sai da funcao.</p>
	 */
	@Override public void exitFundecl(CmenosParser.FundeclContext ctx) { 
		if (!ctx.getChild(1).getText().equals("main")){
			System.out.println("\tlw $ra, 0($sp)");
			int count = 4;
			for (int i = 0; i < sregs.length; i++) {
				System.out.println("\tlw $" + nomes[sregs[i]] + ", " + count + "($sp)");
				count += 4;
			}
			System.out.println("\taddi $sp, $sp, " + (sregs.length + 1)*4);


			System.out.println("\tjr\t$ra");	
		} else {
			System.out.println("\tli $v0, 10");
			System.out.println("\tsyscall");
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Sai do retorno de funcao.</p>
	 */
	@Override public void exitRetdecl(CmenosParser.RetdeclContext ctx) { 
		if (ctx.getChildCount() == 3) {
			int regresult = stack.pop();
			System.out.println("\tmove $v0, $" + nomes[regresult]);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Sai da lista de argumentos de um call (ativ).</p>
	 */
	@Override public void exitArglist(CmenosParser.ArglistContext ctx) { 
		int regresult = stack.pop();
		System.out.println("\tmove $a" + nargsWrite + ", $" + nomes[regresult]);
		nargsWrite++;
	}


	// Funcoes acerca de Declaracao.

	/**
	 * {@inheritDoc}
	 *
	 * <p>Entra declaracao de var.</p>
	 */
	@Override public void enterVardecl(CmenosParser.VardeclContext ctx) {
		//String nome = ctx.getChild(0).getText(); 
		//varDecl.put(nome, nome);
	}


	// Funcoes acerca de Atribuicao.

	/**
	 * {@inheritDoc}
	 *
	 * <p>Sai de uma atribuicao.</p>
	 */
	@Override public void exitAssign(CmenosParser.AssignContext ctx) {
		if (varDecl.get(ctx.getChild(0).getText()) == null) {
			//System.out.println("ERRO! Variavel nao declarada!");
		}
		int regsalve = getReg(ctx.getChild(0).getText());
		int result = stack.pop();
		System.out.println("\tmove $" + nomes[regsalve] + ", $" + nomes[result]);
	}


	// Funcoes acerca de ARITMETICA

	/**
	 * {@inheritDoc}
	 *
	 * <p>Sai de uma expressao de comparacao.</p>
	 */
	@Override public void exitSimpexpr(CmenosParser.SimpexprContext ctx) {
		if (ctx.getChildCount() == 3) {
			int right = stack.pop();
			int left = stack.pop();
			int regresult = left;
			if (ctx.getChild(1).getText().equals("<=")) {
				System.out.println("\tsle $" + nomes[regresult] + ", $" + nomes[left] + ", $" + nomes[right]);
			} else if (ctx.getChild(1).getText().equals("<")){
				System.out.println("\tslt $" + nomes[regresult] + ", $" + nomes[left] + ", $" + nomes[right]);
			} else if (ctx.getChild(1).getText().equals(">")){
				System.out.println("\tsgt $" + nomes[regresult] + ", $" + nomes[left] + ", $" + nomes[right]);
			} else if (ctx.getChild(1).getText().equals(">=")){
				System.out.println("\tsge $" + nomes[regresult] + ", $" + nomes[left] + ", $" + nomes[right]);
			} else if (ctx.getChild(1).getText().equals("==")){
				System.out.println("\tseq $" + nomes[regresult] + ", $" + nomes[left] + ", $" + nomes[right]);
			} else if (ctx.getChild(1).getText().equals("!=")){
				System.out.println("\tsne $" + nomes[regresult] + ", $" + nomes[left] + ", $" + nomes[right]);
			}
			stack.push(regresult);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Sai de um fator (possivel node da arvore).</p>
	 */
	@Override public void exitFator(CmenosParser.FatorContext ctx) { 
		if (ctx.getChildCount() == 1) {
			// para int por enquanto
			if (ctx.NUM() != null) {

				System.out.println("\taddi $" + nomes[tregs[stack.size() % tregs.length]] + ", $zero, " + ctx.getChild(0).getText());
				stack.push(tregs[stack.size() % tregs.length]);

			} else if (ctx.var() != null) {
				MemoryWord m = varRegs.get(ctx.var().getText()); // deve existir
				// acessando a variavel no registrador dele
				int regvar = getReg(ctx.var().getText());
				System.out.println("\tmove $" + nomes[tregs[stack.size() % tregs.length]] + ", $" + nomes[regvar]);
				stack.push(tregs[stack.size() % tregs.length]);

			} else if (ctx.ativ() != null) {
				// salvar as variaveis de argumentos
				System.out.println("\tjal " + ctx.getChild(0).getChild(0).getText());
				System.out.println("\tadd $" + nomes[tregs[stack.size() % tregs.length]] + ", $zero, $v0");
				stack.push(tregs[stack.size() % tregs.length]);
				nargsWrite = 0;

			}
			
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Sai de um termo (possivel operacao multiplicativa/divisao).</p>
	 */
	@Override public void exitTermo(CmenosParser.TermoContext ctx) { 
		if (ctx.getChildCount() == 3) {
			int right = stack.pop();
			int left = stack.pop();
			int regresult = left;
			if (ctx.getChild(1).getText().equals("*")) {
				// mult
				System.out.println("\tmult $" + nomes[left] + ", $" + nomes[right]);
				System.out.println("\tmflo $" + nomes[regresult]);
			} else {
				// div
				System.out.println("\tdiv $" + nomes[left] + ", $" + nomes[right]);

				// é possível comparar com 0 para dar erro 
				System.out.println("\tmflo $" + nomes[regresult]);
			}
			stack.push(regresult);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Sai de uma expressao de soma (possivel operacao adicao/subtracao).</p>
	 */
	@Override public void exitSomaexpr(CmenosParser.SomaexprContext ctx) { 
		if (ctx.getChildCount() == 3) {
			int right = stack.pop();
			int left = stack.pop();
			int regresult = left;
			if (ctx.getChild(1).getText().equals("+")) {
				// add
				System.out.println("\tadd $" + nomes[regresult] + ", $" + nomes[left] + ", $" + nomes[right]);
			} else {
				// sub
				System.out.println("\tsub $" + nomes[regresult] + ", $" + nomes[left] + ", $" + nomes[right]);
			}
			stack.push(regresult);
		}
	}

}