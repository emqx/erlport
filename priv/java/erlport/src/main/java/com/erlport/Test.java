package com.erlport;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author wangwenhai
 * @date 2020/7/30
 * File description:
 */
public class Test {

    public static void main(String[] args) throws InterruptedException {
        Lock lock = new ReentrantLock();
        lock.tryLock(1000, TimeUnit.MILLISECONDS);

        System.out.println("Next");
    }
}
