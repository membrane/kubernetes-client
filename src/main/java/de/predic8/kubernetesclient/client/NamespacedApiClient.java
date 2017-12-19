package de.predic8.kubernetesclient.client;

import io.kubernetes.client.ApiClient;

public abstract class NamespacedApiClient extends ApiClient {

    /**
     * @return "default" (overrideable using the "kubernetes.client.namespace" Spring property) if running outside of the cluster,
     * the pod's own namespace, if running on the inside.
     */
    public abstract String getMyNamespace();

}
