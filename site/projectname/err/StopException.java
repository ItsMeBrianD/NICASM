package site.projectname.err;

import site.projectname.util.Logger;
/**
 * Exception that should stop the program
 * @author  Brian Donald
 * @version 1.0
 * @since   2017-4-22
 */
public class StopException extends Exception {
    /**
     * Throws a stop exception with output to both System.err and a given log and halts the program
     * @param   message Message defining exception
     * @param   out     Log file to write message to
     */
    public StopException(String message, Logger out){
        System.err.println(message);
        for(String s: message.split(" "))
            out.write(s);
        System.exit(-1);
    }
    /**
     * Throws a stop exception with output to System.err and halts the program
     * @param   message Message defining exception
     */
    public StopException(String message){
        System.err.println(message);
        System.exit(-1);
    }
    /**
     * Throws a stop exception with "Unknown error encountered" to System.err and halts the program
     */
    public StopException(){
        System.err.println("Unknown error encountered, stopping!");
        System.exit(-1);
    }
}
