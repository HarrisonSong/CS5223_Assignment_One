//package Common;

import java.io.Serializable;

public class Pair<T, V> implements Serializable {

    private T a;
    private V b;

    public Pair(){}

    public Pair(T a, V b) {
        this.a = a;
        this.b = b;
    }

    public T getA() {
        return a;
    }

    public void setA(T a) {
        this.a = a;
    }

    public V getB() {
        return b;
    }

    public void setB(V b) {
        this.b = b;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null){return false;}
        if(obj == this) {return true;}
        if(!(obj instanceof mazePair)){return false;}
        if(((Pair) obj).a.equals(this.a) && ((Pair) obj).b.equals(this.b)) {return true;}
        else{return false;}
    }
}
