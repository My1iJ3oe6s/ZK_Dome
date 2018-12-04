package cn.joes.curator.lock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.RetryNTimes;

import java.util.concurrent.TimeUnit;

/**
 * Created by myijoes on 2018/11/30.
 *
 * 参考地址: https://www.cnblogs.com/seaspring/p/5536338.html
 *         https://www.jianshu.com/p/6618471f6e75
 *         https://blog.csdn.net/en_joker/article/details/78789891
 *
 * Curator锁的实现包括:
 *      Shared Reentrant Lock - 全功能的分布式锁。任何一刻不会有两个client同时拥有锁
 *      Shared Lock - 与Shared Reentrant Lock类似但是不是重入的
 *      Shared Reentrant Read Write Lock - 类似Java的读写锁，但是是分布式的
 *      Shared Semaphore - 跨JVM的计数信号量
 *      Multi Shared Lock - 将多个锁看成整体，要不全部acquire成功，要不acquire全部失败。release也是释放全部锁
 *
 * @author wanqiao
 */
public class Lock {

    /** Zookeeper info */
    private static final String ZK_ADDRESS = "127.0.0.1:2181";

    private static final String ZK_LOCK_PATH = "/localLock";

    public static void main(String[] args) throws InterruptedException {
        // 1.Connect to zk
        CuratorFramework client = CuratorFrameworkFactory.newClient(
                ZK_ADDRESS,
                // 重试策略
                // 初始休眠时间为 1000ms, 最大重试次数为 3
                new RetryNTimes(10, 5000)
        );
        client.start();
        System.out.println("zk client start successfully!");

        Thread t1 = new Thread(() -> {
            doWithLock(client);
        }, "t1");
        Thread t2 = new Thread(() -> {
            doWithLock(client);
        }, "t2");

        t1.start();
        t2.start();
    }


    /**
     * InterProcessMutex：分布式可重入排它锁
     * InterProcessSemaphoreMutex：分布式排它锁
     * InterProcessReadWriteLock：分布式读写锁
     * InterProcessMultiLock：将多个锁作为单个实体管理的容器
     *
     * 分布式锁参考理解文章: https://www.jianshu.com/p/6618471f6e75
     *                  http://www.cnblogs.com/LiZhiW/p/4931577.html
     *
     * @param client
     */
    private static void doWithLock(CuratorFramework client) {
        InterProcessMutex lock = new InterProcessMutex(client, ZK_LOCK_PATH);
        try {
            if (lock.acquire(10 * 1000, TimeUnit.SECONDS)) {
                System.out.println(Thread.currentThread().getName() + " hold lock");
                Thread.sleep(5000L);
                System.out.println(Thread.currentThread().getName() + " release lock");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                lock.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
