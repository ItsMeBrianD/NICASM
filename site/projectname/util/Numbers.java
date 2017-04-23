package site.projectname.util;

import java.util.HashMap;

import site.projectname.err.SyntaxErrorException;

public class Numbers{
    /**
     * Contains conversions for Hex -> Binary and Binary -> Hex
     */
    public final static HashMap<String,String> hexMap = new HashMap<String,String>();

    private static Logger log;
    /**
     * Converts a number in base startBase to endBase with bit normalization for base 2 and 16
     * @param   startBase   Base of input number
     * @param   endBase     Base of output number
     * @param   signed      Is number signed
     * @param   in          Number to convert
     * @param   normalize   Bits to normalize to
     * @return              in as endBase
     * @throws  SyntaxErrorException    Thrown if output cannot be normalized to expected normalization
     */
    public static String convert(int startBase, int endBase, boolean signed, String in, int normalize) throws SyntaxErrorException {
        if(endBase != 2 && endBase != 16)
            return convert(startBase,endBase,signed,in);
        String out = convert(startBase,endBase,signed,in);
        if(out.length() > normalize)
            throw new SyntaxErrorException("Possible loss of precision!\n\t"+in+" cannot be normalized to "+normalize+" bits without loss of precision!");
        if(endBase == 2){
            while(out.length() < normalize){
                if(signed && in.length() >= 2 && (in.charAt(0) == '-' || in.charAt(1) == '-'))
                    out = '1' + out;
                else
                    out = '0' + out;
            }
        } else if(endBase == 16){
            out = out.substring(1);
            while(out.length() < normalize){
                if(signed && ((in.charAt(0) == '-' || in.charAt(1) == '-') || (startBase == 2 && in.charAt(0) == '1')))
                    out = "F" + out;
                else
                    out = "0" + out;
            }
            out = 'x'+out;
        }
        return out;
    }
    /**
     * Converts a number in base startBase to endBase
     * @param   startBase   Base of input number
     * @param   endBase     Base of output number
     * @param   signed      Is number signed
     * @param   in          Number to convert
     * @return              in as endBase
     * @throws  SyntaxErrorException    Thrown if output cannot be normalized to expected normalization
     */
    public static String convert(int startBase, int endBase, boolean signed, String in){
        String out = "";
        int value = 0;
        boolean neg = false;
        if(in.equals(""))
            in = "0";
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
        if(startBase == 2 && endBase == 16){
            char[] lookup = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
            while(in.length() % 4 > 0)
                in = '0' + in;
            for(int i=0;i<in.length();i+=4){
                int v = Integer.parseInt(in.charAt(i)+"");
                v *= 2;
                v += Integer.parseInt(in.charAt(i+1)+"");
                v *= 2;
                v += Integer.parseInt(in.charAt(i+2)+"");
                v *= 2;
                v += Integer.parseInt(in.charAt(i+3)+"");
                out += lookup[v];
            }
            out = 'x' + out;
            return out;
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

        if(neg && endBase == 2){
            out = flipAdd("0"+out,2);
        }
        else if(neg){
            if(out.startsWith("#") || out.startsWith("x"))
                out = out.charAt(0) + "-" + out.substring(1);
            else
                out = "-" + out;
        }
        return out;
    }
    /**
     * Converts a two's compliment binary number to an integer
     * @param   in  Negative Binary Number to convert
     * @return      Decimal value of in
     */
    public static int tcToInt(String in){
        in = subFlip(in);
        //System.out.println(in);
        int out = 0;
        for(int i=0;i<in.length();i++){
            out*=2;
            if(in.charAt(i) == '1')
                out++;
            //System.out.println(out);
        }
        return -1 * out;
    }

    /**
     * Converts a number of base radix to a negative binary number
     * @param   in      Number to convert
     * @param   radix   Base of in
     * @return          In as negative binary number
     */
    private static String flipAdd(String in, int radix){
        int t = Integer.parseInt(in, radix);
        t = ~t;
        t++;
        String s = Integer.toBinaryString(t);
        return s.substring(s.length()-in.length());
    }

    /**
     * Converts a negative binary number to a postive binary number
     * @param   in  Binary number to convert
     * @return      In as positive binary number
     */
    private static String subFlip(String in){
        int t = Integer.parseInt(in,2);
        t--;
        t = ~t;
        String s = Integer.toBinaryString(t);
        return s.substring(s.length()-in.length());
    }


}
