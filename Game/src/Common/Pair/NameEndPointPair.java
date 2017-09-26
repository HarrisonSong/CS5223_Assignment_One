package Common.Pair;
import Common.EndPoint;
import Game.Game;

public class NameEndPointPair extends Pair<String, EndPoint> {

    public NameEndPointPair() {
        super();
        this.setA(new String(new char[Game.NAME_LENGTH]));
        this.setB(new EndPoint());
    }
    
    public NameEndPointPair(String name, EndPoint ed) {
        super(name, ed);
    }

    public String getName() {
        return getA();
    }

    public void setName(String name) {
        setA(name);
    }

    public EndPoint getEndPoint() {
        return getB();
    }

    public void setEndPoint(EndPoint type) {
        setB(type);
    }
}
