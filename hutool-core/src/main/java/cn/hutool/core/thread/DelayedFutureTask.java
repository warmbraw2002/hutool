package cn.hutool.core.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class DelayedFutureTask<T> extends FutureTask<T> implements Delayed {

    /**
     * 预计超时的时间戳
     */
    private final long expireAt;

    public DelayedFutureTask(Callable<T> callable, long duration) {
        super(callable);
        this.expireAt = System.currentTimeMillis() + duration;
    }


    public long getDelay(TimeUnit unit) {
        return Math.max(0L, unit.convert(this.expireAt - System.currentTimeMillis(), TimeUnit.MILLISECONDS));
    }

    public int compareTo(Delayed o) {
        return o instanceof DelayedFutureTask ? Long.compare(this.expireAt, ((DelayedFutureTask<?>) o).expireAt) : 0;
    }
}