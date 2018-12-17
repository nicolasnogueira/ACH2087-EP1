import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.io.*;

public class CmenosMips {
    public static void main(String[] args) throws IOException {
        InputStream is = new FileInputStream(System.getProperty("user.dir") + "/input"); // or System.in;
        ANTLRInputStream input = new ANTLRInputStream(is);
        CmenosLexer lexer = new CmenosLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CmenosParser parser = new CmenosParser(tokens);
        ParseTree tree = parser.prog(); // prog is the starting rule

        ParseTreeWalker walker = new ParseTreeWalker();
        CmenosMipsGenerator evalByListener = new CmenosMipsGenerator();
        walker.walk(evalByListener, tree);
    }
}

