package Common.Pair;
import Common.EndPoint;
import Game.Game;

public class IdEndPointPair extends Pair<String, EndPoint> {

    public IdEndPointPair() {
        super();
        this.setA(new String(new char[Game.NAME_LENGTH]));
        this.setB(new EndPoint());
    }
    
    public IdEndPointPair(String name, EndPoint ed) {
        super(name, ed);
    }

    public String getId() {
        return getA();
    }

    public void setId(String name) {
        setA(name);
    }

    public EndPoint getEndPoint() {
        return getB();
    }

    public void setEndPoint(EndPoint type) {
        setB(type);
    }
}
