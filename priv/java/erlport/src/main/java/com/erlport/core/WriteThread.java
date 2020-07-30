package com.erlport.core;

import com.erlport.msg.Message;
import com.erlport.proto.Channel;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author wangwenhai
 * @date 2020/7/28
 */
public class WriteThread implements Callable<Object> {
    private Channel channel;
    private Message message;

    WriteThread(Channel channel, Message message) {
        this.channel = channel;
        this.message = message;
    }

    @Override
    public Object call() {
        try {
            final UUID uuid = UUID.randomUUID();
            JPort.REQUEST_MAP.put(message.getId(), uuid);
            channel.write(message);
            //System.err.println("Waiting for......");
            synchronized (JPort.REQUEST_MAP.get(message.getId())) {
                JPort.REQUEST_MAP.get(message.getId()).wait(4000);
            }
            //System.err.println("Unlock......");

            Object result = JPort.RESULT_MAP.get(message.getId());
            JPort.REQUEST_MAP.remove(message.getId());
            JPort.RESULT_MAP.remove(message.getId());
            //System.err.println("REQUEST_MAP A " + LocalDateTime.now() + " MessageId:" + message.getId() + " Lock:" + uuid.toString() + " Result is = " + result);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
