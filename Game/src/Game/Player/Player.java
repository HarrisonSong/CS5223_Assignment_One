package Game.Player;
import Common.Pair;

public class Player {

    private String name;
    private Pair currentPosition = new Pair();
    private int score = 0;
    private PlayerType type = PlayerType.Standard;

    public Player(String name, Pair currentPosition, int score, PlayerType type) {
        this.name = name;
        this.currentPosition = currentPosition;
        this.score = score;
        this.type = type;
    }

    public String getName() {
        return name;
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

    public boolean isNameAs(String name) {return this.name.equals(name);}

    public boolean isAtCell(Pair location){ return this.equals(location);}
}
