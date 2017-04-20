package site.projectname.nicasm;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import site.projectname.util.Logger;
import site.projectname.util.Numbers;

public class BetterAssembler{
    public static String fileName;

    private static Logger log;
    private static Scanner inFile;
    private static PrintWriter outFile;

    private static HashMap<String,Integer> labels = new HashMap<String,Integer>();
    private static HashMap<String,Integer> variables = new HashMap<String,Integer>();
    private static ArrayList<String> errors = new ArrayList<String>();

    private static int lineNumber = 1;

    private static String clean(String line){
        // Take everything before comments
        line = line.split(";")[0];
        // Remove extra whitespace
        while(line.startsWith(" "))
            line = line.substring(1);
        while(line.endsWith(" ") || line.endsWith("\n"))
            line = line.substring(0,line.length()-2);
        return line;
    }

    private static String firstPass(String line) throws SyntaxErrorException {
        line = clean(line);
        String out = "";
        switch(line.charAt(0)){
            case '$':
                // Variable
                if(line.matches(REGEX.VARIABLE.toString()))
                    out = parseVariable(line);
                else
                    throw new SyntaxErrorException(line,REGEX.VARIABLE,lineNumber,BetterAssembler.log);
                break;
            case '*':
                // Label
                if(line.split(" ")[0].matches(REGEX.LABEL.toString()))
                    out = parseLabel(line);
                else
                    throw new SyntaxErrorException(line,REGEX.LABEL,lineNumber,BetterAssembler.log);
                break;
            default:
                out = line;
                break;
        }
        lineNumber++;
        return out;
    }


    private static String parseVariable(String line){
        log.debug("Parsing variable on line " + lineNumber);
        String v = "|-"+lineNumber+":";
        log.debug(v + Logger.spacer(v,9) + line);
        String[] parts = line.replaceAll("[\\s]+"," ").split(" ");
        variables.put(parts[0],lineNumber);
        String out = parts[1] + " " + parts[2];
        return out;
    }
    private static String parseLabel(String line){
        log.debug("Parsing label on line " + lineNumber);
        String v = "|-"+lineNumber+":";
        log.debug(v + Logger.spacer(v,9) + line);
        String[] parts = line.replaceAll("[\\s]+"," ").split(" ");
        labels.put(parts[0],lineNumber);
        String out = "";
        for(int i=1;i<parts.length;i++)
            out += parts[i] + " ";
        out = out.substring(0,out.length()-1); // Removes extra space
        return out;
    }


    private static String secondPass(final String line) throws SyntaxErrorException {
        if(line.equals(""))
            return "";
        log.debug("");
        log.debug("Parsing line " + lineNumber);
        log.debug("|-\t"+line);
        char[] out = {'-','-','-','-','-','-','-','-','-','-','-','-','-','-','-','-'};
        String[] parts = line.replaceAll("([\\s]+)|"+REGEX.SPACE," ").split(" ");
        log.debug("|-\t"+Arrays.toString(parts));
        if(Command.contains(parts[0])){
            Command com = Command.get(parts[0]);
            switch(com){
                case BR:
                    if(parts[0].length() == 2){
                        out = fillBits(11,8,"111",out);
                    } else {
                        if(parts[0].contains("N"))
                            out = fillBits(11,10,"1",out);
                        else
                            out = fillBits(11,10,"0",out);
                        if(parts[0].contains("Z"))
                            out = fillBits(10,9,"1",out);
                        else
                            out = fillBits(10,9,"0",out);
                        if(parts[0].contains("P"))
                            out = fillBits(9,8,"1",out);
                        else
                            out = fillBits(9,8,"0",out);
                    }
                    out = fillBits(8,0,compOffset(parts[1]),out);
                    break;

                default:
                    break;
            }
            int bC = 15;
            int rC = 1;
            if(line.matches(com.regex)){
                out = fillBits(bC,bC-4,com.firstFour,out);
                bC -= 4;
                String[] syntax = com.syntax.split(" ");
                for(String s: syntax){
                    if(s.contains("R")){
                        log.debug("|-\tAdding Register["+ rC +"]!");
                        out = fillBits(bC,bC-3,convertRegister(parts[rC++]),out);
                        bC -= 3;
                    } else if(s.contains("X")){
                        if(parts[rC].contains("*") || parts[rC].contains("$")){
                            out = fillBits(bC,bC-parts[rC].length(),convertImm(parts[rC++],s.length(),line),out);
                        }
                        bC -= s.length();
                    }
                }
            } else {
                throw new SyntaxErrorException(line,com.regex,lineNumber,log);
            }
        } else {
            throw new SyntaxErrorException("Invalid command on line " + lineNumber+"\n\t"+line,log);
        }
        log.debug("|-\tLine " + lineNumber + " compiled from ");
        log.debug("|-\t\t"+line);
        log.debug("|-\t  TO");
        log.debug("|-\t\t"+new String(out));
        log.debug("|-\tOriginal Line:"+lineNumber+": " + line);
        lineNumber++;
        return new String(out);
    }
    private static String compOffset(String in) throws SyntaxErrorException {
        int offSet;
    }


    private static String convertImm(String in,int len,String line) throws SyntaxErrorException{
        if(!in.startsWith("#") || !in.startsWith("x"))
            throw new SyntaxErrorException("Invalid Immediate Value " + lineNumber+"\n\t"+line,log);
        if(in.startsWith("#"))
            return Numbers.convert(10,2,false,in,len);
        if(in.startsWith("x"))
            return Numbers.convert(16,2,false,in,len);
        return "";
    }

    private static String convertRegister(String in){
        in = in.substring(1);
        return Numbers.convert(10,2,false,in,3);
    }

    private static char[] fillBits(int start, int end, String bits, char[] in){
        log.debug("|-\tFilling bits ["+start+":"+end+"] with "+bits);
        int length = start-end;
        for(int i=start;i>end;i--){
            if(bits.charAt(start-i) != 'X')
                in[15-i] = bits.charAt(start-i);
        }
        log.debug("|-\t"+Arrays.toString(in));
        return in;
    }

    /**
     * Assembles a file from Assembly to Hex.
     * @param   file    File to be assembled
     */
    public static void assemble(String file){
        BetterAssembler.log = Logger.getLog("Assembler", Logger.debugGlobal);
        BetterAssembler.log.write("Assembling file " + fileName);
        if(!fileName.endsWith(".nic")){
            log.debug("Warning: Files should end with '.nic'!");
        }
        initFiles();

        String s = "";
        while(inFile.hasNextLine()){
            try{
                s += firstPass(inFile.nextLine()) + "\n";
            } catch(SyntaxErrorException e){
                errors.add(e.getMessage());
            }
        }

        System.out.println("First Pass Complete.");
        checkErrors();
        log.debug("Variables found: "+ variables.keySet());
        log.debug("Labels found: "+ labels.keySet());

        try{
            inFile = new Scanner(s);
        } catch(Exception e){}
        lineNumber = 1;

        while(inFile.hasNextLine()){
            try{
                String l = inFile.nextLine();

                String line = secondPass(l);

                if(line.length() > 0){
                    outFile.print(line+" ");
                }
            } catch(SyntaxErrorException e){
                errors.add(e.getMessage());
            }
        }
        System.out.println("Second Pass Complete.");
        checkErrors();

        outFile.close();
    }

    public static void main(String[] args){
        checkArgs(args);
        assemble(fileName);
    }

    private static void checkErrors(){
        if(!errors.isEmpty()){
            System.err.println(errors.size() + " Error(s) found:");
            for(String s: errors){
                System.err.println(s);
                for(String s2: s.split("\n"))
                    log.write(s2);
            }
            System.exit(-1);
        } else {
            System.out.println("\t No Errors found.");
        }
    }
    private static void checkArgs(String[] args){
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
            System.out.println("Debugging enabled!");
            Logger.debugGlobal = true;
            BetterAssembler.fileName = args[1];
        } else {
            if(args[0].equals("-debug") || args[0].equals("-h") || args[0].equals("--help")){
                System.err.println("Invalid Syntax!");
                System.err.println("Proper usage : java site.projectname.nicasm.Assembler [-debug] filename ");
                System.exit(-1);
            }
            Logger.debugGlobal = false;
            BetterAssembler.fileName = args[0];
        }
    }
    private static void initFiles(){
        File iF = null;
        File oF = null;
        try{
            iF = new File(fileName);
            inFile = new Scanner(iF);
        } catch(Exception e){
            log.writeError(e);
            System.err.println("File not Found!");
            log.debug("Absolute filepath for argument: ");
            log.debug(iF.getAbsolutePath());
            System.exit(-1);
        }
        try{
            oF = new File(fileName.split("[.]")[0] + ".nicp");
            outFile = new PrintWriter(oF);
        } catch(Exception e){
            log.writeError(e);
            System.err.println("Error creating output file!");
            log.debug("Attempting to output to :");
            log.debug(iF.getAbsolutePath());
            System.exit(-1);
        }
    }

}
