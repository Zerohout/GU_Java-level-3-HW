package homework;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

public class Car implements Runnable {
    private static int CARS_COUNT;
    CyclicBarrier cb;
    CountDownLatch cdl;
    Semaphore smp;
    Lock finish;

    static {
        CARS_COUNT = 0;
    }

    private Race race;
    private int speed;
    private String name;

    public String getName() {
        return name;
    }

    public int getSpeed() {
        return speed;
    }

    public Car(Race race, int speed, CyclicBarrier cb, CountDownLatch cdl, Semaphore smp, Lock finish) {
        this.race = race;
        this.speed = speed;
        this.cb = cb;
        this.cdl = cdl;
        this.smp = smp;
        this.finish = finish;
        CARS_COUNT++;
        this.name = "Участник #" + CARS_COUNT;
    }

    public Semaphore getSmp() {
        return smp;
    }

    @Override
    public void run() {
        try {
            System.out.println(this.name + " готовится");
            Thread.sleep(500 + (int) (Math.random() * 800));
            System.out.println(this.name + " готов");
            cb.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < race.getStages().size(); i++) {
            race.getStages().get(i).go(this);
        }
        if(finish.tryLock()){
            System.out.println(name + " WIN!!!");
        }
        cdl.countDown();
    }
}