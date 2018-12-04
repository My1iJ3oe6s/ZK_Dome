package cn.joes.test;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;

/**
 * @author wanqiao
 */
public class ConcurrentTest {

    public static void main(String[] args) {
        final CountDownLatch down = new CountDownLatch(1);

        for (int i = 0; i < 10; i++) {

            new Order(down, i).start();

        }
        System.out.println("123");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        down.countDown();
        System.out.println("等待完毕: ");
    }

    static class Order extends Thread{
        private CountDownLatch down;
        private Integer threadNumber;

        public Order(CountDownLatch down, Integer threadNumber) {
            this.down = down;
            this.threadNumber = threadNumber;
        }

        @Override
        public void run() {
            try {
                System.out.println("开始等到创建订单权限: ");
                down.await();
                System.out.println("创建订单号: ");
            } catch (Exception e) {

            }

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss|SSS");

            String orderNo = sdf.format(new Date());

            System.err.println("生成的订单号是：" + orderNo);
        }
    }
}
