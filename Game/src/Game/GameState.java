package Game;
import Common.Pair;
import Game.Player.Player;

import java.util.*;

public class GameState {
    public List<Player> playerList = new ArrayList<Player>();
    public Pair[] treasureLocation = new Pair[Core.TreasureSize];
}

