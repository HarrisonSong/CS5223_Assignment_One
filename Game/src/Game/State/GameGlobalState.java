package Game.State;

import Common.mazePair;
import Game.Player.Command;
import Game.Player.Player;
import Game.Player.PlayerType;

import java.io.Serializable;
import java.util.*;

public class GameGlobalState implements Serializable {
    public static final int LocationExplorerAttemptTime = 10;

    private int mazeSize;
    private int treasuresSize;

    private List<Player> playerList;
    private List<mazePair> treasureLocation;
    private Stack<Integer> activePlayerQueue;

    public GameGlobalState(int mazeSize, int treasuresSize) {
        this.mazeSize = mazeSize;
        this.treasuresSize = treasuresSize;
        this.playerList = new ArrayList<>();
        this.treasureLocation = new ArrayList<>(treasuresSize);
        for(int i = 0; i < treasuresSize; i++) {
            treasureLocation.add(new mazePair(mazeSize));
        }
        this.activePlayerQueue = new Stack<>();
    }

    public int getIndexOfPlayerByName(String name) {
        int result = -1;
        for(Player player: playerList){
            if(player.isNameAs(name)){
                result = playerList.indexOf(player);
                break;
            }
        }
        return result;
    }

    public boolean isPlayerNameUsed(String name) {
        return getIndexOfPlayerByName(name) != -1;
    }

    public boolean addNewPlayerByName(String name, PlayerType type) {
        if(isPlayerNameUsed(name)) return false;
        boolean found = false;
        for(int i = 0; i < LocationExplorerAttemptTime; i++) {
            mazePair newLocation = new mazePair(this.mazeSize);
            for(Player p: playerList){
                if(p.isAtCell(newLocation)) {
                    found = true;
                    break;
                }
            }
            if(!found){
                this.playerList.add(new Player(name, newLocation, 0, type));
                activePlayerQueue.push(this.playerList.size() - 1);
                return true;
            }
        }
        return false;
    }

    public boolean removePlayerByName(String name){
        if(!isPlayerNameUsed(name)) return false;
        activePlayerQueue.removeElement(getIndexOfPlayerByName(name));
        this.playerList.remove(getIndexOfPlayerByName(name));
        return true;
    }

    /**
     *
     * @param cmd
     * @param playerName
     * @return
     *  true: target user is not found.
     *  false: target user is found and proceed movement.
     */
    public boolean makeMove(Command cmd, String playerName) {
        int indexOfPlayer = getIndexOfPlayerByName(playerName);
        if(isPlayerNameUsed(playerName)) {
            Player targetPlayer = this.playerList.get(indexOfPlayer);
            mazePair currentLocation = targetPlayer.getCurrentPosition();
            switch (cmd){
                case West:
                    currentLocation.setColumn(currentLocation.getColumn() - 1);
                    break;
                case South:
                    currentLocation.setRow(currentLocation.getRow() - 1);
                    break;
                case East:
                    currentLocation.setColumn(currentLocation.getColumn() + 1);
                    break;
                case North:
                    currentLocation.setRow(currentLocation.getRow() + 1);
                    break;
                default:
                    break;
            }
            if(isLocationAccessible(currentLocation)) {
                targetPlayer.setCurrentPosition(currentLocation);
                List<Integer> treasures = findTreasuresAtLocation(currentLocation);
                if(!treasures.isEmpty()){
                    targetPlayer.setScore(targetPlayer.getScore() + treasures.size());

                    /**
                     * pop up the player to be latest active
                     */
                    this.activePlayerQueue.removeElement(indexOfPlayer);
                    this.activePlayerQueue.push(indexOfPlayer);
                    generateNewTreasures(treasures);
                }
            }
            return true;
        }
        return false;
    }

    public List<Player> getPlayerList() {
        return playerList;
    }

    public void setPlayerList(List<Player> playerList) {
        this.playerList = playerList;
    }

    public List<mazePair> getTreasureLocation(){
        return treasureLocation;
    }

    public void setTreasureLocation(List<mazePair> treasureLocation) {
        this.treasureLocation = treasureLocation;
    }

    public int findNextActivePlayerIndex() {
        if(this.activePlayerQueue.size() == 0){
            return -1;
        }else{
            return this.activePlayerQueue.pop();
        }
    }

    //helper methods
    private void generateNewTreasures(List<Integer> list) {
        for(int i=0; i<list.size(); i++){
            treasureLocation.set(list.get(i).intValue(), new mazePair(this.mazeSize));
        }
    }

    private List<Integer> findTreasuresAtLocation(mazePair location) {
        List<Integer> indexList = new ArrayList<>();
        for(int i = 0; i < this.treasuresSize; i++) {
            if(treasureLocation.get(i).equals(location)) {
                indexList.add(i);
            }
        }
        return indexList;
    }

    private boolean isLocationAccessible(mazePair location) {
        return location.isValid() && !findPlayerAtLocation(location);
    }

    private boolean findPlayerAtLocation(mazePair location) {
        for(Player player : this.playerList){
            if(player.isAtCell(location)){
                return true;
            }
        }
        return false;
    }

    //return id of first player with the type asked.
    public String getIdByType(PlayerType type) {
        for(int i=0; i< playerList.size(); i++) {
            if(playerList.get(i).getType() == type) return playerList.get(i).getName();
        }
        return "";
    }
}

