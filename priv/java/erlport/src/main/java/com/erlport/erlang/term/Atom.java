package com.erlport.erlang.term;

public class Atom {

    public String value;

    public Atom(String v) {
        this.value = v;
    }

    @Override
    public String toString() {
        return String.format("Atom(\"%s\")", value);
    }
}
