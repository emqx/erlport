import java.io.*;
import java.util.*;
import com.emqx.erlang.term.*;
import com.emqx.msg.CallMessage;
import com.emqx.core.*;

public class Echo {

    public static Object echo(Object r) {
      return r;
    }

    public static Object rev_call(Object pid, Object x) throws Exception{
       CallMessage callMessage = new CallMessage();
       callMessage.setModule(new Atom("erlport_SUITE"));
       callMessage.setFunction(new Atom("handle_call"));
       callMessage.setArgs(new Object[]{ new Object[]{pid, x}});
       System.err.println("Echo.java 回调的参数: " + pid + " " + x);
       Object result = JPort.call(callMessage, 1000);
       System.err.println("Echo.java 回调的返回值: " + pid + " " + x);
       return result;
    }
}
