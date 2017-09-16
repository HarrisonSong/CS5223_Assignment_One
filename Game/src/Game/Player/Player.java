package Game.Player;

import Common.Pair;

public class Player {

    private char[] ID = new char[2];
    private Pair currentPosition = new Pair();
    private int score = 0;
    private PlayerType type = PlayerType.Standard;

    public Player() {

    }

    Player(char[] id, Pair curr, int scr, PlayerType pt) {
        ID = id;
        currentPosition = curr;
        score = scr;
        type = pt;
    }

    public char[] getID() {
        return ID;
    }

    public void setID(char[] ID) {
        this.ID = ID;
    }

    public Pair getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(Pair currentPosition) {
        this.currentPosition = currentPosition;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public PlayerType getType() {
        return type;
    }

    public void setType(PlayerType type) {
        this.type = type;
    }
}
