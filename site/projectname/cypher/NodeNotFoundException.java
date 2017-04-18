package site.projectname.cypher;
/**
 * Exception thrown by CypherTree when a node is not found
 * @author      Brian Donald
 * @since       2017-4-17
 * @version     1.0
 */

public class NodeNotFoundException extends Exception {
    public NodeNotFoundException(){
    	super("");
    }
    public NodeNotFoundException(String message){
    	super(message);
    }

}
