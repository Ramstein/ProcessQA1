package net.gotev.speechdemo;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ThreadSleep {


    static void sleep(long second) {
        try {
            SECONDS.sleep(second);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
