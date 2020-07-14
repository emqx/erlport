import java.io.*;
import java.util.*;
import erlport.terms.*;

public class Echo {

    public static Object echo(Object r) {
        return r;
    }

    public static void rev_call(Object pid, Object r) {
        // TODO: erlang.call(Atom(b'erlport_SUITE'), Atom(b'handle_call'), [pid, r])
        return;
    }
}
