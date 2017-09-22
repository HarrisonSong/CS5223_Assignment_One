package Game;

import Common.Pair;
import Game.Player.Command;
import Game.Player.Player;
import Game.Player.PlayerType;

import java.util.*;

public class GameGlobalState {
    public static final int LocationExplorerAttemptTime = 10;

    private List<Player> playerList = new ArrayList<Player>();
    private List<Pair> treasureLocation = new ArrayList<Pair>(Game.TreasureSize);
    private int latestActivePlayerIndex = -1;

    public void initialize(Player firstPlayer) {
        for(int i=0; i < Game.TreasureSize; i++) {
            treasureLocation.add(new Pair(Game.MazeSize));
        }
        playerList.add(firstPlayer);
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
            Pair newLocation = new Pair(Game.MazeSize);
            for(Player p: playerList){
                if(p.isAtCell(newLocation)) {
                    found = true;
                    break;
                }
            }
            if(!found){
                this.playerList.add(new Player(name, newLocation, 0, type));
                return true;
            }
        }
        return false;
    }

    public boolean removePlayer(String name){
        if(!isPlayerNameUsed(name)) return false;
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
            Pair currentLocation = targetPlayer.getCurrentPosition();
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
                    this.latestActivePlayerIndex = indexOfPlayer;
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

    public List<Pair> getTreasureLocation(){
        return treasureLocation;
    }

    public void setTreasureLocation(List<Pair> treasureLocation) {
        this.treasureLocation = treasureLocation;
    }

    public int getLatestActivePlayerIndex() {
        return latestActivePlayerIndex;
    }


    //helper methods
    private void generateNewTreasures(List<Integer> list) {
        for(int i=0; i<list.size(); i++){
            treasureLocation.set(list.get(i).intValue(), new Pair(Game.MazeSize));
        }
    }

    private List<Integer> findTreasuresAtLocation(Pair location) {
        List<Integer> indexList = new ArrayList<>();
        for(int i=0; i<Game.TreasureSize; i++) {
            if(treasureLocation.get(i).equals(location)) {
                indexList.add(i);
            }
        }
        return indexList;
    }

    private boolean isLocationAccessible(Pair location) {
        return location.isValid() && !findPlayerAtLocation(location);
    }

    private boolean findPlayerAtLocation(Pair location) {
        for(Player player : this.playerList){
            if(player.isAtCell(location)){
                return true;
            }
        }
        return false;
    }
}

