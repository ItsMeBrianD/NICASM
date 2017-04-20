package site.projectname.nicasm;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import site.projectname.err.SyntaxErrorException;
import site.projectname.util.Logger;
import site.projectname.util.Numbers;


public class BetterAssembler{
    /**
     * File to be Assembled
     */
    public String fileName;

    private Logger log;
    private Scanner inFile;
    private PrintWriter outFile;

    private HashMap<String,Integer> labels = new HashMap<String,Integer>();
    private HashMap<String,Integer> variables = new HashMap<String,Integer>();
    private ArrayList<String> errors = new ArrayList<String>();

    private int lineNumber = 1;
    private int bC = 15;
    /**
     * Creates a BetterAssembler based on command line (or given) arguments
     * @param   args    Arguments used to construct object
     */
    public BetterAssembler(String[] args){
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
            this.fileName = args[1];
        } else {
            if(args[0].equals("-debug") || args[0].equals("-h") || args[0].equals("--help")){
                System.err.println("Invalid Syntax!");
                System.err.println("Proper usage : java site.projectname.nicasm.Assembler [-debug] filename ");
                System.exit(-1);
            }
            Logger.debugGlobal = false;
            this.fileName = args[0];
        }
        initFiles();
    }

    private String clean(String line){
        // Take everything before comments
        line = line.split(";")[0];
        // Condense White Space
        line = line.replaceAll("[\\s\\t]+"," ");

        // Remove extra whitespace
        while(line.startsWith(" "))
            line = line.substring(1);
        while(line.endsWith(" ") || line.endsWith("\n"))
            line = line.substring(0,line.length()-1);
        return line;
    }

    private String firstPass(String line) throws SyntaxErrorException {
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
                    throw new SyntaxErrorException(line,REGEX.VARIABLE.toString(),lineNumber,this.log);
                }
                break;
            case '*':
                // Label
                if(line.split(" ")[0].matches(REGEX.LABEL.toString()))
                    out = parseLabel(line);
                else{
                    throw new SyntaxErrorException(line,REGEX.LABEL.toString(),lineNumber,this.log);
                }
                break;
            default:
                out = line;
                break;
        }

        return out;
    }

    private String parseVariable(String line){
        log.debug("Parsing variable on line " + lineNumber);
        String v = "|-"+lineNumber+":";
        log.debug(v + Logger.spacer(v,9) + line);
        String[] parts = line.replaceAll("[\\s]+"," ").split(" ");
        variables.put(parts[0],lineNumber);
        String out = parts[1] + " " + parts[2];
        return out;
    }
    private String parseLabel(String line){
        log.debug("Parsing label on line " + lineNumber);
        String v = "|-"+lineNumber+":";
        log.debug(v + Logger.spacer(v,9) + line);
        String[] parts = line.replaceAll("[\\s]+"," ").split(" ");
        labels.put(parts[0],lineNumber);
        String out = "";
        for(int i=1;i<parts.length;i++)
            out += parts[i] + " ";
        if(!out.equals(""))
          out = out.substring(0,out.length()-1); // Removes extra space
        return out;
    }
    private String secondPass(final String line) throws SyntaxErrorException {
        if(line.equals(""))
            return "";
        log.debug("");
        log.debug("Parsing line " + lineNumber);log.indent();
        log.debug("Contents:");
        log.debug(line,1);
        char[] out = {'-','-','-','-','-','-','-','-','-','-','-','-','-','-','-','-'};
        String[] parts = line.replaceAll("([\\s]+)|"+REGEX.SPACE," ").split(" ");
        ret: if(Command.contains(parts[0])){
            Command com = Command.get(parts[0]);
            bC = 15;
            int rC = 1;
            if(line.matches(com.regex)){
                log.debug("Adding Command ID");
                out = fillBits(com.firstFour,out);
                // Commands that must be handled entirely differently go here
                switch(com){
                    case FILL:
                        out = fillBits(convertImm(parts[1],16,line),out);
                        break ret;
                    case BLK:

                        int value = Integer.parseInt(Numbers.convert(2,10,false,convertImm(parts[1],16,line),16).substring(1));
                        for(int i=value-1;i>=0;i--){
                            if(i == 0){
                                log.debug("BLK done Recursing");
                                log.unindent();
                                return secondPass(".FILL x0000");
                            } else {
                                log.debug("BLK Recursing");
                                log.unindent();
                                return secondPass(".FILL x0000") + " " +secondPass(".BLK #"+i);
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
                        out = fillBits(compOffset(parts[1],9,line),out);

                        break ret;
                    default:
                        break;
                }
                String[] syntax = com.syntax.split(" ");
                for(String s: syntax){
                    if(s.contains("R")){
                        log.debug("Adding Register["+ rC +"] (" + parts[rC] + ")");
                        out = fillBits(convertReg(parts[rC++]),out);
                    } else if(s.contains("X")){
                        if(parts[rC].contains("*") || parts[rC].contains("$")){
                            out = fillBits(compOffset(parts[rC++],s.length(),line),out);
                        } else if(parts[rC].contains("#") || parts[rC].contains("x")){
                            if(!(com.value.equals("AND") || com.value.equals("ADD")))
                            out = fillBits(convertImm(parts[rC++],s.length(),line),out);
                        }
                    } else {
                        out = fillBits(s,out);
                    }
                }
                switch(com){
                    case ADD: case AND: // Can have either a register or an immediate value
                        if(parts[3].matches(REGEX.IMM5.toString())){
                            log.debug("Adding immediate|reg marker");
                            out = fillBits("1",out);
                            out = fillBits(convertImm(parts[3],5,line),out);
                        } else if(parts[3].matches(REGEX.REGISTER.toString())){
                            log.debug("Adding immediate|reg marker");
                            out = fillBits("0",out);
                            log.debug("Adding buffer");
                            out = fillBits("00",out);
                            log.debug("Adding Register["+ rC +"] (" + parts[rC] + ")");
                            out = fillBits(convertReg(parts[3]),out);
                        } else {
                            //log.unindent();
                            throw new SyntaxErrorException(clean(line),com.regex,lineNumber,log);
                        }
                        break ret;
                }
            } else {
                //log.unindent();
                throw new SyntaxErrorException(clean(line),com.regex,lineNumber,log);
            }
        } else {
            //log.unindent();
            throw new SyntaxErrorException("Invalid command on line " + lineNumber+"\n\t"+line,log);
        }

        String realOut = new String(out);

        if(realOut.contains("-"))
            throw new SyntaxErrorException("Command on line " + lineNumber +" has unset bits!\n\t"+line,log);

        log.debug("Line " + lineNumber + " compiled to ");
        log.debug("(b)"+realOut,1);
        realOut = Numbers.convert(2,16,false,realOut).substring(1);
        log.debug("(x)"+realOut,1);
        log.unindent();

        return realOut;
    }
    private String compOffset(String in, int n,String line) throws SyntaxErrorException {
        int offSet =0;
        int address=0;
        if(in.startsWith("*")){
            address = labels.get(in)-1;
        } else if(in.startsWith("$")){
            address = variables.get(in);
        } else{
            throw new SyntaxErrorException("Invalid Label on line "+lineNumber+"\n\t"+line,log);
        }
        offSet += address-lineNumber;
        log.debug("Offset from " + in + "("+address+") is " + offSet + " lines");
        return Numbers.convert(10,2,true,offSet+"",n);
    }
    private String convertImm(String in, int len, String line) throws SyntaxErrorException{
        if(!(in.startsWith("#") || in.startsWith("x")))
            throw new SyntaxErrorException("Invalid Immediate Value on " + lineNumber+"\n\t"+line,log);
        if(in.startsWith("#"))
            return Numbers.convert(10,2,true,in,len);
        if(in.startsWith("x"))
            return Numbers.convert(16,2,true,in,len);
        return "";
    }
    private String convertReg(String in) throws SyntaxErrorException{
        in = in.substring(1);
        return Numbers.convert(10,2,false,in,3);
    }
    private char[] fillBits(String bits, char[] in){
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
        log.debug("");
        return in;
    }
    private boolean checkErrors(){
        if(!errors.isEmpty()){
            System.err.println(errors.size() + " Error(s) found:");
            for(String s: errors){
                System.err.println(s);
                for(String s2: s.split("\n"))
                    log.write(s2);
            }
            return true;
        } else {
            System.out.println("\t No Errors found.");
            return false;
        }
    }
    private void initFiles(){
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

    /**
     * Assembles a file from Assembly to Hex.
     * @param   file    File to be assembled
     */
    public void assemble(String file){
        this.log = Logger.getLog("Assembler", Logger.debugGlobal);
        this.log.write("Assembling file " + fileName);
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
        if(checkErrors())
          return;
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
        if(checkErrors())
          return;

        outFile.close();
    }

    public static void main(String[] args){
        BetterAssembler a = new BetterAssembler(new String[]{"-debug","test1.nic"});
        a.assemble("test1.nic");
        a = new BetterAssembler(new String[]{"-debug","test2.nic"});
        a.assemble("test2.nic");

        BetterAssembler b = new BetterAssembler(new String[]{"test1.nic"});
        b.assemble("test1.nic");
        b = new BetterAssembler(new String[]{"test2.nic"});
        b.assemble("test2.nic");
    }

}
