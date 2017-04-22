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

public class BetterAssembler
{
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

	private int lineAddr = 0;
    private int lineNum = 1;
	private int bC = 15;

	/**
	 * Creates a BetterAssembler based on command line (or given) arguments
	 *
	 * @param args
	 *            Arguments used to construct object
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

	private String firstPass(String line) throws SyntaxErrorException{
		line = clean(line);
		if (line.equals("")){
			return line;
		}
		String out = "";
		switch (line.charAt(0)){
    		case '$':
    			// Variable
    			if (line.split(" ")[0].matches(REGEX.VARIABLE.toString()))
    				out = parseVariable(line);
    			else{
    				throw new SyntaxErrorException(line, REGEX.VARIABLE.toString(), lineAddr);
    			}
    			break;
    		case '*':
    			// Label
    			if(line.split(" ")[0].matches(REGEX.LABEL.toString())){
    				out = parseLabel(line);
                    if(out.equals("")){
                        out += ".FILL x0000";
                    }
                }
    			else{
    				throw new SyntaxErrorException(line, REGEX.LABEL.toString(), lineAddr);
    			}
    			break;
    		default:
    			out = line;
    			break;
		}
        lineAddr++;
		return out;
	}

	private String parseVariable(String line) throws SyntaxErrorException {
		log.debug("Parsing variable on line " + lineNum);
		String v = "|-" + lineAddr + ":";
		log.debug(v + Logger.spacer(v, 9) + line);
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

	private String parseLabel(String line){
		log.debug("Parsing label on line " + lineNum);
		String v = "|-" + lineAddr + ":";
		log.debug(v + Logger.spacer(v, 9) + line);
		String[] parts = line.replaceAll("[\\s]+", " ").split(" ");
		labels.put(parts[0], lineAddr);
		String out = "";
		for (int i = 1; i < parts.length; i++)
			out += parts[i] + " ";
		if (!out.equals(""))
			out = out.substring(0, out.length() - 1); // Removes extra space
		return out;
	}

	private String secondPass(final String line) throws SyntaxErrorException{
		if (line.equals(""))
			return "";
        log.debug("");
        log.debug("Parsing command on line " + lineNum);
        log.indent();
		char[] out = { '-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-' };
		String[] parts = line.replaceAll("([\\s]+)|" + REGEX.SPACE, " ").split(" ");
        if(Shorthand.contains(parts[0])){
            log.debug("");
            log.debugSpacer();
			log.debug("");
            log.debug("Command is shorthand, converting to basic commands");
            log.debug(line, 1);
            String[] newLines = Shorthand.get(parts[0]).convertSyntax(line,lineAddr);
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
    					if(parts[1].matches(REGEX.IMM16.toString()))
    						out = fillBits(convertImm(parts[1], 16, line), out);
    					else if(parts[1].matches(REGEX.CHAR.toString()))
    						out = fillBits(Numbers.convert(10,2,false,(int)parts[1].charAt(1)+"",16),out);
    					else
    						throw new SyntaxErrorException(clean(line), com.regex, lineAddr);
    					break ret;
    				case BLK:
    					int value = Integer.parseInt(Numbers.convert(2, 10, false, convertImm(parts[1], 16, line), 16).substring(1));
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
				}
				String[] syntax = com.syntax.split(" ");
				for (String s : syntax){
					if (s.contains("R")){
						log.debug("Adding Register[" + rC + "] (" + parts[rC] + ")");
						out = fillBits(convertReg(parts[rC++]), out);
					} else if (s.contains("X")){
						if (parts[rC].contains("*") || parts[rC].contains("$")){
							out = fillBits(compOffset(parts[rC++], s.length(), line), out);
						} else if (parts[rC].contains("#") || parts[rC].contains("x")){
							if (!(com.value.equals("AND") || com.value.equals("ADD")))
								out = fillBits(convertImm(parts[rC++], s.length(), line), out);
						}
					} else{
						out = fillBits(s, out);
					}
				}
				switch (com){
    				case ADD:
    				case AND: // Can have either a register or an immediate value
    					if (parts[3].matches(REGEX.IMM5.toString())){
    						log.debug("Adding immediate|reg marker");
    						out = fillBits("1", out);
    						out = fillBits(convertImm(parts[3], 5, line), out);
    					} else if (parts[3].matches(REGEX.REGISTER.toString())){
    						log.debug("Adding immediate|reg marker");
    						out = fillBits("0", out);
    						log.debug("Adding buffer");
    						out = fillBits("00", out);
    						log.debug("Adding Register[" + rC + "] (" + parts[rC] + ")");
    						out = fillBits(convertReg(parts[3]), out);
    					} else{
    						// log.unindent();
    						throw new SyntaxErrorException(clean(line), com.regex, lineAddr);
    					}
	                   break ret;
				}
			} else{
				// log.unindent();
				throw new SyntaxErrorException(clean(line), com.regex, lineAddr);
			}
		} else{
			// log.unindent();
			throw new SyntaxErrorException("Invalid command on line " + lineNum + "\n\t" + line);
		}

		String realOut = new String(out);

		if (realOut.contains("-"))
			throw new SyntaxErrorException("Command on line " + lineNum + " has unset bits!\n\t" + line);

		log.debug("Line " + lineAddr + " compiled to ");
		log.debug("(b)" + realOut, 1);
		realOut = Numbers.convert(2, 16, false, realOut).substring(1);
		log.debug("(x)" + realOut, 1);
		log.unindent();

        lineAddr++;
		return realOut;
	}

	private String compOffset(String in, int n, String line) throws SyntaxErrorException{
		int offSet = 0;
		int address = 0;
		if (in.startsWith("*")){
			address = labels.get(in);
		} else if (in.startsWith("$")){
			address = variables.get(in);
		} else if(in.matches(REGEX.IMM8.toString())){
			log.debug("Immediate value converted to " + Numbers.tcToInt(convertImm(in,8,line)));
			address = lineAddr + Numbers.tcToInt(convertImm(in,8,line));
		} else{
			throw new SyntaxErrorException("Invalid Label on line " + lineNum + "\n\t" + line);
		}
		offSet += address - lineAddr;
		log.debug("Offset from current addr("+lineAddr+") to " + in.substring(1) + "(" + address + ") is " + offSet + " lines");
        log.debug("Converting offset " + offSet +" into binary with " + n + " bits.");
		log.debug(Numbers.convert(10, 2, true, offSet+"", n),1);
		return Numbers.convert(10, 2, true, offSet+"", n);
	}

	private String convertImm(String in, int len, String line) throws SyntaxErrorException{
		if (!(in.startsWith("#") || in.startsWith("x")))
			throw new SyntaxErrorException("Invalid Immediate Value on " + lineAddr + "\n\t" + line);
		if (in.startsWith("#"))
			return Numbers.convert(10, 2, true, in, len);
		if (in.startsWith("x"))
			return Numbers.convert(16, 2, true, in, len);
		return "";
	}

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

	private boolean checkErrors(){
		if (!errors.isEmpty()){
			System.err.println(errors.size() + " Error(s) found:");
			for (String s : errors){
				System.err.println(s);
				for (String s2 : s.split("\n"))
					log.write(s2);
			}
			return true;
		} else{
			System.out.println("\t No Errors found.");
			return false;
		}
	}

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
		this.log.write("Assembling file " + fileName);
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
        outFile.write("v2.0 raw\n" + compiled + "D000");
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
