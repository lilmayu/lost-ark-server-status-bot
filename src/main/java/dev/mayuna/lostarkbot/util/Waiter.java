package dev.mayuna.lostarkbot.util;

import dev.mayuna.lostarkbot.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.CountDownLatch;

public class Waiter<T> {

    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private @Getter @Setter T object;

    public Waiter() {
    }

    public Waiter(T object) {
        this.object = object;
    }

    public void proceed() {
        countDownLatch.countDown();
    }

    public void await() {
        try {
            countDownLatch.await();
        } catch (InterruptedException exception) {
            exception.printStackTrace();
            Logger.error("Interrupted Exception while awaiting in Waiter!");
        }
    }
}
