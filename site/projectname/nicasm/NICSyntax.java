package site.projectname.nicasm;

import java.util.ArrayList;

/**
 * Contains the master syntax for NICasm
 * @author  Brian Donald
 * @version 1.0
 * @since   2017-4-22
 */
public enum NICSyntax implements site.projectname.lang.Syntax {
    REGISTER(
        "(R[0-7])",
        "REGISTER",
        "Register"
        ),
    SPACE(
        "([,][ ]*)",
        "SPACE",
        "Comma"
        ),
    HEX5 (
        "([x](([0-9A-F])))",
        "HEX5",
        "5-bit Hex signed immediate value"
        ),
    HEX6 (
        "([x]([0-3]?([0-9A-F])))",
        "HEX6",
        "6-bit Hex signed immediate value"
        ),
    HEX8 (
        "([x]([0-9A-F]){1,2})",                           //" // Atom Syntax Highlighting is being weird. I'm spoiled.
        "HEX8",
        "8-bit Hex signed immediate value"
        ),
    HEX16(
        "([x][0-9A-F]{1,4})",
        "HEX16",
        "16-bit Hex signed immediate value"
        ),
    DEC5 (
        "([#][-]?(([1][0-5])|([0]?[0-9])))",
        "DEC5",
        "5-bit Decimal signed immediate value"
        ),
    DEC6 (
        "([#][-]?(([3][0-1])|([0-2]?[0-9])))",
        "DEC6",
        "6-bit Decimal signed immediate value"
        ),
    DEC8 (
        "([#][-]?(([1][0-2][0-7])|([0]?[0-9]?[0-9])))",
        "DEC8",
        "8-bit Decimal signed immediate value"
        ),
    DEC16(
        "([#][-]?(([3][0-2][0-7][0-6][0-7])|([0-2]?[0-9]{1,4})))", //" //
        "DEC16",
        "16-bit Decimal signed immediate value"
        ),
    IMM5 (
        "(" + HEX5  + "|" + DEC5 + ")",
        "IMM5",
        "5-bit signed immediate value"
        ),
    IMM6 (
        "(" + HEX6  + "|" + DEC6 + ")",
        "IMM6",
        "6-bit signed immediate value"
        ),
    IMM8 (
        "(" + HEX8  + "|" + DEC8 + ")",
        "IMM8",
        "8-bit signed immediate value"
        ),
    IMM16(
        "(" + HEX16 + "|" + DEC16 +")",
        "IMM16",
        "16-bit signed immediate value"
        ),
	CHAR(
		"(['][ -~]['])",
		"CHAR",
        "Character"
	       ),
    LABEL(
        "([*][A-Z]+)([\\s]*)",
        "LABEL",
        "Label"
        ),
    VARIABLE(
        "([$][A-Z]+)([\\s]*)",
        "VARIABLE",
        "Variable"
        ),
    VALID(
        "[0-9A-Z, *$x#]",
        "VALID"
        ),
    COMMAND(
        "("+Command.allCommands() + "|" + Shorthand.allCommands() +")",
        "COMMAND",
        "Command"
        ),
    HELPER; // <-- Required for Syntax Interface
    /**
     * Regular expression that represents the given token
     */
    private final String pattern;
    /**
     * Short name of token
     */
    private final String name;
    /**
     * Semantic name of token
     */
    private final String semantic;

    private NICSyntax(String pattern, String name, String h){
        this.pattern = pattern;
        this.name = name;
        this.semantic = h;
    }
    private NICSyntax(String pattern, String name){
        this(pattern,name,"");
    }
    private NICSyntax(){
        this("","","");
    }
    /**
     * Converts token to associated regular expression
     * @return      Regular expression representing token
     */
    public String toString(){
        return this.pattern;
    }
    /**
     * Gets short name of token
     * @return  Short name of token
     */
    public String getName(){
        return this.name;
    }

    /**
     * Checks if syntax contains a given value
     * @param   value   Token name to check for
     * @return          If NICSyntax.value exists
     */
    public boolean contains(String value){

        if(!value.startsWith("("))
            value = "(" + value;
        if(!value.endsWith(")"))
            value = value + ")";
        for(NICSyntax c: NICSyntax.values()){
            if(c.pattern.equals(value)){
                return true;
            }
        }
        return checkPossibles(value);
    }
    /**
     * Checks if v contains any subpatterns that exist as tokens. Useful for deconstructing commands
     * @param   v   Expression to look in
     * @return      NICSyntax.X exists within v
     */
    private boolean checkPossibles(String v){
        for(NICSyntax c: NICSyntax.values()){
            if(v.contains(c.pattern)){
                return true;
            }
        }
        return false;
    }
    /**
     * Gets any subpatterns that exist within v. Useful for deconstructing commands
     * @param   v   Expression to look in
     * @return      All subtokens within v, as semantics, in the format "X or X or X or X..."
     */
    public String getPossibles(String v){
        String out = "";
        v = "(" + v + ")";
        for(NICSyntax c: NICSyntax.values()){
            if(v.contains(c.pattern) && !c.pattern.equals("") && !(c.semantic.contains("Hex signed immediate value") || c.semantic.contains("Decimal signed immediate value"))){
                out += c.semantic + " or ";
            }// else if (c.pattern.contains(v) && c.name.equals("IMM16")){
                //out += c.semantic + " or "; // IMM16 likes to act up because COMMAND includes it as a representation of any possible immediate value
                                            // This requires a special case so a command requiring an IMM16 doesn't say it needs an IMM16 and a Command
            //}
        }
        while(out.endsWith(" or "))
            out = out.substring(0,out.length()-3);
        if(!out.equals(""))
            return out;
        return "Token";
    }

    /**
     * Checks a line for missing commas
     * @param   line    Line to check for errors
     * @param   message Pre-existing message to build on
     * @return          Message with the first missing comma located
     */
    public String errorCheck(String line, String message){
        int spaceCounter = 0;
        while(!line.startsWith(" ")){
            spaceCounter++;
            line = line.substring(1);
        } line = line.substring(1);
        line = line.replace(",[\\s]+",","); // Remove any white space between commas;
        String[] parts = line.split(",");
        for(String s: parts){
            if(s.contains(" ")){
                // Error found
                spaceCounter += s.split(" ")[0].length()+1;
                String spacer = "";
                for(int i=0;i<spaceCounter;i++)
                        spacer += " ";
                message += spacer + "^\n\t";
                message += "Missing comma";

            }
            spaceCounter += s.length();
        }
        return message;
    }
}
