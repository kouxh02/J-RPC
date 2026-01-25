package com.tgu.consumer;

import com.tgu.config.ConfigValidator;
import com.tgu.pojo.User;
import com.tgu.proxy.ClientProxy;
import com.tgu.service.UserService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class TestConsumer {
    private static final int THREAD_POOL_SIZE = 10;
    private static final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public static void main(String[] args) throws InterruptedException {
        log.info("========================================");
        log.info("        J-RPC 服务消费者启动中...");
        log.info("========================================");
        
        // 验证并打印配置
        ConfigValidator.printAllConfig();
        if (!ConfigValidator.validateConfig()) {
            log.error("配置验证失败，客户端启动中止");
            System.exit(1);
        }
        
        log.info("开始测试RPC调用...\n");
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        ClientProxy clientProxy = new ClientProxy();
        UserService proxy = clientProxy.getProxy(UserService.class);

        // ========== 第一批：触发熔断 ==========
        System.out.println("\n" + "=".repeat(60));
        System.out.println("【第一批】触发熔断 - 发送6个并发请求");
        System.out.println("预期：前3个成功，后3个被限流失败，触发熔断器打开");
        System.out.println("=".repeat(60));

        for (int i = 0; i < 6; i++) {
            int requestId = i + 1;
            executor.submit(() -> {
                try {
                    User user = proxy.getUserByUserId(1);
                    if (user != null) {
                        log.info("✅ 请求{} 成功: {}", requestId, user.getUserName());
                        successCount.getAndIncrement();
                    } else {
                        log.error("❌ 请求{} 失败: user == null", requestId);
                        failCount.getAndIncrement();
                    }
                } catch (Exception e) {
                    log.error("❌ 请求{} 异常: {}", requestId, e.getMessage());
                    failCount.getAndIncrement();
                }
            });
        }

        TimeUnit.SECONDS.sleep(2); // 等待第一批完成
        System.out.println("\n>>> 第一批结果 - 成功:" + successCount.get() + ", 失败:" + failCount.get());
        System.out.println(">>> 熔断器应该已经打开\n");

        // ========== 第二批：验证熔断器OPEN ==========
        System.out.println("=".repeat(60));
        System.out.println("【第二批】验证熔断器OPEN - 立即发送5个请求");
        System.out.println("预期：全部被熔断器拦截，不会发送到服务端");
        System.out.println("=".repeat(60));

        successCount.set(0);
        failCount.set(0);

        for (int i = 0; i < 5; i++) {
            int requestId = i + 1;
            executor.submit(() -> {
                try {
                    User user = proxy.getUserByUserId(1);
                    if (user != null) {
                        log.info("✅ 请求{} 成功: {}", requestId, user.getUserName());
                        successCount.getAndIncrement();
                    } else {
                        log.error("❌ 请求{} 失败: user == null (被熔断器拦截)", requestId);
                        failCount.getAndIncrement();
                    }
                } catch (Exception e) {
                    log.error("❌ 请求{} 异常: {}", requestId, e.getMessage());
                    failCount.getAndIncrement();
                }
            });
        }

        TimeUnit.SECONDS.sleep(1);
        System.out.println("\n>>> 第二批结果 - 成功:" + successCount.get() + ", 失败:" + failCount.get());
        System.out.println(">>> 熔断器仍然保持OPEN状态\n");

        // ========== 等待熔断器恢复 ==========
        System.out.println("=".repeat(60));
        System.out.println("【等待3秒】熔断器将进入HALF_OPEN状态");
        System.out.println("=".repeat(60));

        for (int i = 3; i > 0; i--) {
            System.out.println(">>> 倒计时: " + i + " 秒...");
            TimeUnit.SECONDS.sleep(1);
        }
        System.out.println(">>> 时间到！熔断器应该进入HALF_OPEN状态\n");

        // ========== 第三批：测试HALF_OPEN状态 ==========
        System.out.println("=".repeat(60));
        System.out.println("【第三批】测试HALF_OPEN状态 - 发送5个请求，间隔发送");
        System.out.println("预期：部分成功，如果成功率>=60%，熔断器将恢复CLOSED");
        System.out.println("=".repeat(60));

        successCount.set(0);
        failCount.set(0);

        for (int i = 0; i < 5; i++) {
            int requestId = i + 1;
            executor.submit(() -> {
                try {
                    User user = proxy.getUserByUserId(1);
                    if (user != null) {
                        log.info("✅ 请求{} 成功: {}", requestId, user.getUserName());
                        successCount.getAndIncrement();
                    } else {
                        log.error("❌ 请求{} 失败: user == null", requestId);
                        failCount.getAndIncrement();
                    }
                } catch (Exception e) {
                    log.error("❌ 请求{} 异常: {}", requestId, e.getMessage());
                    failCount.getAndIncrement();
                }
            });
//            TimeUnit.MILLISECONDS.sleep(300); // 间隔300ms发送
        }

        TimeUnit.SECONDS.sleep(2);
        System.out.println("\n>>> 第三批结果 - 成功:" + successCount.get() + ", 失败:" + failCount.get());
        System.out.println(">>> 如果成功率>=60%，熔断器应该已恢复CLOSED状态\n");

        // ========== 第四批：验证熔断器已恢复 ==========
        System.out.println("=".repeat(60));
        System.out.println("【第四批】验证熔断器已恢复 - 发送5个请求");
        System.out.println("预期：正常处理，不会被熔断器拦截");
        System.out.println("=".repeat(60));

        successCount.set(0);
        failCount.set(0);

        for (int i = 0; i < 5; i++) {
            int requestId = i + 1;
            executor.submit(() -> {
                try {
                    User user = proxy.getUserByUserId(1);
                    if (user != null) {
                        log.info("✅ 请求{} 成功: {}", requestId, user.getUserName());
                        successCount.getAndIncrement();
                    } else {
                        log.error("❌ 请求{} 失败: user == null", requestId);
                        failCount.getAndIncrement();
                    }
                } catch (Exception e) {
                    log.error("❌ 请求{} 异常: {}", requestId, e.getMessage());
                    failCount.getAndIncrement();
                }
            });
        }

        TimeUnit.SECONDS.sleep(2);
        System.out.println("\n>>> 第四批结果 - 成功:" + successCount.get() + ", 失败:" + failCount.get());
        System.out.println(">>> 熔断器已恢复正常\n");

        // ========== 测试完成 ==========
        executor.shutdown();
        if (executor.awaitTermination(10, TimeUnit.SECONDS)) {
            System.out.println("=".repeat(60));
            System.out.println("【测试完成】");
            System.out.println("完整生命周期：CLOSED → OPEN → HALF_OPEN → CLOSED");
            System.out.println("=".repeat(60));
        } else {
            log.error("任务超时未完成");
        }
    }
}
