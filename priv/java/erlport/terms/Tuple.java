package erlport.terms;

import java.util.*;
import java.lang.*;

public class Tuple extends Object {

    public Object[] elements;

    public static Tuple forElements(Object[] l) {
        Tuple t = new Tuple(l.length);
        t.elements = l;
        return t;
    }

    public Tuple(Integer n) {
        elements = new Object[n];
    }

    public void set(Integer pos, Object e) {
        elements[pos] = e;
    }

    public Object get(Integer pos) {
        return elements[pos];
    }

    public Integer length() {
        return elements.length;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Tuple({");
        for(Object e: elements) {
            sb.append(e.toString() + ", ");
        }
        sb.delete(sb.length()-2, sb.length());
        sb.append("})");
        return sb.toString();
    }
}
