package cn.joes.curator.leader.LeaderLatch;

import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by myijoes on 2018/12/3.
 *
 * 选举:
 *
 * 异常: LeaderLatch实例可以增加ConnectionStateListener来监听网络连接问题。
 *      当 SUSPENDED 或 LOST 时,leader不再认为自己还是leader.当LOST 连接重连后 RECONNECTED,LeaderLatch会删除先前的ZNode然后重新创建一个.
 *
 *      LeaderLatch用户必须考虑导致leadershi丢失的连接问题。强烈推荐你使用ConnectionStateListener。
 *
 * @author wanqiao
 */
public class Leader {

    private static final int CLIENT_QTY = 10;

    private static final String PATH = "/examples/leader";

    public static void main(String[] args) throws Exception {
        List<CuratorFramework> clients = Lists.newArrayList();
        List<LeaderLatch> examples = Lists.newArrayList();
        try {
            for (int i = 0; i < CLIENT_QTY; ++i) {
                CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", new ExponentialBackoffRetry(1000, 3));
                clients.add(client);
                client.start();
                LeaderLatch example = new LeaderLatch(client, PATH, "Client #" + i);
                examples.add(example);
                //必须启动LeaderLatch: leaderLatch.start();一旦启动，LeaderLatch会和其它使用相同latch path的其它LeaderLatch交涉，然后随机的选择其中一个作为leader
                example.start();
            }
            System.out.println("LeaderLatch初始化完成！");
            Thread.sleep(10 * 1000);// 等待Leader选举完成
            LeaderLatch currentLeader = null;
            for (int i = 0; i < CLIENT_QTY; ++i) {
                LeaderLatch example = examples.get(i);
                //hasLeadership()判断当前实例是否为leader
                //LeaderLatch.hasLeadership()与LeaderLatch.getLeader()得到的结果不一定一致，需要通过LeaderLatch.getLeader().isLeader()来判断。
                if (example.hasLeadership()) {
                    currentLeader = example;
                }
            }
            System.out.println("当前leader：" + currentLeader.getId());
            currentLeader.close();
            LeaderLatch leaderLatch = examples.get(0);
            System.out.println("当前实例为:" + leaderLatch.getId());
            // await是一个阻塞方法， 尝试获取leader地位，但是未必能上位
            examples.get(0).await(10, TimeUnit.SECONDS);
            System.out.println("当前leader：" + examples.get(0).getLeader());
            System.out.println("输入回车退出");
            new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            for (LeaderLatch exampleClient : examples) {
                System.out.println("当前leader：" + exampleClient.getLeader());
                try {
                    CloseableUtils.closeQuietly(exampleClient);
                } catch (Exception e) {
                    System.out.println(exampleClient.getId() + " -- " + e.getMessage());
                }
            }
            for (CuratorFramework client : clients) {
                CloseableUtils.closeQuietly(client);
            }
        }
        System.out.println("OK!");
    }

}
