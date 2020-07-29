import com.erlport.Erlang;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author wangwenhai
 * @date 2020/7/29
 * File description: A test demo
 */
public class Test {
    public static void main(String[] args) {
        try {
            Erlang.call("module", "function", new Object[]{1, 2, 3}, 1000);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
