package site.projectname.nicasm;

import static site.projectname.nicasm.NICSyntax.*;

/**
 * Enumerable representation of all valid base commands
 * @author  Brian Donald
 * @version 1.0
 * @since   2017-4-22
 */
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
    /**
     * Regular expression for the command
     */
    public final String regex;
    /**
     * First four bits of a command, indicating which command to execute on the processor
     */
    public final String firstFour;
    /**
     * Syntax for output, can be made up of 0's, 1's, X's, and any token containing 'R'
     * '0' indicates force 0, '1' indicates force 1, 'X' indicates the bit will be handled by a special case, a token containin 'R' indicates a register value will be loaded
     */
    public final String syntax;
    /**
     * Short string version of the command, used for finding a command enum dynamically
     */
    public final String value;
    /**
     * @param   value       Short string version of the command, used for finding a command enum dynamically
     * @param   regex       Regular expression for the command
     * @param   firstFour   First four bits of a command, indicating which command to execute on the processor
     * @param   syntax      Syntax for output, can be made up of 0's, 1's, X's, and any token containing 'R'
     */
    Command(String value,String regex,String firstFour,String syntax){
        this.regex = regex;
        this.firstFour = firstFour;
        this.value=value;
        this.syntax = syntax;
    } Command(){this.regex = "";this.firstFour="";this.value="";this.syntax="";}

    /**
     * Gets a command based on the string value
     * @param   value   Value matching Command.value to cast to Command
     * @return          Command with value matching input
     */
    public static Command get(String value){
        if(value.startsWith("BR"))
            value = value.substring(0,2);

        for(Command c: Command.values()){
            if(c.value.equals(value)){
                return c;
            }
        } return null;
    }
    /**
     * Creates a regular expression that will accept all commands
     * @return      Regular expression containing a token that will accept all possible commands
     */
    public static String allCommands(){
		String out = "";
		for(Command c: Command.values()){
			out += c.value + "|";
		}
		while(out.endsWith("|"))
			out = out.substring(0,out.length()-1);
		return out;
	}
    /**
     * Checks if given value exists as a command
     * @param   value   Value to check for
     * @return          If Command.value exists
     */
    public static boolean contains(String value){
        if(value.startsWith("BR"))
            value = value.substring(0,2);
        for(Command c: Command.values()){
            if(c.value.equals(value)){
                return true;
            }
        }
        return false;
    }
}
