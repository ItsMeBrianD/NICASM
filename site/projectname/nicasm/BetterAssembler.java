package site.projectname.nicasm;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import site.projectname.err.SyntaxErrorException;
import site.projectname.lang.Syntax;
import site.projectname.util.Logger;
import site.projectname.util.Numbers;

/**
 * Assembles/Compiles .nic files into .nicp files for the NIC (NIC Interactive Computer)
 * @author	Brian Donald
 * @version	1.0
 * @since	2017-4-22
 */
public class BetterAssembler {
	/**
	 * File to be Assembled
	 */
	public String fileName;

	private Logger log;
	private Scanner inFile;
	private PrintWriter outFile;

	private HashMap<String, Integer> labels = new HashMap<String, Integer>();
	private HashMap<String, Integer> variables = new HashMap<String, Integer>();
	private ArrayList<String> errors = new ArrayList<String>();
	private final Enum<? extends Syntax> syntax = NICSyntax.HELPER;


	private int lineAddr = 0;
    private int lineNum = 1;
	private int bC = 15;
	private int mainOffset = 0;

	/**
	 * Creates a BetterAssembler based on command line (or given) arguments
	 *
	 * @param args	Arguments used to construct object
	 */
	public BetterAssembler(String[] args){
		if (args.length != 1 && args.length != 2){
			System.err.println("Requires command line input!");
			System.err.println("Proper usage : java site.projectname.nicasm.Assembler [-debug] filename ");
			System.exit(-1);
		} else if (args.length == 2){
			if (!args[0].equals("-debug")){
				System.err.println("Invalid Syntax!");
				System.err.println("Proper usage : java site.projectname.nicasm.Assembler [-debug] filename ");
				System.exit(-1);
			}
			System.out.println("Debugging enabled!");
			Logger.debugGlobal = true;
			this.fileName = args[1];
		} else{
			if (args[0].equals("-debug") || args[0].equals("-h") || args[0].equals("--help")){
				System.err.println("Invalid Syntax!");
				System.err.println("Proper usage : java site.projectname.nicasm.Assembler [-debug] filename ");
				System.exit(-1);
			}
			Logger.debugGlobal = false;
			this.fileName = args[0];
		}
		this.log = Logger.getLog("Assembler", Logger.debugGlobal);
		log.debug("Given Arguments:");
		for(String s: args)
			log.debug(s,1);
		initFiles();
	}

	/**
	 * Cleans a line to ensure it won't fail regex checking due to white space or comments
	 * @param	line	Line to be cleaned
	 * @return			Line without comments,extra white space, and preceeding or following white space
	 */
	private String clean(String line){
		// Take everything before comments
		line = line.split(";")[0];
		// Condense White Space
		line = line.replaceAll("[\\s\\t]+", " ");

		// Remove extra whitespace
		while (line.startsWith(" "))
			line = line.substring(1);
		while (line.endsWith(" ") || line.endsWith("\n"))
			line = line.substring(0, line.length() - 1);
		return line;
	}
	/**
	 * Searches through line for labels, variables, and the .MAIN command, calculating the addresses for these lines respectively
	 * @param	line					Line to be checked for labels, variables, and .MAIN
	 * @return							Line stripped to only the command, all extranious information (Comments, Labels, Variables, .MAIN) is stripped or replaced with .FILL x0000
	 * @throws	SyntaxErrorException	Thrown when improper syntax is found
	 */
	private String firstPass(String line) throws SyntaxErrorException{
		line = clean(line);
		if (line.equals("")){
			return line;
		}
		String out = "";
		switch (line.charAt(0)){
    		case '$':
    			// Variable
    			if (line.split(" ")[0].matches(NICSyntax.VARIABLE.toString()))
    				out = parseVariable(line);
    			else{
    				throw new SyntaxErrorException(line, NICSyntax.VARIABLE.toString(), lineNum, this.syntax);
    			}
    			break;
    		case '*':
    			// Label
    			if(line.split(" ")[0].matches(NICSyntax.LABEL.toString())){
    				out = parseLabel(line);
                    if(out.equals("")){
                        out += ".FILL x0000";
                    }
                }
    			else{
    				throw new SyntaxErrorException(line, NICSyntax.LABEL.toString(), lineNum, this.syntax);
    			}
    			break;
    		default:
    			out = line;
    			break;
		}
		switch(line.split(" ")[0]){
			case ".MAIN":
				log.debug(".MAIN found at address " + Numbers.convert(10,16,false,lineAddr+"",4));
				mainOffset = lineAddr;
				break;
			case ".BLK":
				int offset = Integer.parseInt(Numbers.convert(2,10,false,convertImm(line.split(" ")[1],16,line,Command.BLK.regex)).substring(1)) - 1;
				log.debug("Incrementing lineAddr by " + offset + " due to .BLK");
				lineAddr+=offset;
				break;
		}
        lineAddr++;
		return out;
	}

	/**
	 * Parses a given variable ($[A-Z]+) by removing the variable, adding it to the variables map, and returning the following command
	 * @param	line					Line containing a variable
	 * @return							Line without variable
	 */
	private String parseVariable(String line) {
		log.debug("Parsing variable on line " + lineNum);
		String v = lineAddr + ":";
		log.debug(v + Logger.spacer(v, 9) + line,1);
		String[] parts = line.replaceAll("[\\s]+", " ").split(" ");
        variables.put(parts[0], lineAddr);
        if(parts[1].equals(".BLK")){
            if(parts[2].startsWith("x"))
                lineAddr += Integer.parseInt(Numbers.convert(16,10,false,parts[2]).substring(1))-1;
            else
                lineAddr += Integer.parseInt(parts[2].substring(1))-1;
        }
		return parts[1] + " " + parts[2];
	}

	/**
	 * Parses a given label (*[A-Z]+) by removing the label, adding it to the labels map, and returning the following command
	 * @param	line	Line containing a label
	 * @return			Line without label
	 */
	private String parseLabel(String line){
		log.debug("Parsing label on line " + lineNum);
		String v = lineAddr + ":";
		log.debug(v + Logger.spacer(v, 9) + line,1);
		String[] parts = line.replaceAll("[\\s]+", " ").split(" ");
		labels.put(parts[0], lineAddr);
		String out = "";
		for (int i = 1; i < parts.length; i++)
			out += parts[i] + " ";
		if (!out.equals(""))
			out = out.substring(0, out.length() - 1); // Removes extra space
		return out;
	}
	/**
	 * Converts a given line to hex code, verbose debugging output can be enabled via {@link site.projectname.util.Logger Logger}
	 * @param	line					Line to convert to hex code
	 * @return							Line as Hex Code
	 * @throws	SyntaxErrorException	Thrown when line has invalid syntax according to {@link site.projectname.nicasm.NICSyntax NICSyntax}
	 */
	private String secondPass(final String line) throws SyntaxErrorException{
		if (line.equals(""))
			return "";
        log.debug("");
        log.debug("Parsing command on line " + lineNum);
        log.indent();
		char[] out = { '-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-' };
		String[] parts = line.replaceAll("([\\s]+)|" + NICSyntax.SPACE, " ").split(" ");
        if(Shorthand.contains(parts[0])){
            log.debug("");
            log.debugSpacer();
			log.debug("");
            log.debug("Command is shorthand, converting to basic commands");
            log.debug(line, 1);
            String[] newLines = Shorthand.get(parts[0]).convertSyntax(line,lineNum);
            String realOut = "";
            for(String s: newLines){
                realOut += secondPass(s) + " ";
            }
            log.debugSpacer();
			log.unindent();
            return clean(realOut);
        } else ret: if (Command.contains(parts[0])){
    		log.debug("Command:");
    		log.debug(line, 1);
			Command com = Command.get(parts[0]);
			bC = 15;
			int rC = 1;
			if (line.matches(com.regex)){
                log.debug("Adding Command ID");
				out = fillBits(com.firstFour, out);
				// Commands that must be handled entirely differently go here
				switch (com){
    				case FILL:
    					if(parts[1].matches(NICSyntax.IMM16.toString()))
    						out = fillBits(convertImm(parts[1], 16, line, Command.FILL.toString()), out);
    					else if(parts[1].matches(NICSyntax.CHAR.toString()))
    						out = fillBits(Numbers.convert(10,2,false,(int)parts[1].charAt(1)+"",16),out);
    					else
    						throw new SyntaxErrorException(clean(line), com.regex, lineNum, this.syntax);
    					break ret;
    				case BLK:
    					int value = Integer.parseInt(Numbers.convert(2, 10, false, convertImm(parts[1], 16, line, Command.BLK.regex), 16).substring(1));
              String realOut = "";
              for (int i = value; i > 0; i--){
                    log.unindent();
                    realOut += secondPass(".FILL x0000") + " ";
                    log.indent();
      				}
            	log.unindent();
              return clean(realOut);
	          case BR: // Can appear in 8 forms
	              parts[0] = parts[0].toUpperCase();
	              if (parts[0].length() == 2){
	                  out = fillBits("111", out);
	              } else{
	                  if (parts[0].contains("N"))
	                      out = fillBits("1", out);
	                  else
	                      out = fillBits("0", out);
	                  if (parts[0].contains("Z"))
	                      out = fillBits("1", out);
	                  else
	                      out = fillBits("0", out);
	                  if (parts[0].contains("P"))
	                      out = fillBits("1", out);
	                  else
	                      out = fillBits("0", out);
	              }
	              out = fillBits(compOffset(parts[1], 9, line), out);

	              break ret;
			default:
				break;
			case PRINT://2 forms
				 out = fillBits("1",out);
				 if(parts[1].matches(NICSyntax.CHAR.toString())){
					 out = fillBits("0001",out);
					 log.debug("Printing from character");
					 String charVal = Numbers.convert(10,2,false,(int)parts[1].charAt(1)+"",7);
					 out = fillBits(charVal,out);
				 }
				 else if (parts[1].matches(NICSyntax.REGISTER.toString())){
					 log.debug("Printing from Register[" + rC + "] (" + parts[rC] + ")");
					 out = fillBits(convertReg(parts[1]), out);
					 out = fillBits("00000000",out);
				 } else {
					 // log.unindent();
					 throw new SyntaxErrorException(clean(line), com.regex, lineNum, this.syntax);
				 }
				 break;
			}
				String[] syntax = com.syntax.split(" ");
				for (String s : syntax){
					if (s.contains("R") && parts[rC].contains("R") && !parts[rC].contains("'R'")){
						log.debug("Adding Register[" + rC + "] (" + parts[rC] + ")");
						out = fillBits(convertReg(parts[rC++]), out);
					} else if (s.contains("X")){
						if (parts[rC].contains("*") || parts[rC].contains("$")){
							out = fillBits(compOffset(parts[rC++], s.length(), line), out);
						} else if (parts[rC].contains("#") || parts[rC].contains("x")){
							if (!(com.value.equals("AND") || com.value.equals("ADD")))
								out = fillBits(convertImm(parts[rC++], s.length(), line, com.toString()), out);
						}
					} else{
						out = fillBits(s, out);
					}
				}
				switch (com){
    				case ADD:
    				case AND: // Can have either a register or an immediate value
    					if (parts[3].matches(NICSyntax.IMM5.toString())){
    						log.debug("Adding immediate|reg marker");
    						out = fillBits("1", out);
    						out = fillBits(convertImm(parts[3], 5, line, com.toString()), out);
    					} else if (parts[3].matches(NICSyntax.REGISTER.toString())){
    						log.debug("Adding immediate|reg marker");
    						out = fillBits("0", out);
    						log.debug("Adding buffer");
    						out = fillBits("00", out);
    						log.debug("Adding Register[" + rC + "] (" + parts[rC] + ")");
    						out = fillBits(convertReg(parts[3]), out);
    					} else{
    						// log.unindent();
    						throw new SyntaxErrorException(clean(line), com.regex, lineNum, this.syntax);
    					}
	                   break ret;

				}
			} else{
				// log.unindent();
				throw new SyntaxErrorException(clean(line), com.regex, lineNum, this.syntax);
			}
		} else{
			// log.unindent();
			throw new SyntaxErrorException(line, NICSyntax.COMMAND.toString(), lineNum, this.syntax);
		}

		String realOut = new String(out);

		if (realOut.contains("-"))
			throw new SyntaxErrorException("COMPILER ERROR:\n\tOutput contains unset bits!");

		log.debug("Line " + lineAddr + " compiled to ");
		log.debug("(b)" + realOut, 1);
		realOut = Numbers.convert(2, 16, false, realOut).substring(1);
		log.debug("(x)" + realOut, 1);
		log.unindent();

        lineAddr++;
		return realOut;
	}

	/**
	 * Converts a given label or variable to a boolean offset
	 * @param	in 						Label or Variable used for base
	 * @param	n						Bits to normalize to
	 * @param	line					Line requiring offset, used to verbose error messages
	 * @return							Offset as a binary number
	 * @throws	SyntaxErrorException	Thrown when label or variable is invalid, or by {@link site.projectname.util.Numbers#convert(int,int,boolean,String,int) Numbers.convert()}
	 */
	private String compOffset(String in, int n, String line) throws SyntaxErrorException{
		int offSet = 0;
		int address = 0;
		if (in.startsWith("*")){
			address = labels.get(in);
		} else if (in.startsWith("$")){
			address = variables.get(in);
		} else if(in.matches(NICSyntax.IMM8.toString())){
			log.debug("Immediate value converted to " + Numbers.tcToInt(convertImm(in,8,line,NICSyntax.IMM8.toString())));
			address = lineAddr + Numbers.tcToInt(convertImm(in,8,line,NICSyntax.IMM8.toString()));
		} else{
			throw new SyntaxErrorException("Invalid Label on line " + lineNum + "\n\t" + line);
		}
		offSet += address - lineAddr;
		log.debug("Offset from current addr("+lineAddr+") to " + in.substring(1) + "(" + address + ") is " + offSet + " lines");
        log.debug("Converting offset " + offSet +" into binary with " + n + " bits.");
		log.debug(Numbers.convert(10, 2, true, offSet+"", n),1);
		return Numbers.convert(10, 2, true, offSet+"", n);
	}
	/**
	 * Converts an immediate value to it's binary value
	 * @param	in 						Immediate value to convert
	 * @param	len						Bits to normalize to
	 * @param	line					Line with immediate value, used for verbose error messages
	 * @param	syntax					Syntax required for line, used for verbose error messages
 	 * @return							Binary value of immediate value
	 * @throws	SyntaxErrorException	Thrown when an invalid immediate value is given or by {@link site.projectname.util.Numbers#convert(int,int,boolean,String,int) Numbers.convert()}
	 */
	private String convertImm(String in, int len, String line, String syntax) throws SyntaxErrorException{
		log.debug("");
		if (!(in.startsWith("#") || in.startsWith("x")))
			throw new SyntaxErrorException(line, syntax, lineNum, this.syntax);
		if (in.startsWith("#"))
			return Numbers.convert(10, 2, true, in, len);
		if (in.startsWith("x"))
			return Numbers.convert(16, 2, true, in, len);
		return "";
	}
	/**
	 * Uses object variable bC to fill bits into a char array representing the hexcode to output (Moves left->right)
	 * @param	bits 		Bits to fill
	 * @param	in 			Starting bits
	 * @return				Starting bits with bits appended to the end
	 */
	private char[] fillBits(String bits, char[] in){
		if (bits.replace("X", "").equals(""))
			return in;
		int start = bC;
		int end = start - bits.length();
		log.debug("Filling bits [" + start + ":" + (end + 1) + "] with " + bits);
		for (int i = start; i > end; i--){
			if (bits.charAt(start - i) != 'X'){
				in[15 - i] = bits.charAt(start - i);
				bC--;
			}
		}
		log.debug(Arrays.toString(in), 1);
		log.debug("");
		return in;
	}
	/**
	 * Checks error ArrayList, if it contains errors prints them, otherwise confirms that no errors were found
	 * @return		If any errors have been thrown yet.
	 */
	private boolean checkErrors(){
		if (!errors.isEmpty()){
			System.err.println("\t"+errors.size() + " Error(s) found:");
			log.write("\t"+errors.size()+" Error(s) found:");
			for (String s : errors){
				System.err.println("\t"+s.replace("\n","\n\t"));
				for (String s2 : s.split("\n")){
					log.write("\t"+s2);
				}
			}
			return true;
		} else{
			System.out.println("\tNo Errors found.");
			log.write("\tNo Errors found.");
			return false;
		}
	}

	/**
	 *	Initializes required files
	 */
	private void initFiles(){
		File iF = null;
		File oF = null;
		log.debug("Getting file "+fileName);
		try{
			if(fileName.contains("/") || fileName.contains("\\")){
				String[] names = fileName.split("\\|/");
				File[] dirs = new File[names.length];
				dirs[0] = new File(names[0]);
				for(int i=1; i<names.length;i++){
					dirs[i] = new File(dirs[i-1],names[i]);
				}
				iF = dirs[dirs.length-1];
			} else {
				iF = new File(fileName);
			}
			inFile = new Scanner(iF);
		} catch (Exception e){
			log.writeError(e);
			System.err.println("File not Found!");
			log.debug("Absolute filepath for argument: ");
			log.debug(iF.getAbsolutePath());
			System.exit(-1);
		}
		try{
			oF = new File(fileName.split("[.]")[0] + ".nicp");
			outFile = new PrintWriter(oF);
		} catch (Exception e){
			log.writeError(e);
			System.err.println("Error creating output file!");
			log.debug("Attempting to output to :");
			log.debug(iF.getAbsolutePath());
			System.exit(-1);
		}
	}

	/**
	 * Assembles a file from Assembly to Hex.
	 *
	 * @param file	File to be assembled
	 */
	public void assemble(String file){
		log.write("Assembling file " + fileName);
		if (!fileName.endsWith(".nic")){
			log.debug("Warning: Files should end with '.nic'!");
		}
		initFiles();
		String s = "";
		while (inFile.hasNextLine()){
			try{
				s += firstPass(inFile.nextLine()) + "\n";
			} catch (SyntaxErrorException e){
				errors.add(e.getMessage());
			}
            lineNum++;
		}

		System.out.println("First Pass Complete.");
		log.write("First Pass Complete.");
		if (checkErrors())
			return;
		log.debug("Variables found: " + variables.keySet());
		log.debug("Labels found: " + labels.keySet());

		try{
			inFile = new Scanner(s);
		} catch (Exception e){
		}
		HashMap<Integer,String> lines = new HashMap<Integer,String>();
		lineAddr = 0;
        lineNum = 1;

		String compiled = "";
		String offset = "";
        try{
			offset = Numbers.convert(10,16,false,mainOffset+"",4).substring(1) + " ";
		} catch(SyntaxErrorException e){

		}
		while (inFile.hasNextLine()){
			try{
				String l = inFile.nextLine();
				lines.put(lineAddr,l);
				String line = secondPass(l);

				if (line.length() > 0){
					compiled += line + " ";
				}
			} catch (SyntaxErrorException e){
				errors.add(e.getMessage());
			}
            lineNum++;
		}
		System.out.println("Second Pass Complete.");
		log.write("Second Pass Complete.");
		if (checkErrors())
			return;

        if(Logger.debugGlobal){
            int addr = 0;
            String[] p = compiled.split(" ");
            for(int i=0;i<p.length;i++){
                String l = p[i];
                String label = "";
                for(String key: labels.keySet())
                    if(labels.get(key) == addr)
                        label = key;
                for(String key: variables.keySet())
                    if(variables.get(key) == addr)
                        label = key;
//            for(String l: compiled.split(" ")){
                try{
                    String bin = Numbers.convert(16,2,false,l,16);
                    String address = Numbers.convert(10,16,false,addr+"",4);
					if(lines.get(addr) == null)
                    	log.debug(String.format("%5s %10s %s %s",address,label,l,bin));
					else
						log.debug(String.format("%5s %10s %s %s <- %s",address,label,l,bin,lines.get(addr)));
                } catch(SyntaxErrorException e){
                    log.writeError(e);
                }
                addr++;
            }
        }
		log.unindent();
        outFile.write("v2.0 raw\n"+offset + compiled + "D000");
        outFile.close();
	}
    /**
     * Converts String matching R[0-7] into a binary value of the number
     * @param   in                      Register notation to be converted
     * @return                          Binary value of register
     * @throws  SyntaxErrorException    Thrown if invalid number follows R
     */
    public String convertReg(String in) throws SyntaxErrorException{
		in = in.substring(1);
		return Numbers.convert(10, 2, false, in, 3);
	}


	public static void main(String[] args){
		BetterAssembler a = new BetterAssembler(args);
		a.assemble(a.fileName);
	}

}
