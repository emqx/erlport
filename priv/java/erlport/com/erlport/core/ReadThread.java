package com.erlport.core;

import com.erlport.erlang.term.Atom;
import com.erlport.erlang.term.Binary;
import com.erlport.erlang.term.Tuple;
import com.erlport.proto.*;

import java.io.EOFException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wangwenhai
 * @date 2020/7/11
 * File description: IO data read thread
 */
public class ReadThread extends Thread {
    // request store,used to save state
    static final ConcurrentHashMap<Integer, Object> requestStore = new ConcurrentHashMap<>();
    private Map<Class<?>, Object> classCache = new HashMap<>();
    private Channel channel;

    ReadThread(Channel channel) {
        setName("IOReadThread");
        this.channel = channel;
    }

    @Override
    public void run() {
        for (; ; ) {
            try {
                Request erlangRequest = channel.read();
                try {

                    // Call
                    if (erlangRequest.type == RequestType.CALL) {
                        Class<?> clazz = Class.forName(erlangRequest.classname.value);
                        Object instance = classCache.get(clazz);
                        if (instance == null) {
                            instance = clazz.newInstance();
                            classCache.put(clazz, instance);
                        }
                        Class<?>[] classArgs = new Class[erlangRequest.args.length];
                        Arrays.fill(classArgs, Object.class);
                        Method method = clazz.getMethod(erlangRequest.methodName.value, classArgs);
                        Object result = method.invoke(instance, erlangRequest.args);

                        if (result == null) {
                            result = new Atom("ok");
                        }
                        channel.write(Response.success(erlangRequest.requestId, result));
                    }
                    // Pure Message
                    if (erlangRequest.type == RequestType.MESSAGE) {
                        //
                    }
                    if (erlangRequest.type == RequestType.ERROR) {
                        //
                    }
                    // Call Result:{'r', Id, Content}
                    if (erlangRequest.type == RequestType.RESULT) {
                        Tuple tuple = (Tuple) erlangRequest.rawTerm;
                        if (tuple.length() == 3) {
                            Integer id = (Integer) tuple.get(1);
                            UUID uuid = JPort.MAP.get(id);
                            if (uuid != null) {
                                synchronized (uuid) {
                                    requestStore.put(id, tuple.get(2));
                                    uuid.notify();
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Binary errDesc = Utils.stringToBinary(Utils.getStackTrace(e));
                    channel.write(Response.failure(erlangRequest.requestId, errDesc));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Binary errDesc = Utils.stringToBinary(Utils.getStackTrace(e));
                if (e instanceof EOFException) {
                    System.exit(0);
                }
                try {
                    channel.write(Response.stop(errDesc));
                } catch (Exception e2) {
                    // Binary errDesc2 = Utils.stringToBinary(Utils.getStackTrace(e2));
                }
            }
        }

    }
}