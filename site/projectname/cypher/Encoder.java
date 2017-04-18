package site.projectname.cypher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import site.projectname.util.Logger;


/**
 * Provides explicit methods for the CypherTree class as an encryption method
 *
 * @author  Brian Donald
 * @version 1.0
 * @since   2017-4-17
 */

public class Encoder{

    private HashMap<Character,CypherTree> cypher = new HashMap<Character,CypherTree>();
    private final static HashMap<String,String> hexMap = new HashMap<String,String>();
    private final char key;

    private Logger log;

    /**
     * Initializes a HashMap<String,String> to contain key-value pairs that convert to/from hex
     * Must be called before hexMap can be used.
     */
    public static void initHexMap(){
        hexMap.put("0000","0");
        hexMap.put("0001","1");
        hexMap.put("0010","2");
        hexMap.put("0011","3");
        hexMap.put("0100","4");
        hexMap.put("0101","5");
        hexMap.put("0110","6");
        hexMap.put("0111","7");
        hexMap.put("1000","8");
        hexMap.put("1001","9");
        hexMap.put("1010","A");
        hexMap.put("1011","B");
        hexMap.put("1100","C");
        hexMap.put("1101","D");
        hexMap.put("1110","E");
        hexMap.put("1111","F");
        hexMap.put("0","0000");
        hexMap.put("1","0001");
        hexMap.put("2","0010");
        hexMap.put("3","0011");
        hexMap.put("4","0100");
        hexMap.put("5","0101");
        hexMap.put("6","0110");
        hexMap.put("7","0111");
        hexMap.put("8","1000");
        hexMap.put("9","1001");
        hexMap.put("A","1010");
        hexMap.put("B","1011");
        hexMap.put("C","1100");
        hexMap.put("D","1101");
        hexMap.put("E","1110");
        hexMap.put("F","1111");
    }


    /**
     * Initializes Encoder with Cypher Trees based on the key.
     * @param   key Given value to be used as the key for the header tree
     */
    public Encoder(char key){
        if(Logger.logs.containsKey("Encoder")){
            log = Logger.logs.get("Encoder");
        } else {
            log = new Logger("Encoder");
        }
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
                out2+=hexMap.get(temp);
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
            log.write(c +" -> " + hexMap.get(c+""));
            temp += hexMap.get(c+"");
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
        Encoder.initHexMap();
        Encoder e = new Encoder('*');
        Scanner sc = new Scanner(System.in);
        String s = e.encode(sc.nextLine());
        System.out.println("Encoded: " + s);
        System.out.println("Decoded: " + e.decode(s));
    }
}
