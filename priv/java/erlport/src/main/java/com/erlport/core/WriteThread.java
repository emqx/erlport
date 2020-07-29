package com.erlport.core;

import com.erlport.msg.Message;
import com.erlport.proto.Channel;

import java.util.UUID;
import java.util.concurrent.Callable;

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
    public Object call() throws Exception {
        channel.write(message);
        UUID uuid = UUID.randomUUID();
        JPort.MAP.put(message.getId(), uuid);

        synchronized (uuid) {
            uuid.wait(1000);
            Object o = ReadThread.requestStore.get(message.getId());
            ReadThread.requestStore.remove(message.getId());
            return o;
        }
    }
}
