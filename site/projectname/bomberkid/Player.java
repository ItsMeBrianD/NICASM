/**
 * Player object
 * @author  Nicholas Deckhut
 * @version 0.0
 **/
 package site.projectname.bomberkid;

 public class Player {
     private double xPos, xVel;
     private double yPos, yVel;
     /**
     *creats a player object that starts at (1,1)
     */
     public Player() {
         this(1,1);
     }
     /**
     *creats a player object that starts at (x, y)
     *@param x the x coordinate of the player
     *@param y the y coordinate of the player
     */
     public Player(double x, double y) {
         this.xPos = x;
         this.yPos = y;
         this.xVel = 0;
         this.yVel = 0;
     }

     /**
     *returns the x position of ther player
     *@return returns the x position of the player
     */
     public double getX() {
         return xPos;
     }

     /**
     *returns the y position of ther player
     *@return returns the y position of the player
     */
     public double getY() {
         return yPos;
     }

     /**
     *changes the x position of the player
     *@param newX the value in which the player postion will be changed to
     */
     public void setX(double newX) {
         xPos = newX;
     }

     /**
     *changes the y position of the player
     *@param newY the value in which the player postion will be changed to
     */
     public void setY(double newY) {
         yPos = newY;
     }

     public double getXVel(){
         return this.xVel;
     }
     public double getYVel(){
         return this.yVel;
     }
     public void setXVel(double newX){
         this.xVel = newX;
     }
     public void setYVel(double newY){
         this.yVel = newY;
     }
     public void move(){
         setX(getX() + getXVel());
         setY(getY() + getYVel());
     }
 }
