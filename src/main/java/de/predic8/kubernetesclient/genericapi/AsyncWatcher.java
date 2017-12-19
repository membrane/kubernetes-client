package de.predic8.kubernetesclient.genericapi;

import java.io.Closeable;
import java.io.IOException;

public class AsyncWatcher implements Closeable {
    private Thread t;

    AsyncWatcher(Thread t) {
        this.t = t;
    }

    @Override
    public void close() {
        t.interrupt();
    }
}
