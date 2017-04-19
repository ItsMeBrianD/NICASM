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
     * @return              Hexadecimal value of input. Formatted as [x]([0-9]|[A-F])+
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
            out += next;
        }
        log.debug("Converted to " + out);
        return out;
    }
    /**
     * Converts a decimal value to a binary value
     * @param   decimal     Decimal value to be converted
     * @return              Binary value of input.
     */
    public static String decToBin(int decimal){
        String out = "";
        log.debug("Converting " + decimal + " to Binary!");
        while(decimal > 0){
            int temp = decimal % 2;
            out = (char)(temp+48) + out;
            decimal /= 2;
        }
        log.debug("Converted to " + out);
        return out;
    }
    /**
     * Converts a decimal value to a normalized binary value, truncates left-most values if too long.
     * @param   decimal     Decimal value to be converted
     * @param   n           Number of bits in output
     * @return              Binary value of input.
     */
    public static String decToBin(int decimal,int n){
        log.debug("Converting " + decimal + " to Binary!");
        String out = "";
        while(decimal > 0){
            int r = decimal % 2;
            out = (char)(r+48) + out;
            decimal /= 2;
        }
        while(out.length() > n){
            out = out.substring(1);
        }
        while(out.length() < n){
            out = '0' + out;
        }
        log.debug("Converted to " + out);
        return out;
    }

    /**
     * Converts a Binary value to a Decimal values
     * @param   Binary      Binary value to be converted
     * @return              Decimal value of input
     */
     public static int binToDec(String binary){
         int out = 0;
         log.debug("Converting " + binary + " to Decimal!");
         for(char c: binary.toCharArray()){
             out *= 2;
             out += (int)(c-48);
         }
         log.debug("Converted to " + out);
         return out;
     }

     /**
      * Uses a map of hex -> binary to convert a hexadecimal value to binary
      * @param  hex     Hexadecimal value to convert
      * @return         Binary value of input
      */
     public static String hexToBin(String hex){
        String out = "";
        if(hex.startsWith("x"))
            hex = hex.substring(1);
        for(char c: hex.toCharArray())
            out += hexMap.get(c);
        return out;
     }
     /**
      * Uses a map of hex -> binary to convert a hexadecimal value to binary
      * @param  hex     Hexadecimal value to convert
      * @param  n       Number of bits in output
      * @return         Normalized binary value of input
      */
     public static String hexToBin(String hex,int n){
        String out = "";
        if(hex.startsWith("x"))
            hex = hex.substring(1);
        for(char c: hex.toCharArray()){
            out += hexMap.get(c+"");
        }
        while(out.length() > n)
            out = out.substring(1);
        while(out.length() < n)
            out = '0' + out;
        return out;
     }
     /**
      * Uses a map of binary -> hex to convert a binary value to hexadecimal
      * @param  bin     Binary value to convert
      * @return         Binary value as hexadecimal
      */
     public static String binToHex(String bin){
        while(bin.length() % 4 != 0)
            bin = '0' + bin;
        String out = "";
        while(bin.length() > 0){
            log.debug(bin.substring(0,4) +"->"+ hexMap.get(bin.substring(0,4)+""));
            out += hexMap.get(bin.substring(0,4)+"");
            bin = bin.substring(4);
        }
        return out;
     }

    /**
     * Initializes a HashMap<String,String> to contain key-value pairs that convert to/from hex
     * Must be called before hexMap can be used.
     */
    public static void init(){
        Numbers.log = Logger.getLog("Numbers",false);
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
    public static void main(String[] args){
        init();
        for(int i=0;i>-50;i--)
            decToBin(i,8);
    }
}
