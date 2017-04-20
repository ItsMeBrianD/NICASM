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
        "([x]([0,1]([0-9]|[A-F])))",
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
        "([#](([0-2]?[0-9])|([3][0-1])))",
        "DEC5"
        ),
    DEC6 (
        "([#](([0-5]?[0-9])|([6][0-3])))",
        "DEC6"
    ),
    DEC8 (
        "([#]([0-2]?[0-9]?[0-9]?))",
        "DEC8"
    ),
    DEC16(
        "([#](([6][0-5]{2}[0-3][0-5])|([0-5][0-9]{1,4})))", //" //
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
    LABEL(
        "([*][A-Z]+)([\\s]*)",
        "LABEL"
    ),
    FILL (
        "([\\.]FILL[\\s]+)" + IMM16,
        "FILL",
        ".FILL IMM16"
    ),
    BLK  (
        "([\\.]BLK[\\s]+)" + IMM8,
        "BLK",
        ".BLK IMM8"
    ),
    VARIABLE(
        "([$][A-Z]+)([\\s]+)("+FILL+"|"+BLK+")",
        "VARIABLE",
        "($[A-Z]*) (FILL|BLK)"
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
