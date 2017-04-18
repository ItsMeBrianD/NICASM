/**
 * Nic doesn't know how to comment his files
 *
 *
 */
package site.projectname.bomberkid;

import java.util.Arrays;
import site.projectname.util.Logger;

public class Bomb{
    private int power;
    private int life;

    public int xPos, yPos;
    private Logger log;


    public Bomb(int power,int life){
        if(Logger.logs.containsKey("bombs")){
            log = Logger.logs.get("bombs");
        } else {
            log = new Logger("bombs", World.debug);
        }
        this.power = power;
        this.life = life;
        this.xPos = (int)Math.floor(World.player.getX());
        this.yPos = (int)Math.floor(World.player.getY());
        try{Thread.sleep(life);}catch(Exception e){}
        explode(0, new boolean[] {true,true,true,true});
    }
    public Bomb(int power, int life, int x, int y){
        this(power,life);
        this.xPos = x;
        this.yPos = y;
    }
    private void explode(int x,boolean[] checks){
        boolean[] newChecks = new boolean[4];
        if(checks[0]){
            newChecks[0] = World.map.setMap(xPos+x,yPos,'f');
        }
        if(checks[1]){
            newChecks[1] = World.map.setMap(xPos-x,yPos,'f');
        }
        if(checks[2]){
            newChecks[2] = World.map.setMap(xPos,yPos+x,'f');
        }
        if(checks[3]){
            newChecks[3] = World.map.setMap(xPos,yPos-x,'f');
        } if(x+1 < power){
            try{Thread.sleep(300);}catch(Exception e){}
            explode(x+1,newChecks);
        } else {
            clean(0,new boolean[]{true,true,true,true});
        }
    }
    private void clean(int x,boolean[] checks){
        boolean[] newChecks = new boolean[4];
        if(checks[0]){
            newChecks[0] = World.map.setMap(xPos+x,yPos,' ');
        }
        if(checks[1]){
            newChecks[1] = World.map.setMap(xPos-x,yPos,' ');
        }
        if(checks[2]){
            newChecks[2] = World.map.setMap(xPos,yPos+x,' ');
        }
        if(checks[3]){
            newChecks[3] = World.map.setMap(xPos,yPos-x,' ');
        } if(x+2 < power){
            try{Thread.sleep(300);}catch(Exception e){}
            clean(x+1,newChecks);
        }
        World.gui.bombs.remove(0);
    }

}
