package Game.Player;
import Common.mazePair;

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
        return new mazePair(currentPosition.getRow(), currentPosition.getColumn());
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

    public boolean isAtCell(mazePair location){ return this.equals(location);}
}
