package erlport.terms;

import java.util.*;
import java.lang.*;

public class Binary {

    public byte[] raw;

    public Binary(byte[] bytes) {
        this.raw = bytes;
    }

    @Override
    public String toString() {
        return new String(raw);
    }
}


