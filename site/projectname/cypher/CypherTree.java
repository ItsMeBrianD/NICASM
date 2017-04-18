package site.projectname.cypher;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Random;

import site.projectname.util.Logger;


/**
 * Encryption System based on Tree Addresses
 * A given "key" is used as a seed to populate a tree with most ascii characters,
 * from there the letter to encode is represented as a 7-bit address, and the encoded letter
 * is used as a "key" for the next tree, so on so forth.
 * @author  Brian Donald
 * @version 1.0
 * @since 2017-4-17
 */
class CypherTree{
    private Node head;
    private final Random r;
    private Logger log;


    /**
     * Generates a tree with ascii codes 32-126 populated for treepath encryption
     * @param   head    A given "key" for the tree
     */
    public CypherTree(char head){
        log = Logger.getLog("CypherTree",Logger.debug);
        r = new Random((int)head);
        head -= 97;
        int[] order = new int[97];
        ArrayList<Integer> unused = new ArrayList<Integer>();
        for(int i=32;i<127;i++)
            unused.add(i);
        for(int i=0; i<95; i++){
            //lo 97, hi 123, size 26
            int x = r.nextInt(unused.size());
            order[i] = unused.get(x);
            unused.remove(x);
        }
        this.head = new Node("");
        buildRecursive(order,this.head);
        log.write(this.toString());
    }
    private void buildRecursive(int[] values, Node head){
        if(values.length == 0)
            return;
        if(values.length > 2){
            int[] lo = Arrays.copyOfRange(values,0,values.length/2);
            int[] hi = Arrays.copyOfRange(values,values.length/2,values.length);
            buildRecursive(lo,head.makeLeft());
            buildRecursive(hi,head.makeRight());
        } else if (values.length == 2) {
            head.left = new Node((char)values[0], head.leftPath());
            head.right = new Node((char)values[1], head.rightPath());
        } else if (values.length == 1){
            head.left = new Node((char)values[0], head.leftPath());
        }
    }
    /**
     * Searches the tree down a given path
     * @param   path    String of 1's and 0's used as a path to locate a character
     * @throws  NodeNotFoundException   Thrown when a node isn't found, usually because a path is incomplete
     * @return          Returns the character at the end of the path
     */
    public char traverse(String path) throws NodeNotFoundException{
        Node out = head;
        for(char n: path.toCharArray()){
            if(n == '0')
                out = out.left;
            else if (n == '1')
                out = out.right;
            else
                throw new NodeNotFoundException("Address contains invalid characters!");
            if(out == null)
                throw new NodeNotFoundException("Invalid Address!");
        }
        if(!out.isLeaf())
            throw new NodeNotFoundException("Address refers to branch!");
        return out.getContent();
    }
    /**
     * Converts CypherTree to a human-readable format
     * Example:
     * 0000000 : a
     * 0000001 : b
     * 0000010 : c
     * @return  Human-readable format of CypherTree
     *
     */
    public String toString(){
        return toStringHelper(head);
    }
    private String toStringHelper(Node head){
        String out = "";
        if(head.isLeaf()){
            out = head.toString();
        } else {
            head.toString();
            if(head.left != null)
                out += toStringHelper(head.left);
            if(head.right != null)
                out += toStringHelper(head.right);
        }
        return out;
    }
    /**
     * Gives the path of a given character if it is containted within the tree.
     * @param   in  Character to find in tree
     * @return      Path of input character represented by 1's and 0's
     */
    public String pathOf(char in){
        return pathOf(in,this.head);
    }
    private String pathOf(char in, Node head){
        String out = "";
        if(head == null){
            return out;
        }
        if(head.isLeaf() && in == head.getContent()){
            return head.getPath();
        } else {
            if(!head.isLeaf()){
                out += pathOf(in,head.left);
                out += pathOf(in,head.right);
            } else if (head.left != null){
                out += pathOf(in,head.left);
            }
        }
        if(out.length() > 7)
            out = out.substring(0,7);
        return out;
    }

    private class Node{
        public Node left,right;
        private char content;
        private String path = "";

        public Node(Node left, Node right, String path){
            this.left = left;
            this.right = right;
            this.path = path;
        } public Node (char content, String path){
            this.content = content;
            this.path = path;
        } public Node(String path){
            this.path = path;
        }

        public boolean isLeaf(){
            return this.left==null && this.right==null;
        } public char getContent(){
            return this.content;
        } public Node makeLeft(){
            this.left = new Node(this.path + "0");
            return this.left;
        } public Node makeRight(){
            this.right = new Node(this.path + "1");
            return this.right;
        } public String leftPath(){
            return this.path + "0";
        } public String rightPath(){
            return this.path + "1";
        } public String getPath(){
            return this.path;
        } public String toString(){
            if(isLeaf())
                return this.path + "\t: " + this.content + "\n";
            else
                return this.path + "\n";
        }

    }

    public static void main(String args[]){
        CypherTree ct = new CypherTree('z');
        System.out.println(ct);
    }
}
