package site.projectname.util;

import java.util.HashMap;

public class Numbers{
    /**
     * Contains conversions for Hex -> Binary and Binary -> Hex
     */
    public final static HashMap<String,String> hexMap = new HashMap<String,String>();

    private static Logger log;

    public static String convert(int startBase, int endBase, boolean signed, String in, int normalize){
        if(endBase != 2)
            return convert(startBase,endBase,signed,in);
        String out = convert(startBase,endBase,signed,in);
        while(out.length() > normalize)
            out = out.substring(0,out.length()-1);
        while(out.length() < normalize){
            if(signed && in.charAt(0) == '-')
                out = '1' + out;
            else
                out = '0' + out;
        }
        return out;
    }

    public static String convert(int startBase, int endBase, boolean signed, String in){
        String out = "";
        int value = 0;
        boolean neg = false;
        if(in.startsWith("#") || in.startsWith("x"))
            in = in.substring(1);
        if(signed){
            if(startBase == 2 && in.charAt(0) == '1' && endBase != 16){
                in = subFlip(in);
                neg = true;
            } else if (in.startsWith("-")){
                in = in.substring(1); // Chop off negative sign
                neg = true;
            }
        }
        // Convert to Decimal
        if(startBase == 10)
            value = Integer.parseInt(in);
        else {
            for(char c: in.toCharArray()){
                value *= startBase;
                if((c+"").matches("[0-9]")){
                    // If number is a real integer
                    value += Integer.parseInt(c+"");
                } else if((c+"").matches("[A-F]")){
                    value += (int)c - 55;
                } else {
                }
            }
        }
        if(endBase != 10) {
            while(value%endBase > 0 || value > 0){
                int rem = value % endBase;
                if(endBase > 10 && rem > 10){
                    out = (char)(rem+55) + out;
                } else {
                    out = rem + out;
                }
                value /= endBase;
            }
        } else {
            out += value;
        }
        switch(endBase){
            case 16:
                out = "x" + out;
                break;
            case 10:
                out = "#" + out;
                break;
            default:
                break;
        }
        if(neg && endBase == 2)
            out = flipAdd('0'+out);
        else if(neg){
            if(out.startsWith("#") || out.startsWith("x"))
                out = out.charAt(0) + '-' + out.substring(1);
            else
                out = '-' + out;
        }
        return out;
    }

    private static char flip(char in){
        if(in == '1')
            return '0';
        else
            return '1';
    }

    private static String flipAdd(String in){
        char[] out = new char[in.length()];
        // Flip Bits
        for(int i=0;i<in.length();i++){
            out[i] = flip(in.charAt(i));
        }
        // Add One
        for(int i=out.length-1;i>=0;i--){
            out[i] = flip(out[i]);
            if(out[i] == '1'){
                break;
            }
        }
        return new String(out);
    }

    private static String subFlip(String in){
        char[] out = new char[in.length()];
        // Subtract one
        for(int i=in.length()-1;i>=0;i--){
            out[i] = flip(in.charAt(i));
            if(in.charAt(i) == '1')
                break;
        }
        // Flip Bits
        for(int i=0;i<out.length;i++){
            out[i] = flip(out[i]);
        }
        return new String(out);
    }

    public static void main(String[] args){
        for(int i=2;i<17;i++){
            String temp = convert(10,i,false,"15",4);
            String temp2 = convert(i,10,false,temp,4);
            System.out.println("15 = " + temp2+"\t(b"+i+")");

        }
    }
}
