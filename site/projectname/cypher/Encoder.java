package site.projectname.cypher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import site.projectname.util.Logger;
import site.projectname.util.Numbers;

/**
 * Provides explicit methods for the CypherTree class as an encryption method
 *
 * @author  Brian Donald
 * @version 1.0
 * @since   2017-4-17
 */

public class Encoder{

    private HashMap<Character,CypherTree> cypher = new HashMap<Character,CypherTree>();
    private final char key;

    private Logger log;


    /**
     * Initializes Encoder with Cypher Trees based on the key.
     * @param   key Given value to be used as the key for the header tree
     */
    public Encoder(char key){
        log = Logger.getLog("Encoder",Logger.debug);
        cypher.put(key, new CypherTree(key));
        for(int i=32;i<127;i++){
            //TODO Make this vary based on the key
            cypher.put((char)i,new CypherTree((char)((i*(int)key)%127)));
        }
        this.key = key;
    }


    /**
     * Encodes a given tree based on the CypherTree set
     * @param   in  String to encrypt
     * @return      Encrypted String
     */
    public String encode(String in){
        log.write("Encoding String: " + in);
        String out = "";
        char key = this.key;
        for(char c: in.toCharArray()){
            if((int)c>=32 && (int)c<=126){
                CypherTree ct = cypher.get(key);
                log.write(c + " -> " + ct.pathOf(c));
                out += ct.pathOf(c);
                key = c;
            } else {
                out += c;
            }
        }
        String out2 = "";
        String temp = "";
        while(out.length() % 4 > 0)
            out += "0";
        for(char c: out.toCharArray()){
            temp += c;
            if(temp.length() == 4){
                out2+=Numbers.hexMap.get(temp);
                temp = "";
            }
        }
        log.write("Encoded String = " + out2);
        return out2;
    }
    /**
     * Decrypts (or attempts to) based on CypherTree's in the class
     * @param   in  Encrypted String to Decrypt
     * @return      Decrypted String
     */
    public String decode(String in){
        String out = "";
        String temp = "";

        for(char c: in.toCharArray()){
            log.write(c +" -> " + Numbers.hexMap.get(c+""));
            temp += Numbers.hexMap.get(c+"");
        }
        in = temp;
        log.write(temp);
        temp = "";
        ArrayList<String> parts = new ArrayList<String>();
        for(char c: in.toCharArray()){
            if(c == '0' || c == '1'){
                temp += c;
                if(temp.length() == 7){
                    parts.add(temp);
                    temp = "";
                }
            } else {
                parts.add(c+"");
            }
        }
        char key = this.key;
        for(String part: parts){
            if(part.length() == 7){
                CypherTree ct = cypher.get(key);
                char c = ' ';
                try{
                    c = ct.traverse(part);
                    log.write(part + " -> " + c);
                } catch(NodeNotFoundException e){
                    e.printStackTrace();
                    log.writeError(e);
                }
                key = c;
                out += c;
            }
            else{
                out += part;
            }
        }
        log.write("Decoded :" + out);
        return out;
    }

    public static void main(String args[]){
        if(args.length == 0)
            Logger.debug = false;
        else if(args[0].equals("-debug") || args[0].equals("-v"))
            Logger.debug = true;
        else
            Logger.debug = false;

        Numbers.init();
        Encoder e = new Encoder('*');
        Scanner sc = new Scanner(System.in);
        String s = e.encode(sc.nextLine());
        System.out.println("Encoded: " + s);
        System.out.println("Decoded: " + e.decode(s));
    }
}
