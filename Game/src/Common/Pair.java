package Common;

import Game.Game;
import java.util.concurrent.ThreadLocalRandom;

public class Pair {
    private int row = 0;
    private int column = 0;

    public Pair(){}

    /**
     * construct a point in the range between (0,0) and (max, max)
     * @param max
     */
    public Pair(int max) {
        row = ThreadLocalRandom.current().nextInt(0, max);
        column = ThreadLocalRandom.current().nextInt(0, max);
    }

    public Pair(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return row;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public boolean isValid() {
        return (row >= 0 && column >= 0 && row < Game.TreasureSize && column < Game.TreasureSize);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null){return false;}
        if(obj == this) {return true;}
        if(!(obj instanceof Pair)){return false;}
        if(((Pair) obj).row == this.row && ((Pair) obj).column == this.column) {return true;}
        else{return false;}
    }

}
