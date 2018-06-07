package com.keyeswest.trackme.utilities;

import junit.framework.Assert;


public abstract class PollingCheck {

    private static final long TIME_SLICE = 50;
    private long mTimeout;

    PollingCheck(long timeout) {
        mTimeout = timeout;
    }

    protected abstract boolean check();

    public void run() {
        if (check()) {
            return;
        }

        long timeout = mTimeout;
        while (timeout > 0) {
            try {
                Thread.sleep(TIME_SLICE);
            } catch (InterruptedException e) {
                Assert.fail("Notification error, unexpected InterruptedException");
            }

            if (check()) {
                return;
            }

            timeout -= TIME_SLICE;
        }

        Assert.fail("Notification not set, unexpected timeout");
    }
}
