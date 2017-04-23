package site.projectname.nicasm;

import static site.projectname.nicasm.NICSyntax.*;

public enum Command {
    ADD  ("ADD",  "(ADD)([\\s]+)" +REGISTER+SPACE+REGISTER+SPACE+"("+REGISTER+"|"+IMM5+")",  "0001", "DR SR1 XXXXXX  "),
    AND  ("AND",  "(AND)([\\s]+)" +REGISTER+SPACE+REGISTER+SPACE+"("+REGISTER+"|"+IMM5+")",  "0101", "DR SR1 XXXXXX  "),
    BR   ("BR",   "((BR)[Nn]?[Zz]?[Pp]?)([\\s]+)("+LABEL+"|"+IMM8+")",                       "0000", "XXX XXXXXXXXX  "),
    JMP  ("JMP",  "(JMP)([\\s]+)" +REGISTER,                                                 "1100", "000 BR 000000  "),
    JSR  ("JSR",  "(JSR)([\\s]+)" +LABEL,                                                    "0100", "1 XXXXXXXXXXX  "),
    JSRR ("JSRR", "(JSRR)([\\s]+)"+REGISTER,                                                 "0100", "000 BR 000000  "),
    LD   ("LD",   "(LD)([\\s]+)"  +REGISTER+SPACE+VARIABLE,                                  "0010", "DR XXXXXXXXX   "),
    LDI  ("LDI",  "(LDI)([\\s]+)" +REGISTER+SPACE+VARIABLE,                                  "1010", "DR XXXXXXXXX   "),
    LDR  ("LDR",  "(LDR)([\\s]+)" +REGISTER+SPACE+REGISTER+SPACE+IMM6,                       "0110", "DR BR XXXXXX   "),
    LEA  ("LEA",  "(LEA)([\\s]+)" +REGISTER+SPACE+VARIABLE,                                  "1110", "DR XXXXXXXXX   "),
    NOT  ("NOT",  "(NOT)([\\s]+)" +REGISTER+SPACE+REGISTER,                                  "1001", "DR SR 111111   "),
    RET  ("RET",  "(RET)([\\s]*)",                                                           "1100", "000111000000   "),
    READ ("READ", "(READ)([\\s]+)"+REGISTER,                                                 "1000", "0 DR 00000000  "),
    PRINT("PRINT","(PRINT)([\\s]+)"+"("+REGISTER+"|"+CHAR+")",                               "1000", "XX XXX XXXXXXX "),
    ST   ("ST",   "(ST)([\\s]+)"  +REGISTER+SPACE+VARIABLE,                                  "0011", "SR XXXXXXXXX   "),
    STI  ("STI",  "(STI)([\\s]+)" +REGISTER+SPACE+VARIABLE,                                  "1011", "SR XXXXXXXXX   "),
    STR  ("STR",  "(STR)([\\s]+)" +REGISTER+SPACE+REGISTER+SPACE+IMM6,                       "0111", "SR BR XXXXXX   "),
    TRAP ("TRAP", "(TRAP)([\\s]+)("+LABEL +"|"+IMM8+")",                                     "1111", "0000 XXXXXXXX  "),
    FILL (".FILL","(.FILL)([\\s]+)"+IMM16,                                                   "XXXX", "XXXXXXXXXXXXXXXX"),
    BLK  (".BLK", "(.BLK)([\\s]+)"+IMM16,                                                    "XXXX", "XXXXXXXXXXXXXXXX");

    public final String regex;
    public final String firstFour;
    public final String syntax;
    public final String value;
    Command(String value,String regex,String firstFour,String syntax){
        this.regex = regex;
        this.firstFour = firstFour;
        this.value=value;
        this.syntax = syntax;
    } Command(){this.regex = "";this.firstFour="";this.value="";this.syntax="";}

    public static Command get(String value){
        if(value.startsWith("BR"))
            value = value.substring(0,2);

        for(Command c: Command.values()){
            if(Command.valueOf(c).equals(value)){
                return c;
            }
        } return null;
    }

    public static String allCommands(){
		String out = "";
		for(Command c: Command.values()){
			out += c.value + "|";
		}
		while(out.endsWith("|"))
			out = out.substring(0,out.length()-1);
		return out;
	}

    public static boolean contains(String value){
        if(value.startsWith("BR"))
            value = value.substring(0,2);
        for(Command c: Command.values()){
            if(Command.valueOf(c).equals(value)){
                return true;
            }
        }
        return false;
    }
    public static String valueOf(Command c){
        return c.value;
    }
}
