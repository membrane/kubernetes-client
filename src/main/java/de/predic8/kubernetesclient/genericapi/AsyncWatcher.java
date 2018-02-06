package de.predic8.kubernetesclient.genericapi;

import java.io.Closeable;
import java.io.IOException;

public class AsyncWatcher implements Closeable {
    private Thread t;
    volatile boolean closed = false;

    AsyncWatcher() {
    }

    public void setT(Thread t) {
        this.t = t;
    }

    @Override
    public void close() {
        closed = true;
        t.interrupt();
    }
}
