package site.projectname.err;

import site.projectname.util.Logger;

public class StopException extends Exception {
    public StopException(String message, Logger out){
        System.err.println(message);
        for(String s: message.split(" "))
            out.write(s);
        System.exit(-1);
    }
    public StopException(String message){
        System.err.println(message);
        System.exit(-1);
    }
    public StopException(){
        System.err.println("Unknown error encountered, stopping!");
        System.exit(-1);
    }
}
