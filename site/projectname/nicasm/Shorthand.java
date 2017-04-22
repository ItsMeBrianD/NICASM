package site.projectname.nicasm;

import static site.projectname.nicasm.REGEX.*;
import site.projectname.err.SyntaxErrorException;
import site.projectname.util.Logger;

public enum Shorthand{
	SUB(".SUB","(.SUB)([\\s]+)"+REGISTER+SPACE+REGISTER+SPACE+"("+REGISTER+"|"+IMM5+")",
		".SUB DR SR1 SR2",
		new String[]{
			"NOT SR2,SR2",
			"ADD SR2,SR2,#1",
			"ADD DR,SR1,SR2"}
	),
	MUL(".MUL","(.MUL)([\\s]+)"+REGISTER+SPACE+REGISTER+SPACE+"("+REGISTER+"|"+IMM5+")",
		".MUL DR SR1 SR2",
		new String[]{
			"ADD DR,DR,SR2",
			"ADD SR1,SR1,#-1",
			"BRP #-2"
		}
	);
	public final String value;
	public final String regex;
	public final String syntax;
	public final String[] output;
	private Shorthand(String value,String regex,String syntax,String[] output){
		this.value = value;
		this.regex = regex;
		this.syntax = syntax;
		this.output = output;
	}

	public static boolean contains(String value){
        for(Shorthand c: Shorthand.values()){
            if(c.value.equals(value)){
                return true;
            }
        }
        return false;
    }

	public static Shorthand get(String value){
		for(Shorthand c: Shorthand.values())
			if(c.value.equals(value))
				return c;
		return null;
	}

	public boolean checkSyntax(String in){
		return in.matches(this.regex);
	}
	public String[] convertSyntax(String in,int lineAddr)throws SyntaxErrorException{
		if(!this.checkSyntax(in))
			throw new SyntaxErrorException(in,this.regex,lineAddr);
		String[] parts = in.replace(","," ").replace("[\\s]+"," ").split(" ");
		String[] converted = new String[this.output.length];
		int i = 0;
		for(String out: output){
			out = out.replaceAll("DR",parts[1]);
			out = out.replaceAll("SR1",parts[2]);
			out = out.replaceAll("SR2",parts[3]);
			converted[i++] = out;
		}
		return converted;
	}
}
