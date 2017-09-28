package Common;

import Game.Game;
import java.util.concurrent.ThreadLocalRandom;

public class mazePair extends Pair<Integer, Integer> {


    public mazePair(){
        super();
    }

    /**
     * construct a point in the range between (0,0) and (max, max)
     * @param max
     */
    public mazePair(int max) {
        super(ThreadLocalRandom.current().nextInt(0, max), ThreadLocalRandom.current().nextInt(0, max));
    }

    public mazePair(int row, int column) {
        super(row, column);
    }

    public int getRow() {
        return getA();
    }

    public void setRow(int row) {
        setA(row);
    }

    public int getColumn() {
        return getB();
    }

    public void setColumn(int column) {
        setB(column);
    }

    public boolean isValid() {
        int row = getA();
        int column = getB();
        return (row >= 0 && column >= 0 && row < Game.TreasureSize && column < Game.TreasureSize);
    }
}
