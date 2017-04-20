package site.projectname.err;

import java.util.Arrays;
import site.projectname.util.Logger;

public class SyntaxErrorException extends Exception {
    private String message;
    public SyntaxErrorException(String line, String regex, int lineNumber, Logger log){
        log.debug("SyntaxErrorException -->");
        log.indent();
        log.debug("Generating syntax error message for");
        log.debug(line,1);
        log.debug("Based on");
        log.debug(regex,1);
        String[] regexs = extractRegex(regex,log);
        String message = createMessage(line,regexs,lineNumber,log);
        this.message = message;
        log.unindent();
        log.unindent();
    }
    private String[] extractRegex(String regex, Logger log){
        log.debug("Splitting " + regex);
        int level = 0;
        String temp = "";
        String[] out = new String[0];
        for(char c: regex.toCharArray()){
            if(c == '('){
                if(level > 0)
                    temp += c;
                level ++;
            }
            else if (c == ')'){
                level --;
                if(level == 0){
                    if(temp.equals("[\\s]+")||temp.equals("[\\s]*")||temp.equals("[,][ ]*")){
                        log.debug("Skipping whitespace token",1);
                        temp = "";
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
                            log.debug(out[i],2);
                        log.debug("",1);
                    }
                } else
                    temp += c;
            } else
                temp += c;
        }
        return out;
    }

    private String createMessage(String line,String[] regexs,int lineNumber,Logger log){
        int regexsIndex = 0;
        int spaceCounter = 0;
        String message = "";
        message += "Syntax error on line " + lineNumber +".\n\t";
        message += line+"\n\t";
        log.debug("Checking Input");
        log.debug("Tokens:"+Arrays.toString(line.replace(","," ").replaceAll("[\\s]+"," ").split(" ")),1);
        String[] parts = line.replace(","," ").replaceAll("[\\s]+"," ").split(" ");
        if(parts.length < regexs.length){
            for(int i=regexs.length-parts.length;i>0;i--)
                spaceCounter += parts[i].length();
            String spacer = "";
            for(int i=0;i<spaceCounter;i++)
                    spacer += " ";

            message += spacer + "^ MISSING TOKEN!\n\t";
            message += "Token must match " + regexs[parts.length-1];
            return message;
        }
        for(String s: parts){
            log.debug("Adding " + s.length() + " to spaceCounter",1);
            log.debug("\tToken: " + s,1);
            if(!s.matches(regexs[regexsIndex])){
                log.debug("|-\t Error <<<<<<<");
                // Error has been located!
                // Make things pretty
                String spacer = "";
                for(int i=0;i<spaceCounter;i++)
                        spacer += " ";
                // Construct message
                message += spacer + "^ INVALID TOKEN\n\t";
                message += "Token must match " + regexs[regexsIndex];
                break;
            } else {
                // temp matches, continue
                spaceCounter += s.length() + 1;
                log.debug(s+" matches "+regexs[regexsIndex],1);
                regexsIndex++;
            }
        }
        return message;
    }

    public SyntaxErrorException(String message, Logger out){
        this(message);
        for(String s: message.split("\n"))
            out.write(s);
    }
    public SyntaxErrorException(String message){
        this.message = message;
    }
    public SyntaxErrorException(){
        this("Unknown error encountered, stopping!");
    }
    public String getMessage(){
        return this.message;
    }
}
