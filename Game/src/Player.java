import java.io.Serializable;

public class Player implements Serializable {

    private String name;
    private MazePair currentPosition = new MazePair();
    private int score = 0;
    private PlayerType type = PlayerType.Standard;

    public Player(String name, MazePair currentPosition, int score, PlayerType type) {
        this.name = name;
        this.currentPosition = currentPosition;
        this.score = score;
        this.type = type;
    }

    public MazePair getCurrentPosition() {
        return new MazePair(currentPosition.getRow(), currentPosition.getColumn());
    }

    public void setCurrentPosition(MazePair currentPosition) {
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

    public boolean isAtCell(MazePair location){ return this.currentPosition.equals(location);}

    public void showWhereIAm(){
        System.out.printf(
                "Name: %s\n" +
                        "Score: %d\n" +
                        "Position: %d %d\n",
                this.name,
                this.score,
                this.currentPosition.getRow(),
                this.currentPosition.getColumn());
    }
}
