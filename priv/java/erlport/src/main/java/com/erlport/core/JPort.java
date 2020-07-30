package com.erlport.core;

import com.erlport.msg.CallMessage;
import com.erlport.proto.Channel;
import com.erlport.proto.Options;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * @author wangwenhai
 * @date 2020/7/15
 */
public class JPort {
    private static Channel channel;
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    // K: MessageId V: UUID Lock
    static final ConcurrentHashMap<Integer, Object> REQUEST_MAP = new ConcurrentHashMap<>();
    // K: MessageId V: Call Result
    static final ConcurrentHashMap<Integer, Object> RESULT_MAP = new ConcurrentHashMap<>();

    /**
     * @param args Cli args
     */
    public static void start(String[] args) {
        Options options = new Options(args);
        channel = new Channel(options);
        ReadThread readThread = new ReadThread(channel);
        executorService.submit(readThread);
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

        //System.err.println("[JAVA] Waiting for call......");
        synchronized (JPort.REQUEST_MAP.get(message.getId())) {
            JPort.REQUEST_MAP.get(message.getId()).wait(timeout);
        }
        //System.err.println("[JAVA] Unlock......");

        Object result = JPort.RESULT_MAP.get(message.getId());
        JPort.REQUEST_MAP.remove(message.getId());
        JPort.RESULT_MAP.remove(message.getId());
        //System.err.println("[JAVA] REQUEST_MAP A " + LocalDateTime.now() + " MessageId:" + message.getId() + " Lock:" + uuid.toString() + " Result is = " + result);
        return result;
    }
}
