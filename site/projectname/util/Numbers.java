package site.projectname.util;

import java.util.HashMap;

public class Numbers{
    /**
     * Contains conversions for Hex -> Binary and Binary -> Hex
     */
    public final static HashMap<String,String> hexMap = new HashMap<String,String>();

    private static Logger log;



    /**
     * Converts a hexadecimal number to an decimal value
     *
     * @param   hex Hex string to be converted to decimal. Must match (x){0,1}([0-9]|[A-F])+
     * @return      Decimal value of Hex Input
     */
    public static int hexToDec(String hex){
        log.debug("Converting " + hex + " to decimal!");
        if(hex.startsWith("x"))
            hex = hex.substring(1,hex.length());
        int out = 0;
        String t = "";
        for(char c: hex.toUpperCase().toCharArray()){
            out *= 16;
            t += c;
            if(t.matches("[0-9]")){
                log.debug("Converting " + c + " to " + Integer.parseInt(""+c));
                out += Integer.parseInt(""+c);
            }
            else{
                log.debug("Converting " + c + " to " + (c-55));
                out += (int)c - 55;
            }
            t = "";
        }
        log.debug(hex+" base 16 = "+out+" base 10");
        return out;
    }
    /**
     * Converts a decimal value to a hexadecimal value
     * @param   decimal     Decimal value to be converted
     * @return              Hexadecimal value of inpup. Formatted as [x]([0-9]|[A-F])+
     */
    public static String decToHex(int decimal){
        String out = "x";
        log.debug("Converting " + decimal + " to hex!");
        while(decimal > 16){
            int temp = decimal / 16;
            char next;
            if(temp < 10){
                next = String.valueOf(temp).charAt(0);
            } else {
                next = (char)(temp+55);
            }
            log.debug("Adding " + next);
            out += next;
            decimal %= 16;
        } if(decimal > 0){
            int temp = decimal;
            char next;
            if(temp < 10){
                next = String.valueOf(temp).charAt(0);
            } else {
                next = (char)(temp+55);
            }
            log.debug("Adding " + next);
            out += next;
        }
        log.debug("Converted to " + out);
        return out;
    }
    /**
     * Initializes a HashMap<String,String> to contain key-value pairs that convert to/from hex
     * Must be called before hexMap can be used.
     */
    public static void init(){
        Numbers.log = Logger.getLog("Numbers",Logger.debug);
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
}
