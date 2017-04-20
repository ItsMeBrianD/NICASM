package site.projectname.nicasm;

import static site.projectname.nicasm.REGEX.*;

public enum Command {
    ADD ("ADD",  "(ADD)([\\s]+)" +REGISTER+SPACE+REGISTER+SPACE+"("+REGISTER+"|"+IMM5+")",  "0001", "DR SR1 XXX SR2 "),
    AND ("AND",  "(AND)([\\s]+)" +REGISTER+SPACE+REGISTER+SPACE+"("+REGISTER+"|"+IMM5+")",  "0101", "DR SR1 XXX SR2 "),
    BR  ("BR",   "((BR)[Z]?[N]?[P]?)([\\s]+)"+LABEL,                                          "0000", "XXX XXXXXXXXX  "),
    JMP ("JMP",  "(JMP)([\\s]+)" +REGISTER,                                                 "1100", "000 BR 000000  "),
    JSR ("JSR",  "(JSR)([\\s]+)" +LABEL,                                                    "0100", "1 XXXXXXXXXXX  "),
    JSRR("JSRR", "(JSRR)([\\s]+)"+REGISTER,                                                 "0100", "000 BR 000000  "),
    LD  ("LD",   "(LD)([\\s]+)"  +REGISTER+SPACE+LABEL,                                     "0010", "DR XXXXXXXXX   "),
    LDI ("LDI",  "(LDI)([\\s]+)" +REGISTER+SPACE+LABEL,                                     "1010", "DR XXXXXXXXX   "),
    LDR ("LDR",  "(LDR)([\\s]+)" +REGISTER+SPACE+REGISTER+SPACE+"("+LABEL+"|"+IMM6+")",     "0110", "DR BR XXXXXX   "),
    LEA ("LEA",  "(LEA)([\\s]+)" +REGISTER+SPACE+LABEL,                                     "1110", "DR XXXXXXXXX   "),
    NOT ("NOT",  "(NOT)([\\s]+)" +REGISTER+SPACE+REGISTER,                                  "1001", "DR SR 111111   "),
    RET ("RET",  "(RET)([\\s]*)",                                                           "1100", "000111000000   "),
    RTI ("RTI",  "(RTI)([\\s]*)",                                                           "1000", "000000000000   "),
    ST  ("ST",   "(ST)([\\s]+)"  +REGISTER+SPACE+LABEL,                                     "0011", "SR XXXXXXXXX   "),
    STI ("STI",  "(STI)([\\s]+)" +REGISTER+SPACE+LABEL,                                     "1011", "SR XXXXXXXXX   "),
    STR ("STR",  "(STR)([\\s]+)" +REGISTER+SPACE+REGISTER+SPACE+"("+LABEL+"|"+IMM6+")",     "0111", "SR BR XXXXXX   "),
    TRAP("TRAP", "(TRAP)([\\s]+)("+LABEL +"|"+IMM8+")",                                     "1111", "0000 XXXXXXXX  "),

    FILL(".FILL",  REGEX.FILL.toString(),                                                   "XXXX", "XXXXXXXXXXXXXXXX");
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
