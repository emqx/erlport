package erlport;

import java.io.*;
import erlport.terms.*;

class Utils {

    // Java String -> Erlang Binary
    static Binary stringToBinary(String str) {
        return new Binary(str.getBytes());   
    }

    static String getStackTrace(Exception e) {
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.flush();
            sw.flush();

        } catch (Exception e2) {
            e2.printStackTrace();
        } finally {
            if (sw != null) {
                try {
                    sw.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (pw != null) {
                pw.close();
            }
        }
        return sw.toString();
    }
}
