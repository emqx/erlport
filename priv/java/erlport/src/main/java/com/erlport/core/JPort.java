package com.erlport.core;

import com.erlport.msg.CallMessage;
import com.erlport.proto.Channel;
import com.erlport.proto.Options;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author wangwenhai
 * @date 2020/7/15
 */
public class JPort {
    static final ExecutorService executorService = Executors.newCachedThreadPool();
    // K: MessageId V: UUID Lock
    static final ConcurrentHashMap<Integer, Object> REQUEST_MAP = new ConcurrentHashMap<>();
    // K: MessageId V: Call Result
    static final ConcurrentHashMap<Integer, Object> RESULT_MAP = new ConcurrentHashMap<>();
    // Read Channel
    private static Channel channel;

    /**
     * @param args Cli args
     */
    public static void start(String[] args) {
        Options options = new Options(args);
        channel = new Channel(options);
        Reader reader = new Reader(channel);
        executorService.submit(reader);
    }

    /**
     * synchronized call
     *
     * @param message Call msg
     * @param timeout Timeout
     * @return ErlangTerm Object
     */
    public static Object call(CallMessage message, long timeout) throws Exception {

        final UUID uuid = UUID.randomUUID();
        JPort.REQUEST_MAP.put(message.getId(), uuid);
        channel.write(message);

        System.err.println("[Java] Try waiting response....");
        synchronized (JPort.REQUEST_MAP.get(message.getId())) {
            System.err.println("[Java] Try waiting response.2..");
            JPort.REQUEST_MAP.get(message.getId()).wait(timeout);
            System.err.println("[Java] Waited, handle response....");
        }
        System.err.println("[Java] Try waiting done....");
        Object result = JPort.RESULT_MAP.get(message.getId());
        JPort.REQUEST_MAP.remove(message.getId());
        JPort.RESULT_MAP.remove(message.getId());
        return result;
    }
}
