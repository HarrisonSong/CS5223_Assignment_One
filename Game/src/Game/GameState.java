package Game;
import Common.Pair;
import Game.Player.Player;

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
}

