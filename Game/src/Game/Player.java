package Game;

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
}
