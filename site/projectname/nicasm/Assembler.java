package site.projectname.nicasm;

import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.io.File;

import site.projectname.util.Logger;
import site.projectname.util.Numbers;
/**
 * Assembles .nic files into usable hex-format for nicpc
 *
 * @author      Brian Donald
 * @version     0.0
 * @since       2017-4-18
 */
public class Assembler{

    public static String fileName;
    public static Logger log;
    private static Scanner file;
    private static int lineNumber = 0;
    /**
     * All valid Assembly Commands for nic
     */
    public enum Command {
        ADD( "(ADD)[ ]+ " + regRegEx + spaRegEx + regRegEx + "(" + regRegEx + "|" + immRegEx + ")"),
        AND,
        BR,
        JMP,
        JSRR,
        LD,
        LDI,
        LDR,
        LEA,
        NOT,
        RET,
        RTI,
        ST,
        STI,
        STR,
        TRAP;
        public final String regex;
        Command(String regex){
            this.regex = regex;
        } Command(){this.regex = "";}

        public static boolean contains(String value){
            try{
                if(Command.valueOf(value).equals(value))
                    return true;
            } catch(Exception e){}
            return false;
        }
    }

    /**
     * Regex for Register inputs, i.e. R0-R7
     */
    public final static String  regRegEx = "(R[0-7])";

    public final static String  spaRegEx = "[,][ ]*";

    public final static String  immRegEx = "([#](([0-5]{0,1}[0-9])|([6][0-3])))|([x]([0,1]([0-9]|[A-F])))";

    public static String convertLine(String line) {
        line = line.split(";")[0]; // Disregard Comments
        if(Command.contains(line.split(" ")[0])){
            Command c = Command.valueOf(line.split(" ")[0]);
            if(line.matches(c.regex)){

            } else {
                log.debug("Syntax Error on "+lineNumber+"!");
            }
        } else {
            log.debug("Invalid Command Found on line"+lineNumber+"!");
        }
        lineNumber++;
        return "";
    }


    public static void main(String[] args){
        /*
         * Checking Command line arguments to look for debugging information and filename info
         */
        if(args.length != 1 && args.length != 2){
            System.err.println("Requires command line input!");
            System.err.println("Proper usage : java site.projectname.nicasm.Assembler [-debug] filename ");
            System.exit(-1);
        } else if (args.length == 2) {
            if(!args[0].equals("-debug")){
                System.err.println("Invalid Syntax!");
                System.err.println("Proper usage : java site.projectname.nicasm.Assembler [-debug] filename ");
                System.exit(-1);
            }
            Logger.debug = true;
            Assembler.fileName = args[1];
        } else {
            if(args[0].equals("-debug") || args[0].equals("-h") || args[0].equals("--help")){
                System.err.println("Invalid Syntax!");
                System.err.println("Proper usage : java site.projectname.nicasm.Assembler [-debug] filename ");
                System.exit(-1);
            }
            Logger.debug = false;
            Assembler.fileName = args[0];
        }

        /*
         * Report start, create logs
         */
        System.out.println("Assembler Starting!");
        Assembler.log = Logger.getLog("Assembler",new SimpleDateFormat(""),Logger.debug);
        Assembler.log.write("Assembling file " + fileName);
        /*
         * If file does not end with .nic, warn that .nic file is expected, but continue
         */
        if(!fileName.endsWith(".nic")){
            log.debug("Warning: Files should end with '.nic'!");
        }

        /*
         * Create Scanner Object
         */
        File f = null;
        try{
            f = new File(fileName);
            file = new Scanner(f);
        } catch(Exception e){
            log.writeError(e);
            System.err.println("File not found!");
            log.debug("Looking for file at:");
            log.debug(f.getAbsolutePath());
            System.exit(-1);
        }

    }
}
