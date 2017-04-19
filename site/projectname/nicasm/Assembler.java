package site.projectname.nicasm;

import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.Arrays;
import java.io.File;
import java.io.PrintWriter;


import site.projectname.util.Logger;
import site.projectname.util.Numbers;
/**
 * Assembles .nic inFiles into usable hex-format for nicpc
 *
 * @author      Brian Donald
 * @version     0.0
 * @since       2017-4-18
 */
public class Assembler{

    public static String fileName;
    public static Logger log;
    private static Scanner inFile;
    private static PrintWriter outFile;
    private static int lineNumber = 1;
    /**
     * All valid Assembly Commands for nic
     */
    public enum Command {
        ADD("ADD", "(ADD)[ ]+" + regRegEx + spaRegEx + regRegEx + spaRegEx + "(" + regRegEx + "|" + immRegEx + ")", "0001", "DR SR1 XXX SR2"),
        AND("AND", "(AND)[ ]+" + regRegEx + spaRegEx + regRegEx + spaRegEx + "(" + regRegEx + "|" + immRegEx + ")", "0101", "DR SR1 XXX SR2"),
        BR("BR","(BR)([Z]|[N]|[P]){0,3}","0000","XXX XXXXXXXXX"),
        JMP("JMP","(JMP)[ ]+" + regRegEx, "1100", "000 BR 000000"),
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
        public final String firstFour;
        public final String syntax;
        public final String value;
        Command(String value,String regex,String firstFour,String syntax){
            this.regex = regex;
            this.firstFour = firstFour;
            this.value=value;
            this.syntax = syntax;
        } Command(){this.regex = "";this.firstFour="";this.value="";this.syntax="";}

        public static boolean contains(String value){
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

    /**
     * Regex for Register inputs, i.e. R0-R7
     */
    public final static String  regRegEx = "(R[0-7])";

    public final static String  spaRegEx = "([,][ ]*)";

    public final static String  hexRegEx = "([x]([0,1]([0-9]|[A-F])))";

    public final static String  decRegEx = "([#](([0-2]{0,1}[0-9])|([3][0-1])))";

    public final static String  immRegEx = hexRegEx + "|" + decRegEx;

    public static String convertLine(String line) {

        StringBuilder outBinary = new StringBuilder("----------------");


        log.debug("Line\t\t: " + line);
        line = line.split(";")[0]; // Disregard Comments
        while(line.endsWith(" "))
            line = line.substring(0,line.length()-1);
        log.debug("Line for parsing\t: " + line);
        if(line.equals(""))
            return "";
        log.debug("Command\t\t: \"" + line.split(" ")[0] + "\"");

        if(Command.contains(line.split(" ")[0])){
            Command com = Command.valueOf(line.split(" ")[0]);
            if(line.matches(com.regex)){
                String[] parts = line.split("[ ]|"+ spaRegEx);

                log.debug("Line split into: " + Arrays.toString(parts));
                for(int bit=15;bit>=12;bit--)
                    outBinary.setCharAt(bit,com.firstFour.charAt(15-bit));

                    String[] syntax = com.syntax.split(" ");
                    int bitCounter = 11;
                    int regCounter = 1;
                    for(String s: syntax){
                        if(s.contains("R") && parts[regCounter].contains("R")){
                            int r = Integer.parseInt(parts[regCounter++].substring(1));
                            String bits = Numbers.decToBin(r,3);
                            for(int bit=bitCounter;bit>bitCounter-3;bit--){
                                log.debug("Setting bit " + bit + " with value in bits[" + (bitCounter-bit) + "]");
                                outBinary.setCharAt(bit,bits.charAt(bitCounter-bit));
                            }
                            bitCounter -= 3;
                        } else {
                                for(char c: s.toCharArray()) {
                                if(c == '0' || c == '1')
                                    outBinary.setCharAt(bitCounter--,c);
                                else
                                    bitCounter--;
                            }
                        }
                    }

                //AND & ADD have 2 variants, check which is applicable
                if(parts[0].equals("AND") || parts[0].equals("ADD")){
                    if(parts[3].matches(immRegEx)){
                        log.debug("Command contains an immediate value!");
                        // Has Immediate Value
                        outBinary.setCharAt(5,'1');
                        String binary = "";
                        log.debug("Immediate value is " + parts[3]);
                        if(parts[3].matches(hexRegEx)){
                            binary = Numbers.hexToBin(parts[3],5);
                        } else if (parts[3].matches(decRegEx)){
                            binary = Numbers.decToBin(Integer.parseInt(parts[3].substring(1)),5);
                        } else {
                            log.debug("Invalid immediate value given!");
                        }
                        for(int bit=4; bit>=0; bit--)
                            outBinary.setCharAt(4-bit,binary.charAt(bit));
                    }
                    else{
                        log.debug("Command does not contain an immediate value!");
                        // Has Register Value
                        outBinary.setCharAt(5,'0');
                        outBinary.setCharAt(4,'0');
                        outBinary.setCharAt(3,'0');
                    }
                } else {

                }
            } else {
                log.debug("Syntax Error on line "+lineNumber+"!");
            }
        } else {
            log.debug("Invalid Command Found on line "+lineNumber+"!");
        }
        lineNumber++;
        String out = "";
        for(char c: outBinary.toString().toCharArray()){
            out = c + out;
        }
        return Numbers.binToHex(out);
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
            Logger.debugGlobal = true;
            Assembler.fileName = args[1];
        } else {
            if(args[0].equals("-debug") || args[0].equals("-h") || args[0].equals("--help")){
                System.err.println("Invalid Syntax!");
                System.err.println("Proper usage : java site.projectname.nicasm.Assembler [-debug] filename ");
                System.exit(-1);
            }
            Logger.debugGlobal = false;
            Assembler.fileName = args[0];
        }

        /*
         * Report start, create logs
         */
        System.out.println("Assembler Starting!");
        Assembler.log = Logger.getLog("Assembler",new SimpleDateFormat(""),Logger.debugGlobal);
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
        File iF = null;
        File oF = null;
        try{
            iF = new File(fileName);
            inFile = new Scanner(iF);
        } catch(Exception e){
            log.writeError(e);
            System.err.println("File not found!");
            log.debug("Looking for inFile at:");
            log.debug(iF.getAbsolutePath());
            System.exit(-1);
        }
        try{
            oF = new File(fileName.split("[.]")[0] + ".nicp");
            outFile = new PrintWriter(oF);
        } catch(Exception e){log.writeError(e);}
        Numbers.init();
        while(inFile.hasNextLine()){
            String line = convertLine(inFile.nextLine());
            if(line.length() > 0){
                log.debug(line+"");
                outFile.print(line+" ");
            }
        }
        outFile.flush();
        System.out.println("Assembly Complete! No Errors found!");
    }
}
