package com.erlport.core;

import com.erlport.msg.CallMessage;
import com.erlport.proto.Channel;
import com.erlport.proto.Options;

import java.util.UUID;
import java.util.concurrent.*;

/**
 * @author wangwenhai
 * @date 2020/7/15
 */
public class JPort {
    private static Channel channel;
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    static final ConcurrentHashMap<Integer, UUID> MAP = new ConcurrentHashMap<>();
    /**
     * @param args Cli args
     */
    public static void start(String[] args) {
        Options options = new Options(args);
        channel = new Channel(options);
        ReadThread readThread = new ReadThread(channel);
        executorService.execute(readThread);
        System.err.println("JPort started");
    }
    /**
     * synchronized call
     *
     * @param callMessage Call msg
     * @param timeout     Timeout
     * @return ErlangTerm Object
     */
    public static Object call(CallMessage callMessage, long timeout) throws InterruptedException, ExecutionException, TimeoutException {
        Future<Object> future = executorService.submit(new WriteThread(channel, callMessage));
        return future.get(timeout, TimeUnit.MILLISECONDS);
    }
}
