package Common;
import Game.Game;

import java.util.concurrent.ThreadLocalRandom;

//use to represent location in maze
public class Pair {
    public int row = 0;
    public int col = 0;

    public Pair(){}

    //generate a random between (0,0) (max, max)
    public Pair(int max)
    {
        row = ThreadLocalRandom.current().nextInt(0, max);
        col = ThreadLocalRandom.current().nextInt(0, max);
    }

    public Pair(int r, int c){
        row = r;
        col = c;
    }

    public boolean isValid()
    {
        return (row >= 0 && col >= 0 && row <=Game.TreasureSize-1 && col <= Game.TreasureSize-1);
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == null){return false;}
        if(obj == this) {return true;}
        if(!(obj instanceof Pair)){return false;}
        if(((Pair) obj).row == this.row && ((Pair) obj).col == this.col) {return true;}
        else{return false;}
    }

}
