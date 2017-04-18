/**
 * Bomberman Clone created as a quick project
 *
 * @author  Nic Deckhut, Brian Donald
 * @version 0.0
 */

package site.projectname.bomberkid;

class World{
    protected static Map map;
    protected static GUI gui;
    protected static Player player;
    protected static boolean debug = false;
    public static void main(String[] args){
        if(args.length > 0){
            if(args[0].equals("-debug"))
                World.debug = true;
        }

        World.map = new Map();
        World.gui = new GUI(World.debug);
        World.player = new Player(1,1);

        gui.bombs.add(new Bomb(4, 1000, 1, 1));
    }

}
