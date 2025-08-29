import lexer.*;
import node.*;
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
				
				String arquivo = file.getName();

				Lexer lexer =
						new Lexer(
								new PushbackReader(  
										new FileReader(arquivo), 1024)); 
				Token token;
				
				System.out.println("-------------" + file.getName().toUpperCase() + "-------------");
				
				while(!((token = lexer.next()) instanceof EOF)) {
					System.out.println(token.getClass() + "PENIS GIGANTE");
					//System.out.println(": "+token.toString()+" \n");
				}
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
}