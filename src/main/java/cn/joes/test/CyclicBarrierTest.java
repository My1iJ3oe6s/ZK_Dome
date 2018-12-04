package cn.joes.test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by myijoes on 2018/12/3.
 *
 * CyclicBarrier: 回环栅栏 (通过它可以实现让一组线程等待至某个状态之后再全部同时执行。叫做回环是因为当所有等待线程都被释放以后，CyclicBarrier可以被重用。我们暂且把这个状态就叫做barrier，当调用await()方法之后，线程就处于barrier了。
 *   API:
 *      public CyclicBarrier(int parties, Runnable barrierAction) {}
 *      public CyclicBarrier(int parties) {}
 *      参数parties指让多少个线程或者任务等待至barrier状态；参数barrierAction为当这些线程都达到barrier状态时会执行的内容。
 *
 *      public int await() throws InterruptedException, BrokenBarrierException { };
 *      public int await(long timeout, TimeUnit unit)throws InterruptedException,BrokenBarrierException,TimeoutException { };
 *      第一个版本比较常用，用来挂起当前线程，直至所有线程都到达barrier状态再同时执行后续任务；
 *      第二个版本是让这些线程等待至一定的时间，如果还有线程没有到达barrier状态就直接让到达barrier的线程执行后续任务。
 *
 * @author wanqiao
 *
 *  参考地址: http://www.importnew.com/21889.html
 */
public class CyclicBarrierTest {
    public static void main(String[] args) {
        int N = 4;
        CyclicBarrier barrier  = new CyclicBarrier(N);

        //该构造方法会在4个任务执行完毕的时候随机选择一个线程取执行Runnable里面的额外任务
        CyclicBarrier barrier1  = new CyclicBarrier(N,new Runnable() {
            @Override
            public void run() {
                System.out.println("当前线程"+Thread.currentThread().getName());
            }
        });

        for(int i=0;i<N;i++) {
            //new Writer(barrier).start();
            new Writer(barrier1).start();
        }
        System.out.println("主线程后续操作继续执行....");

        try {
            Thread.sleep(25000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //当barrier在使用完毕的情况下时可以重新启用的额
        System.out.println("CyclicBarrier重用");

        for(int i=0;i<N;i++) {
            new Writer(barrier).start();
        }
    }
    static class Writer extends Thread{
        private CyclicBarrier cyclicBarrier;
        public Writer(CyclicBarrier cyclicBarrier) {
            this.cyclicBarrier = cyclicBarrier;
        }

        @Override
        public void run() {
            System.out.println("线程"+Thread.currentThread().getName()+"正在写入数据...");
            try {
                //以睡眠来模拟写入数据操作
                Thread.sleep(3000);
                System.out.println("线程"+Thread.currentThread().getName()+"写入数据完毕，等待其他线程写入完毕");
                //cyclicBarrier.await();
                //当4个任务只执行3个 在等待另一个的时候超过下面的等待时间将出现异常,默认情况下会一直等待,不出现异常
                cyclicBarrier.await(2000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }catch(BrokenBarrierException e){
                e.printStackTrace();
            }catch (TimeoutException e) {
                e.printStackTrace();
            }
            System.out.println("所有线程写入完毕，继续处理其他任务...");
        }
    }
}
