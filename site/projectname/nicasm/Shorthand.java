package site.projectname.nicasm;

import java.util.HashMap;

import static site.projectname.nicasm.NICSyntax.*;
import site.projectname.err.SyntaxErrorException;
import site.projectname.util.Logger;

public enum Shorthand{
	// Math
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
	),
	// Stack
	INIT(".INIT","(.INIT)",
		".INIT",
		new String[]{
			"LD R5,$STACK",
			"LD R6,$STACK",
			"ADD R6,R6,#1"
		}
	),
	PUSH(".PUSH","(.PUSH)([\\s]+)"+REGISTER,
		".PUSH DR",
		new String[]{
			"ADD R6,R6,#1",
			"STR DR,R6,#0"
		}
	),
	GETV(".GETV","(.GETV)([\\s]+)"+REGISTER+SPACE+IMM5,
		".GETV DR IMM5",
		new String[]{
			"LDR DR,R6,IMM5"
		}

	),
	SETV(".SETV","(.SETV)([\\s]+)"+IMM5+SPACE+REGISTER,
		".SETV IMM5 DR",
		new String[]{
			"STR DR,R6,IMM5"
		}
	),
	GETP(".GETP","(.GETP)([\\s]+)"+REGISTER+SPACE+IMM5,
		".GETP DR IMM5",
		new String[]{
			"ADD R5,R5,#-3",
			"LDR DR,R5,IMM5",
			"ADD R5,R5,#3"
		}
	),
	SETP(".SETP","(.SETP)([\\s]+)"+IMM5+SPACE+REGISTER,
		".SETP IMM5 DR",
		new String[]{
			"ADD R5,R5,#-3",
			"STR DR,R5,IMM5",
			"ADD R5,R5,#3"
		}
	),
	RETURN(".RETURN","(RETURN)([\\s]+)"+REGISTER,
		".RETURN DR",
		new String[]{
			"COPY R6,R5", // R6 = R5
			"POP R5",
			"RET",
			"POP R7",
			"POP DR",
			"COPY R6,R5",
			"ADD R6,R6,#1"
		}
	),
	// Utility
	COPY(".COPY","(.COPY)([\\s]+)"+REGISTER+SPACE+REGISTER,
		".COPY DR,SR1",
		new String[]{
			"ADD DR,SR1,#0"
		}
	),
	ZERO(".ZERO","(.ZERO)[\\s]+"+REGISTER,
		".ZERO R",
		new String[]{
			"AND R,R,#0"
		}
	),
	MAIN(".MAIN","(.MAIN)[\\s]*",
		".MAIN",
		new String[]{
			".FILL x0000"
		}
	);
	public final String value;
	public final String regex;
	public final String syntax;
	public final String[] output;
	public static String allCommands(){
		String out = "";
		for(Shorthand c: Shorthand.values()){
			out += c.value + "|";
		}
		while(out.endsWith("|"))
			out = out.substring(0,out.length()-1);
		return out;
	}
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
	public String[] convertSyntax(String in,int lineNum)throws SyntaxErrorException{
		if(!this.checkSyntax(in))
			throw new SyntaxErrorException(in,this.regex, lineNum, NICSyntax.HELPER);
		String[] parts = in.replace(","," ").replace("[\\s]+"," ").split(" ");
		String[] converted = new String[this.output.length];
		int i = 1;
		HashMap<String,String> map = new HashMap<String,String>();
		for(String key: syntax.replace(","," ").split(" ")){
			if(!key.equals(value))
				map.put(key,parts[i++]);
		}
		i = 0;
		for(String out: output){
			String p = out.replace(",[\\s]+",",").split(" ")[1];
			out = out.replace(",[\\s]+",",").split(" ")[0];
			for(String key: map.keySet())
				p = p.replaceAll(key,map.get(key));
			converted[i++] = out +" "+p;
		}
		return converted;
	}
}
