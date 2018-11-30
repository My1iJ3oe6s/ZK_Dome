package cn.joes.curator.client;

import cn.joes.dome.test.StandaloneZKServer;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.io.File;
import java.util.Date;
import java.util.concurrent.*;

/**
 * Created by myijoes on 2018/11/30.
 *
 * @author wanqiao
 */
public class ZookeeperClient {

    /**
     * Zookeeper info
     */
    private static final String ZK_ADDRESS = "127.0.0.1:2181";

    private static final String ZK_PATH = "/zktest" +  new Date().toString();

    private static final ThreadFactory namedThreadFactory =
            new ThreadFactoryBuilder().setNameFormat("Zookeeper-Embed-Server").setDaemon(true).build();

    private static final ExecutorService singleThreadPool =
            new ThreadPoolExecutor(
                    1,
                    1,
                    0L,
                    TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(1),
                    namedThreadFactory,
                    new ThreadPoolExecutor.AbortPolicy());

    public static void main(String[] args) throws Exception {
        //ZookeeperClient.startServer();

        CuratorFramework connection = ZookeeperClient.connection();

        connection.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/test/1", "test1".getBytes());

        //API使用
        api(connection);

        //监听器
        watch(connection);

    }

    /**
     *
     */
    public static void api(CuratorFramework connection) throws Exception {
        try {

            //保护机制
            connection.create().creatingParentsIfNeeded().withProtection().forPath(ZK_PATH, "test1".getBytes());

            //默认持久化
            connection.create().creatingParentsIfNeeded().forPath(ZK_PATH+1, "test1".getBytes());

            //持久化
            connection.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(ZK_PATH+2, "test1".getBytes());

            //短暂 + 有序
            connection.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(ZK_PATH+3, "test1".getBytes());

            //有序 + 持久化
            connection.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(ZK_PATH+4, "test1".getBytes());

            //短暂
            connection.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(ZK_PATH+5, "test1".getBytes());

            //修改
            connection.setData().forPath(ZK_PATH, "new data .".getBytes());

            //删除
            connection.delete().forPath(ZK_PATH);

            //获取所有子节点
            connection.getChildren().forPath(ZK_PATH);

            Stat stat = connection.checkExists().forPath(ZK_PATH);

            //监听器
            watch(connection);

        } catch (Exception e) {
            throw new Exception("创建异常");
        }
        connection.close();
    }

    /**
     *
     */
    public static void watch(CuratorFramework connection) throws Exception {


        // 2.Register watcher
        PathChildrenCache watcher = new PathChildrenCache(
                connection,
                "/test",
                true
                // if cache data
        );
        watcher.getListenable().addListener((client1, event) -> {
            ChildData data = event.getData();
            if (data == null) {
                System.out.println("No data in event[" + event + "]");
            } else {
                System.out.println("Receive event: "
                        + "type=[" + event.getType() + "]"
                        + ", path=[" + data.getPath() + "]"
                        + ", data=[" + new String(data.getData()) + "]"
                        + ", stat=[" + data.getStat() + "]");
            }
        });

        watcher.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        System.out.println("Register zk watcher successfully!");

        Thread.sleep(Integer.MAX_VALUE);
    }



    /**
     * 启动服务器
     */
    public static void startServer() {
        singleThreadPool.submit(
                () -> {
                    StandaloneZKServer.startStandaloneServer1(
                            "2000",
                            new File(System.getProperty("java.io.tmpdir"), "zookeeper").getAbsolutePath(),
                            "2181",
                            "10",
                            "5");
                });
    }

    /**
     * 客户端的连接
     */
    public static CuratorFramework connection() {
        CuratorFramework client = CuratorFrameworkFactory.newClient(
                ZK_ADDRESS,
                new RetryNTimes(10, 5000)
        );
        client.start();
        System.out.println("zk client start successfully!");
        return client;
    }
}
