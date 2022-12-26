package cn.hutool.core.thread;

import org.junit.AfterClass;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class RetrievableThreadPoolTest {

    private static RetrievableThreadPool<String> retrievableThreadPool= new RetrievableThreadPool<>(
            Executors.newFixedThreadPool(2),
            0,
            -1L
    );

    private static Callable<String> successEveryTime = () -> "这就是答案！";


    private static Callable<String> failEveryTime = () -> {
        throw new RuntimeException("必然失败的任务，不可能有答案。");
    };

    private static int count = 0;
    Callable<String> successAfterOneFailure = () -> {
        count++;
        if (count == 1) {
            throw new RuntimeException("抛第一次异常");
        }
        return "经过一次异常，终于算出了答案";
    };


    Callable<String> successAfterTwoFailure = () -> {
        count++;
        if (count == 1) {
            throw new RuntimeException("抛第一次异常");
        }
        if (count == 2) {
            throw new RuntimeException("抛第二次异常");
        }
        return "经过两次异常，终于算出了答案";
    };

    Callable<String> successAfterThreeFailure = () -> {
        count++;
        if (count == 1) {
            throw new RuntimeException("抛第一次异常");
        }
        if (count == 2) {
            throw new RuntimeException("抛第二次异常");
        }
        if (count == 3) {
            throw new RuntimeException("抛第三次异常");
        }
        return "经过三次异常，终于算出了答案";
    };

    Callable<String> calculateFor10s = () -> {
        for (int i = 1; i <= 10; i++) {
            Thread.sleep(1000);
            System.out.println("当前计算进度已完成" + i + "0%");
        }
        return "经过十秒的计算，总算得出结果了";
    };



    @Test
    public void test1() throws ExecutionException, InterruptedException {
        retrievableThreadPool.setRetry(0);
        retrievableThreadPool.setTimeout(-1);
        retrievableThreadPool.submit(successEveryTime).get();
    }

    @Test(expected = Exception.class)
    public void test2() throws ExecutionException, InterruptedException {
        retrievableThreadPool.setRetry(0);
        retrievableThreadPool.setTimeout(-1);
        retrievableThreadPool.submit(failEveryTime).get();
    }

    @Test(expected = Exception.class)
    public void test3() throws ExecutionException, InterruptedException {
        retrievableThreadPool.setRetry(0);
        retrievableThreadPool.setTimeout(-1);
        count=0;
        retrievableThreadPool.submit(successAfterOneFailure).get();
    }

    @Test
    public void test4() throws ExecutionException, InterruptedException {
        retrievableThreadPool.setRetry(1);
        retrievableThreadPool.setTimeout(-1);
        count=0;
        retrievableThreadPool.submit(successAfterOneFailure).get();
    }

    @Test(expected = Exception.class)
    public void test5() throws ExecutionException, InterruptedException {
        retrievableThreadPool.setRetry(1);
        retrievableThreadPool.setTimeout(-1);
        count=0;
        retrievableThreadPool.submit(successAfterTwoFailure).get();
    }

    @Test
    public void test6() throws ExecutionException, InterruptedException {
        retrievableThreadPool.setRetry(2);
        retrievableThreadPool.setTimeout(-1);
        count=0;
        retrievableThreadPool.submit(successAfterTwoFailure).get();
    }

    @Test(expected = Exception.class)
    public void test7() throws ExecutionException, InterruptedException {
        retrievableThreadPool.setRetry(2);
        retrievableThreadPool.setTimeout(-1);
        count=0;
        retrievableThreadPool.submit(successAfterThreeFailure).get();
    }

    @Test
    public void test8() throws ExecutionException, InterruptedException {
        retrievableThreadPool.setRetry(3);
        retrievableThreadPool.setTimeout(-1);
        count=0;
        retrievableThreadPool.submit(successAfterThreeFailure).get();
    }

    @Test(expected = Exception.class)
    public void test9() throws ExecutionException, InterruptedException {
        retrievableThreadPool.setRetry(3);
        retrievableThreadPool.setTimeout(-1);
        count=0;
        retrievableThreadPool.submit(failEveryTime).get();
    }

    @Test
    public void test10() throws ExecutionException, InterruptedException {
        retrievableThreadPool.setRetry(0);
        retrievableThreadPool.setTimeout(-1);
        retrievableThreadPool.submit(calculateFor10s).get();
    }

    @Test
    public void test11() throws ExecutionException, InterruptedException {
        retrievableThreadPool.setRetry(0);
        retrievableThreadPool.setTimeout(15000);
        retrievableThreadPool.submit(calculateFor10s).get();
    }

    @Test(expected = Exception.class)
    public void test12() throws ExecutionException, InterruptedException {
        retrievableThreadPool.setRetry(0);
        retrievableThreadPool.setTimeout(5000);
        retrievableThreadPool.submit(calculateFor10s).get();
    }

    @Test(expected = Exception.class)
    public void test13() throws ExecutionException, InterruptedException {
        retrievableThreadPool.setRetry(0);
        retrievableThreadPool.setTimeout(0);
        retrievableThreadPool.submit(calculateFor10s).get();
    }

    @Test(expected = Exception.class)
    public void test14() throws ExecutionException, InterruptedException {
        retrievableThreadPool.setRetry(0);
        retrievableThreadPool.setTimeout(1);
        retrievableThreadPool.submit(calculateFor10s).get();
    }

    @AfterClass
    public static void destroy(){
        retrievableThreadPool.shutDown();
    }


}
