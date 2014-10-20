package ru.gagauz.socket.server;

import java.util.concurrent.*;

public class TestFuture implements Callable<Integer> {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        System.out.println("++ execute before");
        Future<Integer> future = executorService.submit(new TestFuture());
        System.out.println("++ execute after");
        System.out.println("++ future get before");
        int i = future.get();
        System.out.println("++ future get after");
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("+ call before");
        Thread.sleep(10000);
        System.out.println("+ call after");
        return 10;
    }
}
