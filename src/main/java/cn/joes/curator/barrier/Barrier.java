package cn.joes.curator.barrier;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.concurrent.*;

/**
 * Created by myijoes on 2018/12/3.
 *
 * @author wanqiao
 */
public class Barrier {

    private static final int QTY = 5;

    private static final String PATH = "/examples/barrier";

    public static void main(String[] args) throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", new ExponentialBackoffRetry(1000, 3));
        client.start();
        ExecutorService service = Executors.newFixedThreadPool(QTY);
        DistributedBarrier controlBarrier = new DistributedBarrier(client, PATH);
        controlBarrier.setBarrier();
        for (int i = 0; i < QTY; ++i) {
            final DistributedBarrier barrier = new DistributedBarrier(client, PATH);
            final int index = i;
            Callable<Void> task = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    Thread.sleep((long) (3 * Math.random()));
                    System.out.println("Client #" + index + " 等待");
                    barrier.waitOnBarrier();
                    System.out.println("Client #" + index + " 开始");
                    return null;
                }
            };
            service.submit(task);
        }
        Thread.sleep(1000 * 3);
        System.out.println("所有的Client都在等待");
        controlBarrier.removeBarrier();
        service.shutdown();
        service.awaitTermination(10, TimeUnit.MINUTES);
        client.close();
        System.out.println("OK!");
    }

}
