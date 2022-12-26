package cn.hutool.core.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 可重试的线程池
 * 构造函数如下：
 * public RetrievableThreadPool(ExecutorService executorService, int retry, long timeout)
 * executorService表示线程池
 * retry表示重试次数，在执行出现异常后，还会额外重试几次
 * timeout表示超时时间，单位是毫秒。在整个执行过程中（包括重试），如果超过了这个时间，任务将被停止。设置为负数表示不限制时间。
 * RetrievableThreadPool<T>中的泛型 T 表示任务返回类型
 */
public class RetrievableThreadPool<T> {

    private static final Logger LOGGER= LoggerFactory.getLogger(RetrievableThreadPool.class);

    private final ExecutorService executor;

    private final Thread listener;

    private final AtomicInteger retry;

    private final AtomicLong timeout;

    private final DelayQueue<DelayedFutureTask<T>> queue;

    public RetrievableThreadPool(ExecutorService executorService, int retry, long timeout) {
        this.executor = executorService;
        this.retry = new AtomicInteger(retry);
        this.timeout = new AtomicLong(timeout);
        this.queue = new DelayQueue<>();
        this.listener = new Thread(() -> {
            while (true) {
                try {
                    DelayedFutureTask<T> take = queue.take();
                    if (!take.isDone()) {
                        LOGGER.info("发现已超时任务{}，即将终止",take);
                        take.cancel(true);
                    }
                } catch (InterruptedException e) {
                    return;
                }
//                if (Thread.currentThread()
            }
        });
        listener.start();
    }

    /**
     * 提交任务
     * @param callable 具体要执行的任务
     * @return 任务返回值
     */
    public  Future<T> submit(Callable<T> callable) {
        RetryableCallable<T> retryableCallable = new RetryableCallable<>(callable, retry.get());
        DelayedFutureTask<T> task = new DelayedFutureTask<>(retryableCallable, timeout.get());
        executor.submit(task);
        if (timeout.get() > 0) {
            queue.add(task);
        }
        return task;
    }

    /**
     * 关闭线程池
     */
    public void shutDown(){
        LOGGER.info("即将关闭线程池");
        executor.shutdown();
        listener.interrupt();
    }

    public int getRetry() {
        return retry.get();
    }

    public void setRetry(int retry) {
        if (retry<0){
            throw new IllegalArgumentException("重试次数不得小于0");
        }
        this.retry.set(retry);
    }

    public long getTimeout() {
        return timeout.get();
    }

    public void setTimeout(long timeout) {
        if (timeout==0){
            throw new IllegalArgumentException("超时时间要么大于0（单位为毫秒），要么小于0（不限制）");
        }
        this.timeout.set(timeout);
    }
}
