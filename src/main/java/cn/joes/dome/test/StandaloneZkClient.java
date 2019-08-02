package cn.joes.dome.test;

import org.apache.log4j.Logger;
import org.apache.zookeeper.*;

public class StandaloneZkClient {
	
	private static final Logger logger = Logger.getLogger(StandaloneZkClient.class);

	public static void main(String[] args) throws Exception {
		String connectString = "erp-g2.staging.pagoda.com.cn:2083";
		//String connectString = "127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183";
        ZooKeeper zk = new ZooKeeper(connectString, 10000, new Watcher() {
            @Override
			public void process(WatchedEvent event) {
				logger.info("Zk event: [" + event.toString() + "]");
			}
        });
   
        System.out.println(zk.getState());
        logger.info("Zk Status: " + zk.getState());
        
      /*  zk.create("/nodes", "节点集合".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        zk.create("/nodes/persistent", "持久节点".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        zk.create("/nodes/persistent_sequential1", "持久顺序节点1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
        zk.create("/nodes/persistent_sequential2", "持久顺序节点2".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
        zk.create("/nodes/ephemeral", "临时节点".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        zk.create("/nodes/ephemeral_sequential1", "临时顺序节点1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        zk.create("/nodes/ephemeral_sequential2", "临时顺序节点2".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
   */
       /* zk.setData("/nodes/persistent", "改变持久节点".getBytes(), -1);*/
        
        for (String child : zk.getChildren("/datasync", true)) {
        	logger.info("Zk nodes: [" + "/nodes/" + child + ": " + new String(zk.getData("/datasync/" + child, true, null)) + "]");
        	//zk.delete("/nodes/" + child, -1);
        }
        
        //zk.delete("/nodes", -1);


        zk.close();
    }
	
}
