package cn.joes.test;

import java.util.concurrent.Semaphore;

/**
 * Created by myijoes on 2018/12/3.
 *
 * Semaphore : 信号量 (Semaphore可以控同时访问的线程个数，通过 acquire() 获取一个许可，如果没有就等待，而 release() 释放一个许可。)
 *
 *  API: tryAcquire() //尝试获取一个许可，若获取成功，则立即返回true，若获取失败，则立即返回false
 *       tryAcquire(long timeout, TimeUnit unit) //尝试获取一个许可，若在指定的时间内获取成功，则立即返回true，否则则立即返回false
 *       tryAcquire(int permits)  //尝试获取permits个许可，若获取成功，则立即返回true，若获取失败，则立即返回false
 *       tryAcquire(int permits, long timeout, TimeUnit unit)//尝试获取permits个许可，若在指定的时间内获取成功，则立即返回true，否则则立即返回false

 * @author wanqiao
 */
public class SemaphoreTest {

    public static void main(String[] args) {
        //工人数
        int N = 8;
        //机器数目
        Semaphore semaphore = new Semaphore(5);
        for(int i=0;i<N;i++){
            new Worker(i,semaphore).start();
        }
    }

    static class Worker extends Thread{
        private int num;
        private Semaphore semaphore;
        public Worker(int num,Semaphore semaphore){
            this.num = num;
            this.semaphore = semaphore;
        }

        @Override
        public void run() {
            try {
                semaphore.acquire();
                System.out.println("工人"+this.num+"占用一个机器在生产...");
                Thread.sleep(2000);
                System.out.println("工人"+this.num+"释放出机器");
                semaphore.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
