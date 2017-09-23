package Common.Pair;
import Common.EndPoint;

public class IdEndPointPair extends Pair<String, EndPoint> {
    public IdEndPointPair(){}
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
