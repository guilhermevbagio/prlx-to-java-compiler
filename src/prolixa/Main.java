package prolixa;
import prolixa.lexer.*;
import prolixa.node.*;
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

				Lexer lexer =
						new Lexer(
								new PushbackReader(  
										new FileReader(file), 1024)); 
				Token token;
				
				System.out.println("\n -------------" + file.getName().toUpperCase() + "------------- \n");
				
				while(!((token = lexer.next()) instanceof EOF)) {
					System.out.println(token.getClass());
					System.out.println(" ( "+token.toString()+")");
				}
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
}