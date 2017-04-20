package site.projectname.nicasm;

public enum REGEX{
    REGISTER(
        "(R[0-7])",
        "REGISTER"
    ),
    SPACE(
        "([,][ ]*)",
        "SPACE"
    ),
    HEX5 (
        "([x](([0-9]|[A-F])))",
        "HEX5"
    ),
    HEX6 (
        "([x]([0-3]?([0-9]|[A-F])))",
        "HEX6"
    ),
    HEX8 (
        "([x]([0-9]|[A-F]){1,2})",                           //" // Atom Syntax Highlighting is being weird. I'm spoiled.
        "HEX8"
    ),
    HEX16(
        "([x][0-9A-F]{1,4})",
        "HEX16"
    ),
    DEC5 (
        "([#][-]?(([1][0-5])|([0]?[0-9])))",
        "DEC5"
        ),
    DEC6 (
        "([#][-]?(([3][0-1])|([0-2]?[0-9])))",
        "DEC6"
    ),
    DEC8 (
        "([#][-]?(([1][0-2][0-7])|([0]?[0-9]?[0-9])))",
        "DEC8"
    ),
    DEC16(
        "([#][-]?(([3][0-2][0-7][0-6][0-7])|([0-2]?[0-9]{1,4})))", //" //
        "DEC16"
    ),
    IMM5 (
        "(" + HEX5  + "|" + DEC5 + ")",
        "IMM5",
        "(HEX5|DEC5)"
    ),
    IMM6 (
        "(" + HEX6  + "|" + DEC6 + ")",
        "IMM6",
        "(HEX6|DEC6)"
    ),
    IMM8 (
        "(" + HEX8  + "|" + DEC8 + ")",
        "IMM8",
        "(HEX8|DEC8)"
    ),
    IMM16(
        "(" + HEX16 + "|" + DEC16 +")",
        "IMM16",
        "(HEX16|DEC16)"
    ),
	CHAR(
		"['][ -~][']",
		"CHAR"
	),
    LABEL(
        "([*][A-Z]+)([\\s]*)",
        "LABEL"
    ),
    FILL (
        "([\\.]FILL[\\s]+)(" + IMM16 + "|" + CHAR + ")",
        "FILL",
        ".FILL (IMM16|CHAR)"
    ),
    BLK  (
        "([\\.]BLK[\\s]+)" + IMM8,
        "BLK",
        ".BLK IMM8"
    ),
    VARIABLE(
        "([$][A-Z]+)([\\s]*)",
        "VARIABLE",
        "($[A-Z]*)"
    ),
    VALID(
        "[0-9]|[A-Z]|[,]|[ ]|[*]|[$]|[x]|[#]",
        "VALID"
    );
    private final String pattern,name,recipie;
    private REGEX(String pattern, String name, String recipie){
        this.pattern = pattern;
        this.name = name;
        this.recipie = recipie;
    }
    private REGEX(String pattern,String name){
        this(pattern,name,"");
    }
    public String toString(){
        return this.pattern;
    }
    public String getName(){
        return this.name;
    }
    public String getRecipie(){
        return this.recipie;
    }
}
