/**
 * Renders and interfaces map for game
 * @author  Nicholas Deckhut
 * @version 0.0
 **/
package site.projectname.bomberkid;

public class Map {
    private final static String MAP_STRING =
    "XXXXXXXXX" +
    "X  +++  X" +
    "X X+X+X X" +
    "X+++++++X" +
    "X X+X+X X" +
    "X+++++++X" +
    "X X+X+X X" +
    "X  +++  X" +
    "XXXXXXXXX";

    private char[][] map;
    /**
    *returns a new map object with the defalt map layout
    */
    public Map() {
        this(MAP_STRING, 9);
    }

    /**
    *returns a new map object
    *@param map string that contains a map definition
    *@param sideLength amount of chars per line for the map paramater
    */
    public Map(String map, int sideLength) {
        this.map =renderMap(map, sideLength);
    }
    /**
    *renders a map from string to 2d array
    *@param mapS string that contains map layout
    *@param sideLength side length of the map (not all maps are square)
    *@return returns a 2d char array with map from paramater mapS
    */
    private static char[][] renderMap(String mapS, int sideLength){
        char[] mapC = mapS.toCharArray();
        char[][] mapA = new char[sideLength][sideLength];

        for (int i = 0; i < mapA.length; i++){
            for (int j = 0; j < mapA.length; j++){
                mapA[i][j] = mapC[j + i*sideLength];
            }
        }
        return mapA;
    }
    /**
    *prints the map
    */
    private void printMap(){
        for (int i = 0; i < map.length; i++){
            for (int j = 0; j < map.length; j++){
                System.out.print(map[i][j]);
            }
            System.out.println();
        }
    }
    /**
    *returns the map in 2d array form
    *@return returns the map
    */
    public char[][] getMap(){
        return map;
    }

    public boolean setMap(int x, int y, char newChar){
        if(map[x][y] == 'X'){
            return false;
        } else if(map[x][y]=='+'){
            map[x][y] = newChar;
            return false;
        } else {
            map[x][y] = newChar;
            return true;
        }
    }



    //for testing purposes only
    public static void main(String[] args) {

    }
}
