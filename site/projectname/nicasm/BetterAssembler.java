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
    private static int bC = 15;

    private static String clean(String line){
        // Take everything before comments
        line = line.split(";")[0];
        // Condense White Space
        line = line.replaceAll("[\\s\\t]+"," ");

        // Remove extra whitespace
        while(line.startsWith(" "))
            line = line.substring(1);
        while(line.endsWith(" ") || line.endsWith("\n"))
            line = line.substring(0,line.length()-2);
        return line;
    }

    private static String firstPass(String line) throws SyntaxErrorException {
        line = clean(line);
        if(line.equals("")){

            return line;
        }
        String out = "";
        switch(line.charAt(0)){
            case '$':
                // Variable
                if(line.split(" ")[0].matches(REGEX.VARIABLE.toString()))
                    out = parseVariable(line);
                else{
                    throw new SyntaxErrorException(line,REGEX.VARIABLE,lineNumber,BetterAssembler.log);
                }
                break;
            case '*':
                // Label
                if(line.split(" ")[0].matches(REGEX.LABEL.toString()))
                    out = parseLabel(line);
                else{

                    throw new SyntaxErrorException(line,REGEX.LABEL,lineNumber,BetterAssembler.log);
                }
                break;
            default:
                out = line;
                break;
        }

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
        log.debug("Parsing line " + lineNumber);log.indentLevel++;
        log.debug("Contents:");
        log.debug(line,1);
        char[] out = {'-','-','-','-','-','-','-','-','-','-','-','-','-','-','-','-'};
        String[] parts = line.replaceAll("([\\s]+)|"+REGEX.SPACE," ").split(" ");
        ret: if(Command.contains(parts[0])){
            Command com = Command.get(parts[0]);
            bC = 15;
            int rC = 1;
            if(line.matches(com.regex)){
                out = fillBits(com.firstFour,out);
                // Special case commands go here
                switch(com){
                    case FILL:
                        out = fillBits(convertImm(parts[1],16,line),out);
                        break ret;
                    case BLK:

                        int value = Integer.parseInt(Numbers.convert(2,10,false,convertImm(parts[1],16,line),16).substring(1));
                        for(int i=value-1;i>=0;i--){
                            if(i == 0){
                                log.debug("BLK done Recursing");
                                return secondPass(".FILL x0000");
                            } else {
                                log.debug("BLK Recursing");
                                return secondPass(".FILL x0000") + " " +secondPass(".BLK #"+i) + " ";
                            }
                        }

                    case BR: // Can appear in 8 forms
                        parts[0] = parts[0].toUpperCase();
                        if(parts[0].length() == 2){
                            out = fillBits("111",out);
                        } else {
                            if(parts[0].contains("N"))
                                out = fillBits("1",out);
                            else
                                out = fillBits("0",out);
                            if(parts[0].contains("Z"))
                                out = fillBits("1",out);
                            else
                                out = fillBits("0",out);
                            if(parts[0].contains("P"))
                                out = fillBits("1",out);
                            else
                                out = fillBits("0",out);
                        }
                        out = fillBits(compOffset(parts[1],line,9),out);

                        break ret;
                    case ADD: case AND: // Can have either a register or an immediate value
                        out = fillBits(convertRegister(parts[1]),out);
                        out = fillBits(convertRegister(parts[2]),out);
                        if(parts[3].matches(REGEX.IMM5.toString())){
                            out = fillBits("1",out);
                            out = fillBits(convertImm(parts[3],5,line),out);
                        } else if(parts[3].matches(REGEX.REGISTER.toString())){
                            out = fillBits("000",out);
                            out = fillBits(convertRegister(parts[3]),out);
                        } else {
                            throw new SyntaxErrorException(clean(line),com.regex,lineNumber,log);
                        }
                        break ret;
                    default:
                        break;
                }
                String[] syntax = com.syntax.split(" ");
                for(String s: syntax){
                    if(s.contains("R")){
                        log.debug("Adding Register["+ rC +"]");
                        out = fillBits(convertRegister(parts[rC++]),out);
                    } else if(s.contains("X")){
                        if(parts[rC].contains("*") || parts[rC].contains("$")){
                            out = fillBits(compOffset(parts[rC++],line,s.length()),out);
                        }
                    }
                }
            } else {

                throw new SyntaxErrorException(clean(line),com.regex,lineNumber,log);
            }
        } else {

            throw new SyntaxErrorException("Invalid command on line " + lineNumber+"\n\t"+line,log);
        }


        log.debug("Line " + lineNumber + " compiled to ");
        log.debug(new String(out),1);
        log.indentLevel--;
        return new String(out);
    }
    private static String compOffset(String in, String line, int n) throws SyntaxErrorException {
        int offSet = 1;
        int address=0;
        if(in.startsWith("*")){
            address = labels.get(in)-lineNumber;
        } else if(in.startsWith("$")){
            address = variables.get(in)-lineNumber;
        } else{
            throw new SyntaxErrorException("Invalid Label on line "+lineNumber+"\n\t"+line,log);
        }
        offSet += address;
        log.debug("Offset from " + in + "("+address+") is " + offSet + " lines");
        return Numbers.convert(10,2,true,offSet+"",n);
    }


    private static String convertImm(String in,int len,String line) throws SyntaxErrorException{
        if(!(in.startsWith("#") || in.startsWith("x")))
            throw new SyntaxErrorException("Invalid Immediate Value on " + lineNumber+"\n\t"+line,log);
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

    private static char[] fillBits(String bits, char[] in){
        if(bits.replace("X","").equals(""))
            return in;
        int start = bC;
        int end = start-bits.length();
        log.debug("Filling bits ["+start+":"+(end+1)+"] with "+bits);
        for(int i=start;i>end;i--){
            if(bits.charAt(start-i) != 'X'){
                in[15-i] = bits.charAt(start-i);
                bC--;
            }
        }
        log.debug(Arrays.toString(in),1);
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
            lineNumber++;
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
            lineNumber++;
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
