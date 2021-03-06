package de.predic8.kubernetesclient.genericapi;

import io.kubernetes.client.openapi.ApiException;

public interface Watcher<T> {
    void eventReceived(Action action, T resource);

    /**
     * Run when the watcher finally closes.
     *
     * @param cause What caused the watcher to be closed. Null means normal close.
     */
    void onClose(ApiException cause);

    enum Action {
        ADDED, MODIFIED, DELETED, ERROR
    }

}
