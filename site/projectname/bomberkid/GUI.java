/**
 * Contains GUI Object used to init display for game
 * @author  Brian Donald
 * @version 0.0
 **/

package site.projectname.bomberkid;

import javax.swing.JFrame;
import javax.swing.JPanel;

import javax.swing.Timer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

import site.projectname.util.Logger;
import site.projectname.util.ImageUtil;

import java.util.ArrayList;

import static site.projectname.bomberkid.World.player;

public class GUI {

    public JFrame frame = new JFrame("Bomberkid");
    public Logger log;
    private Image[] tiles = new Image[12];
    private Image playerIMG;

    private GraphicsTimer gt;
    private final int WINDOW_SIZE = 650;
    protected ArrayList<Bomb> bombs;

    int increment;
    public GUI() {
        if(!Logger.logs.containsKey("GUI")) {
            log = new Logger("GUI");
            log.write("GUI Log started!");
        } else {
            log = Logger.logs.get("GUI");
        }
        frame.setSize(WINDOW_SIZE,WINDOW_SIZE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addKeyListener(new KeyBinder());
        frame.setVisible(true);
        gt = new GraphicsTimer();
        bombs = new ArrayList<Bomb>();
    }

    public GUI(boolean debug){
        log = new Logger("GUI", debug);
        log.write("GUI Log started!");
        frame.setSize(WINDOW_SIZE,WINDOW_SIZE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addKeyListener(new KeyBinder());
        frame.setVisible(true);
        gt = new GraphicsTimer();

    }

    private class GraphicsTimer implements ActionListener {
        final int tickrate = 16;
        final Timer timer;
        char[][] map = World.map.getMap();

        public GraphicsTimer() {
            increment = (int)Math.ceil((double)WINDOW_SIZE/(double)map.length);
            timer = new Timer(tickrate, this);
            log.write("Graphics Timer Started!");
            log.write("\t\tTickrate: "+tickrate);
            log.write("\t\tWindow Resolution: "+WINDOW_SIZE);
            log.write("\t\tGraphics Increment: "+increment);
            log.write("\t\tMap Array Size: "+map.length+"x"+map[0].length);
            tiles[0] = ImageUtil.resizeImage(ImageUtil.toBufferedImage(ImageUtil.importImage("Ground.png")),(WINDOW_SIZE/map.length),(WINDOW_SIZE/map.length));
            tiles[1] = ImageUtil.resizeImage(ImageUtil.toBufferedImage(ImageUtil.importImage("Barrier.png")),(WINDOW_SIZE/map.length),(WINDOW_SIZE/map.length));
            playerIMG = ImageUtil.resizeImage(ImageUtil.toBufferedImage(ImageUtil.importImage("Player.png")),(WINDOW_SIZE/map.length),(WINDOW_SIZE/map.length));
            timer.start();
        }
        public void actionPerformed(ActionEvent e) {
            Graphics g = frame.getGraphics();
            map = World.map.getMap();

            //Map Drawing
            for(int width=0; width<WINDOW_SIZE;width+=increment) {
                for(int height=0;height<WINDOW_SIZE;height+=increment) {
                    switch(map[width/increment][height/increment]) {
                        case 'X':
                            if(!World.debug)
                                g.drawImage(tiles[1],width,height,null);
                            else
                                g.setColor(Color.black);
                            break;
                        case '+':
                            if(!World.debug){}
                                //Draw Destructable Wall
                            else
                                g.setColor(Color.gray);
                            break;
                        case 'f':
                                g.setColor(Color.orange);
                            break;
                        default:
                            if(!World.debug)
                                g.drawImage(tiles[0],width,height,null);
                            else
                                g.setColor(Color.white);
                            break;
                    }
                    if(World.debug)
                        g.fillRect(width,height,increment,increment);
                }
            }
            g.drawImage(playerIMG,(int)Math.floor(World.player.getX()*increment),(int)Math.floor(World.player.getY()*increment),null);
            player.move();
        }
    }
    private class KeyBinder implements KeyListener {
        public void keyTyped(KeyEvent e) {
            if (e.getKeyCode()==KeyEvent.VK_SPACE) {
              //drop bomb
              bombs.add(new Bomb(4, 100, (int)(Math.floor(player.getX()) - (Math.floor(player.getX()) % increment)), (int)(Math.floor(player.getY()) - (Math.floor(player.getY()) % increment))));
            }
            // if (e.getKeyChar() == 'r')
            // {
            //     System.out.println("MARKER MARKER MARKER MARKER MARKER MARKER MARKER MARKER MARKER MARKER MARKER");
            // }
        }


        public void keyPressed(KeyEvent e) {
            double speed = 0.05;
            if (e.getKeyChar() == 'w' || e.getKeyChar() == 'W') {
              player.setYVel(-1 * speed);
            }
            if (e.getKeyChar() == 'a' || e.getKeyChar() == 'A') {
              player.setXVel(-1 * speed);
            }
            if (e.getKeyChar() == 's' || e.getKeyChar() == 'S') {
              player.setYVel(speed);
            }
            if (e.getKeyChar() == 'd' || e.getKeyChar() == 'D') {
              player.setXVel(speed);
            }
        }

        public void keyReleased(KeyEvent e) {
            if (e.getKeyChar() == 'w' || e.getKeyChar() == 'W' || e.getKeyCode() == 38) {
                player.setYVel(0);
            }
            if (e.getKeyChar() == 'a' || e.getKeyChar() == 'A' || e.getKeyCode() == 37) {
                player.setXVel(0);
            }
            if (e.getKeyChar() == 's' || e.getKeyChar() == 'S' || e.getKeyCode() == 40) {
                player.setYVel(0);
            }
            if (e.getKeyChar() == 'd' || e.getKeyChar() == 'D' || e.getKeyCode() == 39) {
                player.setXVel(0);
            }
        }
    }
}
