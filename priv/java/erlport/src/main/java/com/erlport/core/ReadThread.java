package com.erlport.core;

import com.erlport.erlang.term.Atom;
import com.erlport.erlang.term.Binary;
import com.erlport.erlang.term.Tuple;
import com.erlport.proto.*;

import java.awt.*;
import java.io.EOFException;
import java.lang.reflect.InvocationTargetException;
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
                //System.err.println("[JAVA] try read a message\n");
                Request request = channel.read();
                //System.err.println("[JAVA] Read:" + request + "\n");
                try {
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

                        Object finalInstance = instance;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Object result = null;
                                try {
                                    result = method.invoke(finalInstance, request.args);
                                    if (result == null) {
                                        result = new Atom("ok");
                                    }
                                    channel.write(Response.success(request.requestId, result));
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();

                    } else if (request.type == RequestType.RESULT) {
                        // [type, Id, Result]
                        // [Atom("r"), 2, Tuple{elements=[Atom("resp"), Atom("x")]}]

                        Tuple tuple = (Tuple) request.rawTerm;
                        if (tuple.length() == 3) {
                            Integer id = (Integer) tuple.get(1);
                            if (JPort.REQUEST_MAP.get(id) != null) {
                                JPort.RESULT_MAP.put(id, tuple.get(2));
                                //System.err.println("X :" + id + " Lock:" + JPort.REQUEST_MAP.get(id).toString());
                                synchronized (JPort.REQUEST_MAP.get(id)) {
                                    JPort.REQUEST_MAP.get(id).notifyAll();
                                }
                                //System.err.println("Y :" + id + " Result :" + JPort.RESULT_MAP.get(id));
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