package cn.joes.curator.leader.LeaderSelector;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by myijoes on 2018/12/3.
 * <p>
 * Curator还提供了另外一种选举方法。与Leader latch不同的是这种方法可以对领导权进行控制，在适当的时候释放领导权，这样每个节点都有可能获得领导权。主要涉及以下四个类：
 * LeaderSelector - 选举Leader的角色。
 * LeaderSelectorListener - 选举Leader时的事件监听。
 * LeaderSelectorListenerAdapter - 选举Leader时的事件监听，官方提供的适配器，用于用户扩展。
 * CancelLeadershipException - 取消Leader权异常
 * <p>
 * 类似LeaderLatch,必须start: leaderSelector.start();一旦启动，当实例取得领导权时LeaderSelectorListener的takeLeadership()方法被调用。
 * 而takeLeadership()方法执行完毕时领导权会自动释放重新选举。当你不再使用LeaderSelector实例时，应该调用它的close方法。LeaderSelector类中也有
 * hasLeadership()、getLeader()方法。
 * <p>
 * 异常处理
 * LeaderSelectorListener类继承ConnectionStateListener。LeaderSelector必须小心连接状态的改变。如果实例成为leader,它应该相应SUSPENDED 或 LOST。
 * 当 SUSPENDED 状态出现时，实例必须假定在重新连接成功之前它可能不再是leader了。如果LOST状态出现，实例不再是leader，takeLeadership方法返回.
 * <p>
 * 注意: 推荐处理方式是当收到SUSPENDED 或 LOST时抛出CancelLeadershipException异常. 这会导致LeaderSelector实例中断并取消执行takeLeadership方法的异常。
 * 这非常重要，你必须考虑扩展LeaderSelectorListenerAdapter。LeaderSelectorListenerAdapter提供了推荐的处理逻辑。
 *
 * 可以在takeLeadership方法中进行任务的分配等业务处理，并且不要返回(一返回就会释放Leader权)，如果你想要要此实例一直是leader的话可以加一个死循环。
 * leaderSelector.autoRequeue();保证在此实例释放领导权之后还可能获得领导权。在这里我们使用AtomicInteger来记录此client获得领导权的次数，每个client有平等的机会获得领导权。
 *
 * @author wanqiao
 */
public class LeaderSelectorExample extends LeaderSelectorListenerAdapter implements Closeable {

    private final String name;
    private final LeaderSelector leaderSelector;
    private final AtomicInteger leaderCount = new AtomicInteger();

    public LeaderSelectorExample(CuratorFramework client, String path, String name) {
        this.name = name;
        leaderSelector = new LeaderSelector(client, path, this);
        //保证在此实例释放领导权之后还可能获得领导权。
        leaderSelector.autoRequeue();
    }

    public void start() throws IOException {
        leaderSelector.start();
    }

    @Override
    public void close() throws IOException {
        leaderSelector.close();
    }

    @Override
    public void takeLeadership(CuratorFramework client) throws Exception {
        final int waitSeconds = 1;
        System.out.println(name + " 是当前的leader(" + leaderSelector.hasLeadership() + ") 等待" + waitSeconds + "秒...");
        System.out.println(name + " 之前成为leader的次数：" + leaderCount.getAndIncrement() + "次");
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(waitSeconds));
        } catch (InterruptedException e) {
            System.err.println(name + " 已被中断");
            Thread.currentThread().interrupt();
        } finally {
            System.out.println(name + " 放弃leader\n");
        }
    }
}
