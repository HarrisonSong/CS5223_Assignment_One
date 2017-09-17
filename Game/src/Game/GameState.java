package Game;
import Common.Pair;
import Game.Player.Command;
import Game.Player.Player;
import Game.Player.PlayerType;

import java.util.*;

public class GameState {
    private List<Player> playerList = new ArrayList<Player>();
    private Pair[] treasureLocation = new Pair[Game.TreasureSize];

    public List<Player> getPlayerList() {
        return playerList;
    }

    public void setPlayerList(List<Player> playerList) {
        this.playerList = playerList;
    }

    public Pair[] getTreasureLocation(){
        return treasureLocation;
    }

    public void setTreasureLocation(Pair[] treasureLocation) {
        this.treasureLocation = treasureLocation;
    }

    public void initialize(Player firstPlayer) {
        for(int i=0; i<Game.TreasureSize; i++)
        {
            treasureLocation[i] = new Pair(Game.MazeSize-1);
        }
        playerList.add(firstPlayer);
    }

    //return -1 means not found, other number means index in list
    public int getIndexOfPlayerById(char[] id) {
        int result = -1;
        for(Player p: playerList){if(p.isNameAs(id)){result = playerList.indexOf(p); break;}}
        return result;
    }

    public boolean addNewPlayerById(char[] id, PlayerType type) {
        if(getIndexOfPlayerById(id) != -1) return false;
        Player newPlayer;
        boolean found=false;
        for(int i=0; i<10; i++)
        {
            Pair loc = new Pair(Game.MazeSize-1);
            for(Player p: playerList){
                if(p.isAtCell(loc)) {
                    found = true;
                    break;
                }
            }
            if(!found){
                newPlayer = new Player(id, loc, 0, type);
                this.playerList.add(newPlayer);
                return true;
            }
        }
        return false;

    }

    //return true user found, return false user not found
    public boolean makeMove(Command cmd, char[] id) {

        int indexOfPlayer = getIndexOfPlayerById(id);

        if(indexOfPlayer != -1)
        {
            Pair loc = playerList.get(indexOfPlayer).getCurrentPosition();
            switch (cmd){
                case West:
                    loc.col = loc.col -1;
                    break;
                case South:
                    loc.row = loc.row -1;
                    break;
                case East:
                    loc.col = loc.col +1;
                    break;
                case North:
                    loc.row = loc.row +1;
                    break;
                default:
                    break;
            }
            if(isAccessibleLocation(loc))
            {
                Player p = playerList.remove(indexOfPlayer);//last one is lastest active
                List<Integer> treasureGot = findTreasureAtLocation(loc);
                p.setScore(p.getScore()+ treasureGot.size());
                p.setCurrentPosition(loc);
                if(treasureGot.size() != 0){generateNewTreasures(treasureGot);}
                playerList.add(p);
            }
            return true;
        }
        return false;
    }

    private List<Integer> findTreasureAtLocation(Pair loc) {
        List<Integer> indexList = new ArrayList<Integer>();
        for(int i=0; i<Game.TreasureSize; i++) {
            if(treasureLocation[i].equals(loc)) {
                indexList.add(new Integer(i));
            }
        }
        return indexList;
    }

    private boolean findPlayerAtLocation(Pair loc) {
        for(int i=0; i<playerList.size(); i++){
            if(playerList.get(i).isAtCell(loc)){
                return true;
            }
        }
        return false;
    }

    private boolean isAccessibleLocation(Pair loc) {
        if(loc.isValid()){
            return (!findPlayerAtLocation(loc));
        }
        else{return false;}
    }

    private void generateNewTreasures(List<Integer> list)
    {
        for(int i=0; i<list.size(); i++){
            treasureLocation[list.get(i).intValue()] = new Pair(Game.MazeSize);
        }

    }
}

