package com.tgu.consumer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadLocalDirtyDataDemo {

    // 模拟一个存放用户信息的 ThreadLocal
    private static ThreadLocal<String> userSession = new ThreadLocal<>();

    public static void main(String[] args) {
        // 1. 创建一个固定大小为 1 的线程池
        // 重点：必须是 1，这样才能保证两个任务一定是被同一个线程（Thread-1）执行的，以此模拟线程复用
        ExecutorService pool = Executors.newFixedThreadPool(1);

        // 2. 任务一：正常设置了用户，但是【忘记 remove】
        pool.execute(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println(threadName + ": 正在处理【张三】的请求...");
            userSession.set("用户数据: 张三");


            userSession.remove();            // ❌ 模拟业务代码结束，但开发人员忘了写 userSession.remove();
        });

        // 3. 任务二：代表一个新的请求，它本不该有数据
        pool.execute(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println(threadName + ": 正在处理【李四】的请求...");

            // ⚠️ 灾难发生：李四并没有登录，却读到了张三的数据！
            String data = userSession.get();
            if (data != null) {
                System.out.println("⚠️⚠️⚠️ 发生严重事故！李四读到了脏数据 -> " + data);
            } else {
                System.out.println("正常：没有读到旧数据");
            }
        });

        pool.shutdown();
    }
}