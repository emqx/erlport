# Java Erlang port driver
## 1.Build package

```$shell
mvn clean
mvn package
```
Package at path:`target/erlport-{Version}.jar`

## 2.User interface

```java
import com.erlport.Erlang;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
public class Test {
    public static void main(String[] args) {
        try {
            Erlang.call("module", "function", new Object[]{1, 2, 3}, 1000);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
```