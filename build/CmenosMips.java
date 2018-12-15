import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.io.*;

public class CmenosMips {
    public static void main(String[] args) throws IOException {
        InputStream is = new FileInputStream("../../input"); // or System.in;
        ANTLRInputStream input = new ANTLRInputStream(is);
        CmenosLexer lexer = new CmenosLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CmenosParser parser = new CmenosParser(tokens);
        ParseTree tree = parser.prog(); // prog is the starting rule

        System.out.println("Tree:");
        System.out.println(tree.toStringTree(parser));
        System.out.println();

        /*System.out.println("Visitor:");
        EvalVisitor evalByVisitor = new EvalVisitor();
        evalByVisitor.visit(tree);
        System.out.println();*/

        System.out.println("Listener:");
        ParseTreeWalker walker = new ParseTreeWalker();
        CmenosMipsGenerator evalByListener = new CmenosMipsGenerator();
        walker.walk(evalByListener, tree);
    }
}

