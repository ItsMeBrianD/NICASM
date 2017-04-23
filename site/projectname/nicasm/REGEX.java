package site.projectname.nicasm;

import java.util.ArrayList;

public enum REGEX implements site.projectname.lang.Syntax {
    REGISTER(
        "(R[0-7])",
        "REGISTER",
        "Register"
    ),
    SPACE(
        "([,][ ]*)",
        "SPACE",
        ""
    ),
    HEX5 (
        "([x](([0-9]|[A-F])))",
        "HEX5",
        "5-bit Hex Immediate value"
    ),
    HEX6 (
        "([x]([0-3]?([0-9]|[A-F])))",
        "HEX6",
        "6-bit Hex Immediate value"
    ),
    HEX8 (
        "([x]([0-9]|[A-F]){1,2})",                           //" // Atom Syntax Highlighting is being weird. I'm spoiled.
        "HEX8",
        "8-bit Hex Immediate value"
    ),
    HEX16(
        "([x][0-9A-F]{1,4})",
        "HEX16",
        "16-bit Hex Immediate value"
    ),
    DEC5 (
        "([#][-]?(([1][0-5])|([0]?[0-9])))",
        "DEC5",
        "5-bit Decimal Immediate value"
        ),
    DEC6 (
        "([#][-]?(([3][0-1])|([0-2]?[0-9])))",
        "DEC6",
        "6-bit Decimal Immediate value"
    ),
    DEC8 (
        "([#][-]?(([1][0-2][0-7])|([0]?[0-9]?[0-9])))",
        "DEC8",
        "8-bit Decimal Immediate value"
    ),
    DEC16(
        "([#][-]?(([3][0-2][0-7][0-6][0-7])|([0-2]?[0-9]{1,4})))", //" //
        "DEC16",
        "16-bit Decimal Immediate value"
    ),
    IMM5 (
        "(" + HEX5  + "|" + DEC5 + ")",
        "IMM5",
        "5-bit Immediate value"
    ),
    IMM6 (
        "(" + HEX6  + "|" + DEC6 + ")",
        "IMM6",
        "6-bit Immediate value"
    ),
    IMM8 (
        "(" + HEX8  + "|" + DEC8 + ")",
        "IMM8",
        "8-bit Immediate value"
    ),
    IMM16(
        "(" + HEX16 + "|" + DEC16 +")",
        "IMM16",
        "16-bit Immediate value"
    ),
	CHAR(
		"['][ -~][']",
		"CHAR",
        "Character"
	),
    LABEL(
        "([*][A-Z]+)([\\s]*)",
        "LABEL",
        "Label"
    ),
    FILL (
        "([\\.]FILL[\\s]+)(" + IMM16 + "|" + CHAR + ")",
        "FILL",
        ".FILL Command"
    ),
    BLK  (
        "([\\.]BLK[\\s]+)" + IMM8,
        "BLK",
        ".BLK Command"
    ),
    VARIABLE(
        "([$][A-Z]+)([\\s]*)",
        "VARIABLE",
        "($[A-Z]*)"
    ),
    VALID(
        "[0-9]|[A-Z]|[,]|[ ]|[*]|[$]|[x]|[#]",
        "VALID"
    ),
    COMMAND(
        "("+Command.allCommands() + Shorthand.allCommands() +")(R[0-9]|"+IMM16+")"+"("+SPACE+"(R[0-9]|"+IMM16+"))*[\\s]*",
        "COMMAND",
        "Command"
    ),
    HELPER; // <-- Required for Syntax Interface
    private final String pattern;
    private final String name;
    private final String semantic;

    private REGEX(String pattern, String name, String h){
        this.pattern = pattern;
        this.name = name;
        this.semantic = h;
    }
    private REGEX(String pattern, String name){
        this(pattern,name,"");
    }
    private REGEX(){
        this("","","");
    }
    public String toString(){
        return this.pattern;
    }
    public String getName(){
        return this.name;
    }
    public String getSemantic(String s){
        for(REGEX r: REGEX.values())
            if(r.semantic.equals(s))
                return r.semantic;
        return null;
    }
    public boolean contains(String value){
        for(REGEX c: REGEX.values()){
            if(c.pattern.equals(value)){
                return true;
            }
        }
        return checkPossibles(value);
    }
    private boolean checkPossibles(String v){
        for(REGEX c: REGEX.values()){
            if(v.contains(c.pattern) || c.pattern.contains(v)){
                return true;
            }
        }
        return false;
    }
    public String getPossibles(String v){
        String out = "";
        System.out.println(COMMAND);
        for(REGEX c: REGEX.values()){
            if((v.contains(c.pattern) || c.pattern.contains(v)) && !c.pattern.equals("") && !(c.semantic.contains("Hex Immediate value") || c.semantic.contains("Decimal Immediate value"))){
                out += c.semantic + " or ";
            }
        }
        while(out.endsWith(" or "))
            out = out.substring(0,out.length()-3);
        if(!out.equals(""))
            return out;
        return "Token";
    }
}
