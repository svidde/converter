import java.util.Scanner;

public class Main {
	
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		Converter c = new Converter();
	    Scanner input = new Scanner(System.in);
	    
	    System.out.print("Enter filename FROM without .txt: ");
	    String fromFile = input.next();
	    System.out.print("Enter filename TO without .xml: ");
	    String toFile = input.next();

		c.setFromFile(String.format("%s.txt", fromFile));
		c.setTofile(String.format("%s.xml", toFile));
		
		if ( c.convert() ) {
			System.out.println( String.format("Successfully created %s.xml from %s.txt", toFile, fromFile) );
		} else {
			System.out.println("Error");
		}
	}
}