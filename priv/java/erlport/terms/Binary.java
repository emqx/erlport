package erlport.terms;

import java.util.*;
import java.lang.*;

public class Binary {

    public byte[] raw;

    public Binary(byte[] bytes) {
        this.raw = bytes;
    }

    public Binary(String str) {
        this.raw = str.getBytes();
    }

    @Override
    public String toString() {
        try {
            return new String(raw);
        } catch (Exception e) {
            return Arrays.toString(raw);
        }
    }
}
