package Common;

import Game.Player.PlayerType;

public class NameTypePair extends Pair<String, PlayerType> {

    public NameTypePair(String name, PlayerType type) {
        super(name, type);
    }

    public String getPlayerName() {
        return getA();
    }

    public void setPlayerName(String name) {
        setA(name);
    }

    public PlayerType getPlayerType() {
        return getB();
    }

    public void setPlayerType(PlayerType type) {
        setB(type);
    }
}
