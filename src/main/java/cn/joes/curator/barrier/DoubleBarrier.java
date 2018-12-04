package cn.joes.curator.barrier;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.barriers.DistributedDoubleBarrier;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by myijoes on 2018/12/3.
 * <p>
 * 双栅栏允许客户端在计算的开始和结束时同步。当足够的进程加入到双栅栏时，进程开始计算，当计算完成时，离开栅栏
 * <p>
 * memberQty是成员数量，当enter方法被调用时，成员被阻塞，直到所有的成员都调用了enter。当leave方法被调用时，它也阻塞调用线程，知道所有的成员都调用了leave。
 * 就像百米赛跑比赛， 发令枪响， 所有的运动员开始跑，等所有的运动员跑过终点线，比赛才结束。
 * 注意：参数memberQty的值只是一个阈值，而不是一个限制值。当等待栅栏的数量大于或等于这个值栅栏就会打开！
 * <p>
 * 与栅栏(DistributedBarrier)一样,双栅栏的barrierPath参数也是用来确定是否是同一个栅栏的，双栅栏的使用情况如下：
 * 1.从多个客户端在同一个路径上创建双栅栏(DistributedDoubleBarrier),然后调用enter()方法，等待栅栏数量达到memberQty时就可以进入栅栏。
 * 2.栅栏数量达到memberQty，多个客户端同时停止阻塞继续运行，直到执行leave()方法，等待memberQty个数量的栅栏同时阻塞到leave()方法中。
 * 3.memberQty个数量的栅栏同时阻塞到leave()方法中，多个客户端的leave()方法停止阻塞，继续运行。
 * <p>
 * DistributedDoubleBarrier类的主要方法如下：
 * enter()、enter(long maxWait, TimeUnit unit) - 等待同时进入栅栏
 * leave()、leave(long maxWait, TimeUnit unit) - 等待同时离开栅栏
 * <p>
 * 异常处理：DistributedDoubleBarrier会监控连接状态，当连接断掉时enter()和leave方法会抛出异常。
 *
 * 注意：创建双栅栏的数量为：(QTY + 2)，而创建双栅栏的参数为：new DistributedDoubleBarrier(client, PATH, QTY)，当等待栅栏的数量大于或等于这个值(QTY)栅栏就会打开！
 *
 * @author wanqiao
 */
public class DoubleBarrier {

    private static final int QTY = 5;
    private static final String PATH = "/examples/barrier";

    public static void main(String[] args) throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", new ExponentialBackoffRetry(1000, 3));
        client.start();
        ExecutorService service = Executors.newFixedThreadPool(QTY);
        for (int i = 0; i < (QTY + 2); ++i) {
            final DistributedDoubleBarrier barrier = new DistributedDoubleBarrier(client, PATH, QTY);
            final int index = i;
            Callable<Void> task = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    Thread.sleep((long) (3 * Math.random()));
                    System.out.println("Client #" + index + " 等待");
                    if (false == barrier.enter(5, TimeUnit.SECONDS)) {
                        System.out.println("Client #" + index + " 等待超时！");
                        return null;
                    }
                    System.out.println("Client #" + index + " 进入");
                    Thread.sleep((long) (3000 * Math.random()));
                    barrier.leave();
                    System.out.println("Client #" + index + " 结束");
                    return null;
                }
            };
            service.submit(task);
        }
        service.shutdown();
        service.awaitTermination(10, TimeUnit.MINUTES);
        client.close();
        System.out.println("OK!");
    }

}
