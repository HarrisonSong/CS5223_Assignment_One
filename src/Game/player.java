package Game;

public class player {

    public char[] ID = new char[2];
    public pair currentPosition = new pair();
    public int score = 0;
    public playerType type = playerType.Standard;

    player (char[] id, pair curr, int scr, playerType pt)
    {
        ID = id;
        currentPosition = curr;
        score = scr;
        type = pt;
    }
}
