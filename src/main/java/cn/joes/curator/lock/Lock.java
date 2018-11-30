package cn.joes.curator.lock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.RetryNTimes;

import java.util.concurrent.TimeUnit;

/**
 * Created by myijoes on 2018/11/30.
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
                new RetryNTimes(100, 500000)
        );
        client.start();
        System.out.println("zk client start successfully!");

        Thread t1 = new Thread(() -> {
            doWithLock(client);
        }, "t1");
       /* Thread t2 = new Thread(() -> {
            doWithLock(client);
        }, "t2");*/

        t1.start();
        //t2.start();
    }


    /**
     * InterProcessMutex：分布式可重入排它锁
     * InterProcessSemaphoreMutex：分布式排它锁
     * InterProcessReadWriteLock：分布式读写锁
     * InterProcessMultiLock：将多个锁作为单个实体管理的容器
     *
     * 分布式锁参考理解文章: https://www.jianshu.com/p/6618471f6e75
     *
     * @param client
     */
    private static void doWithLock(CuratorFramework client) {
        InterProcessMutex lock = new InterProcessMutex(client, ZK_LOCK_PATH);
        try {
            if (lock.acquire(120 * 1000, TimeUnit.SECONDS)) {
                System.out.println(Thread.currentThread().getName() + " hold lock");
                Thread.sleep(500000L);
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
