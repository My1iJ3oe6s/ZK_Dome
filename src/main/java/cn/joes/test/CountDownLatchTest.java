package cn.joes.test;

import java.util.concurrent.CountDownLatch;

/**
 * Created by myijoes on 2018/12/3.
 *
 * CountDownLatch: 实现类似计数器的功能(表示当两个任务同时完成时继续往下执行)
 *      await() //调用await()方法的线程会被挂起，它会等待直到count值为0才继续执行
 *      await(long timeout, TimeUnit unit) //和await()类似，只不过等待一定的时间后count值还没变为0的话就会继续执行
 *      countDown() //将count值减1
 *
 * @author wanqiao
 *
 * 参考地址: http://www.importnew.com/21889.html
 */
public class CountDownLatchTest {


    public static void main(String[] args) {
        final CountDownLatch latch = new CountDownLatch(2);

        new Thread() {
            @Override
            public void run() {
                try {
                    System.out.println("子线程" + Thread.currentThread().getName() + "正在执行");
                    Thread.sleep(3000);
                    System.out.println("子线程" + Thread.currentThread().getName() + "执行完毕");
                    latch.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            ;
        }.start();

        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                    System.out.println("子线程" + Thread.currentThread().getName() + "正在执行");
                    Thread.sleep(2900);
                    System.out.println("子线程" + Thread.currentThread().getName() + "执行完毕");
                    latch.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            ;
        }.start();

        try {
            System.out.println("等待2个子线程执行完毕...");
            latch.await();
            System.out.println("2个子线程已经执行完毕");
            System.out.println("继续执行主线程");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
