import prolixa.lexer.*;
import prolixa.node.*;
import prolixa.parser.*;
import prolixa.analysis.*;
import java.io.*;

public class Main
{
    public static void main(String[] args)
    {
        try
        {
            File folder = new File("test");
            File[] files = folder.listFiles();

            for(File file : files) {

                
                String filePath = file.getPath();
                System.out.println("------------- Parsing " + file.getName().toUpperCase() + " -------------");

                try {
                    Lexer lexer = new Lexer(
                        new PushbackReader(
                            new FileReader(filePath), 1024));

                    Parser parser = new Parser(lexer);

                    Start ast = parser.parse();
                    
                    ast.apply(new SemanticAnalyzer());

                } catch(Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
                
                System.out.println("\n----------------------------------------------\n");
            }
        }
        catch(Exception e)
        {
            System.out.println("error: " + e.getMessage());
        }
    }
}