package Common;

import Interface.GameInterface;

public class NameStubPair extends Pair<String, GameInterface> {
    
    public NameStubPair(String name, GameInterface stub) {
        super(name, stub);
    }

    public String getPlayerName() {
        return getA();
    }

    public void setPlayerName(String name) {
        setA(name);
    }

    public GameInterface getStub() {
        return getB();
    }

    public void setStub(GameInterface stub) {
        setB(stub);
    }
}
