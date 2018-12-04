package cn.joes.curator.leader.LeaderSelector;

import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by myijoes on 2018/12/3.
 *
 * @author wanqiao
 */
public class LeaderSelectorExampleTest {

    private static final int CLIENT_QTY = 10;

    private static final String PATH = "/examples/leader";

    public static void main(String[] args) throws Exception {
        List<CuratorFramework> clients = Lists.newArrayList();
        List<LeaderSelectorExample> examples = Lists.newArrayList();
        try {
            for (int i = 0; i < CLIENT_QTY; ++i) {
                CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", new ExponentialBackoffRetry(1000, 3));
                clients.add(client);
                client.start();
                LeaderSelectorExample example = new LeaderSelectorExample(client, PATH, "Client #" + i);
                examples.add(example);
                example.start();
            }
            System.out.println("输入回车退出：");
            new BufferedReader(new InputStreamReader(System.in)).readLine();
        } finally {
            for (LeaderSelectorExample exampleClient : examples) {
                CloseableUtils.closeQuietly(exampleClient);
            }
            for (CuratorFramework client : clients) {
                CloseableUtils.closeQuietly(client);
            }
        }
        System.out.println("OK!");
    }

}
