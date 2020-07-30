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
 */
public class ReadThread extends Thread {
    private ConcurrentHashMap<Class<?>, Object> classCache = new ConcurrentHashMap<>();
    private Channel channel;

    ReadThread(Channel channel) {
        setName("IOReadThread");
        this.channel = channel;
    }

    @Override
    public void run() {
        for (; ; ) {
            try {
                Request request = channel.read();
                try {
                    System.err.println("Request :" + request.rawTerm);

                    if (request.type == RequestType.CALL) {
                        Class<?> clazz = Class.forName(request.classname.value);
                        Object instance = classCache.get(clazz);
                        if (instance == null) {
                            instance = clazz.newInstance();
                            classCache.put(clazz, instance);
                        }
                        Class<?>[] classArgs = new Class[request.args.length];
                        Arrays.fill(classArgs, Object.class);
                        Method method = clazz.getMethod(request.methodName.value, classArgs);
                        Object result = method.invoke(instance, request.args);

                        if (result == null) {
                            result = new Atom("ok");
                        }
                        channel.write(Response.success(request.requestId, result));

                    } else if (request.type == RequestType.RESULT) {
                        // [type, Id, Result]
                        // [Atom("r"), 2, Tuple{elements=[Atom("resp"), Atom("x")]}]

                        Tuple tuple = (Tuple) request.rawTerm;
                        if (tuple.length() == 3) {
                            Integer id = (Integer) tuple.get(1);
                            if (JPort.REQUEST_MAP.get(id) != null) {
                                JPort.RESULT_MAP.put(id, tuple.get(2));
                                System.err.println("X :" + id + " Lock:" + JPort.REQUEST_MAP.get(id).toString());
                                synchronized (JPort.REQUEST_MAP.get(id)) {
                                    JPort.REQUEST_MAP.get(id).notifyAll();
                                }
                                System.err.println("Y :" + id + " Result :" + JPort.RESULT_MAP.get(id));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Binary errDesc = Utils.stringToBinary(Utils.getStackTrace(e));
                    channel.write(Response.failure(request.requestId, errDesc));
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