package Common;

import Game.Game;
import java.util.concurrent.ThreadLocalRandom;

//use to represent location in maze
public class Pair {
    private int row = 0;
    private int col = 0;

    public Pair(){}

    //generate a random between (0,0) (max, max)
    public Pair(int max) {
        row = ThreadLocalRandom.current().nextInt(0, max);
        col = ThreadLocalRandom.current().nextInt(0, max);
    }

    public Pair(int row, int column) {
        this.row = row;
        this.col = column;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return row;
    }

    public void setCol(int column) {
        this.col = column;
    }

    public boolean isValid() {
        return (row >= 0 && col >= 0 && row < Game.TreasureSize && col < Game.TreasureSize);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null){return false;}
        if(obj == this) {return true;}
        if(!(obj instanceof Pair)){return false;}
        if(((Pair) obj).row == this.row && ((Pair) obj).col == this.col) {return true;}
        else{return false;}
    }

}
