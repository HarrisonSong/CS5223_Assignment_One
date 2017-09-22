package Game.Player;
import Common.Pair.mazePair;

public class Player {

    private String name;
    private mazePair currentPosition = new mazePair();
    private int score = 0;
    private PlayerType type = PlayerType.Standard;

    public Player(String name, mazePair currentPosition, int score, PlayerType type) {
        this.name = name;
        this.currentPosition = currentPosition;
        this.score = score;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public mazePair getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(mazePair currentPosition) {
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

    public boolean isAtCell(mazePair location){ return this.equals(location);}
}
