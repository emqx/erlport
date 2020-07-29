package com.erlport.core;

import com.erlport.erlang.term.Atom;
import com.erlport.msg.CallMessage;
import com.erlport.proto.Channel;
import com.erlport.proto.Options;

import java.util.UUID;
import java.util.concurrent.*;

/**
 * @author wangwenhai
 * @date 2020/7/15
 * File description: Java-Erlang port
 */
public class JPort {
    private static Channel channel;
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    static final ConcurrentHashMap<Integer, UUID> MAP = new ConcurrentHashMap<>();


    /**
     * @param args args
     */
    public static void start(String[] args) {
        if (args.length < 4) {
            System.exit(0);
        }
        Options options = new Options(args);
        channel = new Channel(options);
        ReadThread readThread = new ReadThread(channel);
        executorService.execute(readThread);
        System.err.println("<><><><> JPort started");

    }

    /**
     *  synchronized call
     * @param callMessage call msg
     * @param timeout timeout
     * @return result
     */

    public static Object call(CallMessage callMessage, long timeout) {
        Future<Object> future = executorService.submit(new WriteThread(channel, callMessage));
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return new Atom("timout");
        }
    }
}
