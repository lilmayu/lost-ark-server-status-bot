package dev.mayuna.lostarkbot.util;

import dev.mayuna.lostarkbot.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;

import java.util.Timer;
import java.util.TimerTask;

public class SpecialRateLimiter {

    private static @Getter @Setter int maxRequests = 50;
    private static @Getter @Setter long resetRequestCountAfter = 1000;

    private static @Getter int currentRequestCount = 0;
    private static @Getter int lastRequestCount = 0;

    private static @Getter Timer timer;
    private static final Object mutex = new Object();

    public static void init() {
        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer("SpecialRateLimiter");

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                lastRequestCount = currentRequestCount;
                currentRequestCount = 0;

                synchronized (mutex) {
                    mutex.notifyAll();
                }
            }
        }, 0, resetRequestCountAfter);
    }

    public static void waitIfRateLimited() {
        if (currentRequestCount >= maxRequests) {
            try {
                synchronized (mutex) {
                    Logger.flow("RateLimited! Waiting...");
                    mutex.wait();
                }
            } catch (Exception exception) {
                Logger.get().warn("Exception while waiting rate limited!", exception);
            }
        }
    }

    public static void madeRequest() {
        currentRequestCount++;
    }
}
