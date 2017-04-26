package site.projectname.err;

import java.util.Arrays;
import site.projectname.lang.Syntax;
import site.projectname.util.Logger;

/**
 * Verbose Syntax Error finder. Uses a given {@link site.projectname.lang.Syntax Syntax} to look for errors
 * @author  Brian Donald
 * @version 1.0
 * @since   2017-4-22
 */
public class SyntaxErrorException extends Exception {
    private String message;
    /**
     * Best constructor for a SyntaxErrorException, it's Syntax scanner requires all of this information to properly create an error message
     * @param   line        Line on which the error occurs
     * @param   regex       Regular expression to use while scanning for errors. Should be built with components from the given syntax
     * @param   lineNumber  Line Number the error occurs on, used for a more verbose error message.
     * @param   syntax      Given syntax to use while searching for errors
     */
    public SyntaxErrorException(String line, String regex, int lineNumber, Enum<? extends Syntax> syntax){
        Logger log = Logger.getLog("SyntaxError",Logger.debugGlobal);
        log.debug("SyntaxErrorException -->");
        log.indent();
        log.debug("Generating syntax error message for");
        log.debug(line,1);
        log.debug("Based on");
        log.debug(regex,1);
        String[] regexs = extractRegex(regex);
        String message = createMessage(line,regexs,lineNumber,syntax);
        this.message = message;
        log.unindent();
        log.unindent();
    }
    /**
     * Breaks a regular expression into it's base tokens
     * <p>
     * Example input / output: <br>
     * Input: ([A-Z])([A-Za-z]+|[0-9]*)
     * Output: ["([A-Z])","([A-Za-z]+|[0-9]*)"]
     * @param   regex   Regular expression to break down
     * @return          Array of all 0-th level tokens
     */
    private String[] extractRegex(String regex){
        Logger log = Logger.getLog("SyntaxError",Logger.debugGlobal);
        log.debug("Splitting " + regex);
        int level = 0;
        String temp = "";
        String[] out = new String[0];
        boolean close = false;
        for(char c: regex.toCharArray()){
            if(c == '('){
                level ++;
                close = false;
                if(level == 1)
                    continue;
            }
            else if (c == ')'){
                level --;
                if(level == 0){
                    if(temp.equals("[\\s]+")||temp.equals("[\\s]*")||temp.equals("[,][ ]*")){
                        log.debug("Skipping whitespace token",1);
                        temp = "";
                        continue;
                    } else {
                        // Add regex to output
                        log.debug("Current out.length : " + out.length,1);
                        log.debug("Adding String : " + temp,1);
                        // Scale output array by + 1
                        String[] tempA = new String[out.length+1];
                        // Copy existing data
                        for(int i=0;i<out.length;i++)
                            tempA[i] = out[i];
                        // Add new String
                        tempA[out.length] = temp;
                        temp = "";
                        out = tempA;
                        log.debug("Updated out.length : " + out.length,1);
                        log.debug("out :\t" + out[0],1);
                        for(int i=1;i<out.length;i++)
                            log.debug("\t"+out[i],2);
                        log.debug("",1);
                        close = true;
                        continue;
                    }
                } else {
                    close = false;
                }
            } else if((c == '*' || c == '+') && close) {
                out[out.length-1] = out[out.length-1] + c;
                close = false;
            } else {
                close = false;
            } temp += c;
        }
        return out;
    }

    /**
     * Creates an error message basd on a given series of Regular Expressions (For best results take from extractRegex), a {@link site.projectname.lang.Syntax Syntax}, and a line
     * @param       line        Line to parse for errors
     * @param       regexs      Array of Regular Expressions in the order they should appear as space or comment seperated tokens
     * @param       lineNumber  Line Number where the error occurs, used to create verbose message
     * @param       syntaxEnum  Should (but doesn't have to be) any Syntax.HELPER, used to access "static" methods in the syntax
     * @return                  Error message pointing to the error on the line with a description
     */
    private String createMessage(String line,String[] regexs,int lineNumber, Enum<? extends Syntax> syntaxEnum){
        Syntax syntax = (Syntax) syntaxEnum;
        Logger log = Logger.getLog("SyntaxError",Logger.debugGlobal);
        int regexsIndex = 0;
        int spaceCounter = 0;
        String message = "";
        message += "Syntax error on line " + lineNumber +".\n\t";
        message += line+"\n\t";
        log.debug("Checking Input");
        log.debug("Tokens:"+Arrays.toString(line.replace(","," ").replaceAll("[\\s]+"," ").split(" ")),1);
        String[] parts = line.replace(","," ").replaceAll("[\\s]+"," ").split(" ");
        if(parts.length < regexs.length && !regexs[regexs.length-1].endsWith("*")){
            for(int i=regexs.length-parts.length-1;i>0;i--)
                spaceCounter += parts[i].length();
            String spacer = "";
            for(int i=0;i<line.length();i++)
                    spacer += " ";

            message += spacer + "^\n\t";
            message += syntax.getPossibles(regexs[regexs.length-1]) + "required";
            log.debug("Parts Length: " + parts.length);
            log.debug("Regexs Length: " + regexs.length);
            return message;
        }
        for(String s: parts){
            log.debug("\tToken: " + s,1);
            log.indent();
            if(!s.matches(regexs[regexsIndex])){
                // Error has been located!
                // Make things pretty
                String spacer = "";
                for(int i=0;i<spaceCounter;i++)
                        spacer += " ";
                // Construct message
                if(syntax.contains(regexs[regexsIndex])){
                    if(s.length() > 1)
                        message += spacer +"^";
                    else
                        message += spacer;
                    for(int i=0; i<s.length()-2;i++)
                        message += "-";
                    message += "^\n\t" + syntax.getPossibles(regexs[regexsIndex]) + "required";
                    return message;
                } else {
                    message += spacer + "^ INVALID TOKEN\n\t";
                    message += "Token must match " + regexs[regexsIndex];
                    return message;
                }
            } else {
                // temp matches, continue
                spaceCounter += s.length() + 1;
                log.debug(s+" matches "+regexs[regexsIndex],1);
                regexsIndex++;
            }
            log.debug("Adding " + s.length() + " to spaceCounter",1);
            log.unindent();
        }
        message = syntax.errorCheck(line,message);
        return message;
    }

    /**
     * Acts as a regular exception, with the addition of outputing the error to the given log
     * @param   message     Message to print
     * @param   out         Logger to write to
     */
    public SyntaxErrorException(String message, Logger out){
        this(message);
        for(String s: message.split("\n"))
            out.write(s);
    }
    /**
     * Acts as a regular Exception, however getMessage must be invoked to print
     * @param   message     Error message
     */
    public SyntaxErrorException(String message){
        this.message = message;
    }
    /**
     * Returns the message stored in the exception
     * @return      Error Message
     */
    public String getMessage(){
        return this.message;
    }
}
