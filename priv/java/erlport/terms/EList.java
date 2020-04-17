package erlport.terms;

import java.util.*;
import java.lang.*;

public class EList extends Object {

    public ArrayList<Object> elements;

    public Object tail;

    public EList() {
        elements = new ArrayList<Object>();
    }

    public void add(Object e) {
        elements.add(e);
    }

    public void setTail(Object t) {
        this.tail = t; 
    }

    @Override
    public String toString() {
        return elements.toString();
    }
}


