package com.erlport.core;

import com.erlport.msg.Message;
import com.erlport.proto.Channel;

import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * @author wangwenhai
 * @date 2020/7/28
 * File description:
 */
public class WriteThread implements Callable<Object> {
    private Channel channel;
    private Message message;

    public WriteThread(Channel channel, Message message) {
        this.channel = channel;
        this.message = message;
    }

    @Override
    public Object call() throws Exception {
        channel.write(message);
        UUID uuid = java.util.UUID.randomUUID();
        JPort.MAP.put(message.getId(), uuid);
        System.err.println("调用Call消息  ID=:" + message.getId() + " UUID:" + uuid);

        synchronized (uuid) {
            System.err.println("开始等待结果通知:" + message.getId());
            uuid.wait(1000);
            System.err.println("通知:返回值收到  ID=:" + ReadThread.requestStore.get(message.getId()));
            return ReadThread.requestStore.get(message.getId());
        }
    }
}
