import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class Converter {

	private String lastGroup;
	private ArrayList<String> input;
	private String fromFile;
	private String tofile;
	private int currentIndentation;
	private boolean validPers;
	private boolean validFam;
	private String delimiter;
	private StringBuilder xmlContent;
	
	public Converter() {
		this.init();
	}
	
	//Getters and setters for the filenames
	public String getFromFile() {
		return fromFile;
	}
	public void setFromFile(String fromFile) {
		this.fromFile = fromFile;
	}
	public String getTofile() {
		return tofile;
	}
	public void setTofile(String tofile) {
		this.tofile = tofile;
	}
	
	public boolean convert() {
		if (getfile()) {
			getContent();
			return writeToFile();
		}
		return false;
	}
	
	private String startTag(String tag) {
		StringBuilder toReturn = new StringBuilder();
		for (int i = 0; i < currentIndentation; i++) {
			toReturn.append("\t");
		}
		currentIndentation++;
		return String.format("%s<%s>", toReturn.toString(), tag);
	}
	private String startTagln(String tag) {
		return startTag(tag) + "\n";
	}
	private String closeTag(String tag) {
		currentIndentation--;
		return "</"+tag+">\n";
	}
	private String closeTagIndentation(String tag) {
		StringBuilder toReturn = new StringBuilder();
		for (int i = 0; i < currentIndentation-1; i++) {
			toReturn.append("\t");
		}
		return toReturn.toString() + closeTag(tag);
	}

	private void init() {
		this.input = new ArrayList<String>();
		this.lastGroup = "people";
		this.currentIndentation = 0;
		this.validPers = true;
		this.delimiter = "\\|";
		this.xmlContent = new StringBuilder();
		this.xmlContent.append(startTagln("people"));
	}
	
	private boolean getfile() {
		try {
			BufferedReader in;
			in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(this.fromFile)), "utf-8"));
			String str;
			while ((str = in.readLine()) != null) {
				input.add(str);
			}
			in.close();  
			return true;   
		} catch (UnsupportedEncodingException e) {
			System.out.println(e.getMessage());	//e.printStackTrace();
			return false;
		} catch (IOException e) {
			System.out.println(e.getMessage());	//e.printStackTrace();
			return false;
		}
	}
	
	// Read each line in the ArrayList<String>, call different functions depending on the first char of the string
	private void getContent() {
		for (int i = 0 ; i < input.size() ; i++) {
			if (this.input.get(i).toUpperCase().startsWith("P")) {
				if (this.validPerson(i)) {	// Dont try to read the person if it isnt valid			
					this.xmlContent.append(getPerson(i));
				}
			} else if (this.input.get(i).toUpperCase().startsWith("A")) {
				if (this.validAddress(i) && this.validFam && this.validPers) { // Dont try to read the address if it isnt valid or the person/family isnt valid
					this.xmlContent.append(getAddress(i));
				}	
			} else if (this.input.get(i).toUpperCase().startsWith("T")) {
				if (this.validPhone(i) && this.validPers && this.validFam) { // Dont try to read the phone if it isnt valid or the person/family isnt valid
					this.xmlContent.append(getPhone(i));
				}
			} else if (this.input.get(i).toUpperCase().startsWith("F")) {
				if (this.validFamily(i) && this.validPers) { // Dont try to read the family if it isnt valid or the person isnt valid
					this.xmlContent.append(getFamily(i)); 
				}
			}
		}
		if (this.lastGroup.equals("family")) {
			this.xmlContent.append(closeTagIndentation("family"));
		} else if (this.lastGroup.equals("person")){
			this.xmlContent.append(closeTagIndentation("person"));
		}
		this.xmlContent.append(closeTagIndentation("people"));
	}
	
	// Write this.xmlContent to file (utf-8)
	private boolean writeToFile() {
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.tofile), "utf-8"))) {
		    writer.write(this.xmlContent.toString());
		    return true;
		} catch (IOException ex) {
			System.out.println(ex.getMessage());
			return false;
		} 
	}
	
	// Valid person when split and array == 3
	private boolean validPerson(int i) {
		int validPLength = 3;
		String[] person = this.input.get(i).split(this.delimiter);
		if (person.length == validPLength) {
			this.validPers = true;
			this.validFam = true;
			return true;	
		} else {
			this.validPers = false;
			System.out.println(String.format("Person at line %s isnt valid", i+1));
			return false;
		}
	}	
	// Valid person when the split array == 3 && followed with a, t or f
	/*
	private boolean validPerson(int i) {
		int next = (i+1 >= this.input.size()) ? i : i+1;
		String[] person = this.input.get(i).split(this.delimiter);
		if (person.length == 3 && this.input.get(next).startsWith("A") ||
				person.length == 3 && this.input.get(next).startsWith("T") ||
				person.length == 3 && this.input.get(next).startsWith("F")) {
			this.validPers = true;
			return true;	
		} else {
			this.validPers = false;
			System.out.println(String.format("Person at line %s isnt valid", i+1));
			return false;
		}
	}
	 */
	// Valid address when the split array == 4
	private boolean validAddress(int i) {
		int validALength = 4;
		String[] address = this.input.get(i).split(this.delimiter);
		if (address.length == validALength && i > 0) {
			return true;	
		} else {
			System.out.println(String.format("Address at line %s isnt valid", i+1));
			return false;
		}
	}
	// Valid phone when the split array == 3
	private boolean validPhone(int i) {
		int validALength = 3;
		String[] phone = this.input.get(i).split(this.delimiter);
		if (phone.length == validALength && i > 0) {
			return true;	
		} else {
			System.out.println(String.format("Phone at line %s isnt valid", i+1));
			return false;
		}
	}
	// Valid family when the split array == 3 && not first element.
	private boolean validFamily(int i) {
		int validFLength = 3;
		String[] family = this.input.get(i).split(this.delimiter);
		if (family.length == validFLength && i > 0) {
			this.validFam = true;
			return true;	
		} else {
			System.out.println(String.format("Family at line %s isnt valid", i + 1));
			this.validFam = false;
			return false;
		}
	}
	// Valid family when the split array == 3 && not first element && followed with a or t. 
	/*
	private boolean validFamily(int i) {
		int validFLength = 3;
		int next = (i+1 >= this.input.size()) ? i : i+1;
		String[] family = this.input.get(i).split(this.delimiter);
		if (family.length == validFLength && this.input.get(next).startsWith("A") && i > 0 ||
				family.length == validFLength && this.input.get(next).startsWith("T") && i > 0) {
			this.validFam = true;
			return true;	
		} else {
			System.out.println(String.format("Family at line %s isnt valid", i+1));
			this.validFam = false;
			return false;
		}
	}*/
	 
	
	private String getAddress(int i) {
		String[] address = this.input.get(i).split(this.delimiter);
		StringBuilder toReturn = new StringBuilder();
		String street = address[1];
		String city = address[2];
		String zip = address[3];
		toReturn.append(startTagln("address"));
		toReturn.append(String.format("%s%s%s", startTag("street"), street, closeTag("street")));
		toReturn.append(String.format("%s%s%s", startTag("city"), city, closeTag("city")));
		toReturn.append(String.format("%s%s%s", startTag("zip"), zip, closeTag("zip")));
		return String.format("%s%s", toReturn.toString(), closeTagIndentation("address"));
	}
	private String getPhone(int i) {
		String[] phone = this.input.get(i).split(this.delimiter);
		StringBuilder toReturn = new StringBuilder();
		String mobile = phone[1];
		String landline = phone[2];
		toReturn.append(startTagln("phone"));
		toReturn.append(String.format("%s%s%s", startTag("mobile"), mobile, closeTag("mobile")));
		toReturn.append(String.format("%s%s%s", startTag("landline"), landline, closeTag("landline")));
		return String.format("%s%s", toReturn.toString(), closeTagIndentation("phone"));
	}
	private String getPerson(int i) {
		String[] person = this.input.get(i).split(this.delimiter);
		StringBuilder toReturn = new StringBuilder();
		String firstname = person[1];
		String lastname = person[2];
		if (this.lastGroup.equals("family")) {
			toReturn.append(closeTagIndentation("family"));
			toReturn.append(closeTagIndentation("person"));
		} else if (this.lastGroup.equals("person")) {
			toReturn.append(closeTagIndentation("person"));
		}
		toReturn.append(startTagln("person"));
		toReturn.append(String.format("%s%s%s", startTag("firstname"), firstname, closeTag("firstname")));
		toReturn.append(String.format("%s%s%s", startTag("lastname"), lastname, closeTag("lastname")));
		this.lastGroup = "person";
		return toReturn.toString();
	}
	private String getFamily(int i) {
		String[] family = this.input.get(i).split(this.delimiter);
		StringBuilder toReturn = new StringBuilder();
		String name = family[1];
		String born = family[2];
		if (this.lastGroup.equals("family")) {
			toReturn.append(closeTagIndentation("family"));
		}
		toReturn.append(startTagln("family"));
		toReturn.append(String.format("%s%s%s", startTag("name"), name, closeTag("name")));
		toReturn.append(String.format("%s%s%s", startTag("born"), born, closeTag("born")));
		this.lastGroup = "family";
		return toReturn.toString();
	}
}

