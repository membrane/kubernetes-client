package de.predic8.kubernetesclient.util;

import de.predic8.kubernetesclient.CustomCoreV1Api;
import de.predic8.kubernetesclient.genericapi.ArbitraryResourceApi;
import de.predic8.kubernetesclient.genericapi.AsyncWatcher;
import de.predic8.kubernetesclient.genericapi.Watcher;
import de.predic8.kubernetesclient.util.ApiExceptionParser;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.ApiextensionsV1Api;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1CustomResourceDefinition;
import io.kubernetes.client.openapi.models.V1CustomResourceDefinitionCondition;
import okhttp3.Call;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class KubeUtil {
    private static final Logger LOG = LoggerFactory.getLogger(KubeUtil.class);

    @Autowired
    ApiClient apiClient;

    @Autowired
    ApiextensionsV1Api api;

    @Autowired
    ApiExceptionParser apiExceptionParser;

    @Autowired
    @Qualifier("slowApiClient")
    ApiClient slowApiClient;

    private final Object threadEvent = new Object();

    @Autowired
    CustomCoreV1Api core;

    public <T> T ifExists(Call c, Class<T> clazz) throws ApiException {
        try {
            return (T) apiClient.execute(c, clazz).getData();
        } catch (ApiException e) {
            if ("NotFound".equals(apiExceptionParser.getReason(e)))
                return null;
            throw e;
        }
    }

    public void deletePodAndWaitForItToBeDeleted(V1Pod pod) throws ApiException {
        AsyncWatcher watch = new ArbitraryResourceApi<V1Pod>(apiClient, slowApiClient, null, "v1", "pods")
            .watchAsync(pod.getMetadata().getNamespace(), null, V1Pod.class, "metadata.name="+pod.getMetadata().getName(), null, new Watcher<V1Pod>() {
                @Override
                public void eventReceived(Action action, V1Pod resource) {

                    LOG.debug("resource.getMetadata().getName() = " + resource.getMetadata().getName());
                    LOG.debug("resource.getStatus().getPhase() = " + resource.getStatus().getPhase() + " Action: " + action);

                    if (action == Action.DELETED && resource.getMetadata().getName().equals(pod.getMetadata().getName())) {
                        synchronized (threadEvent) {
                            threadEvent.notify();
                        }
                    }

                }

                @Override
                public void onClose(ApiException cause) {
                    LOG.warn("delete watcher closed");
                    if (cause != null)
                        LOG.error("watching delete failed: ", cause);
                }
            });

        core.deleteNamespacedPod2(pod.getMetadata().getName(), pod.getMetadata().getNamespace(), new V1DeleteOptions(), null, null, null, null);

        try {
            synchronized (threadEvent) {
                threadEvent.wait();
            }
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } finally {
            watch.close();
        }
    }

    public void createCRDAndWaitUntilEstablished(V1CustomResourceDefinition crd) {
        try {
            api.createCustomResourceDefinition(crd, null, null, null);
        } catch (ApiException e) {
            handleExistsException(e);
        }

        // wait for 'Established' condition
        // .status.conditions[?(@.type=="Established")].status == 'True'
        OUTER:
        while (true) {
            V1CustomResourceDefinition crd2 = null;
            try {
                crd2 = api.readCustomResourceDefinition(crd.getMetadata().getName(), null, null, null);
            } catch (ApiException e) {
                e.printStackTrace();
            }
            if (crd2.getStatus() != null && crd2.getStatus().getConditions() != null)
                for (V1CustomResourceDefinitionCondition condition : crd2.getStatus().getConditions()) {
                    if ("Established".equals(condition.getType()) && "True".equals(condition.getStatus())) {
                        break OUTER;
                    }
                }
            LOG.info("Waiting for CRD to be established...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void handleExistsException(ApiException e) {
        try {
            if (!"AlreadyExists".equals(apiExceptionParser.getReason(e)))
                throw new RuntimeException(e);
        } catch (ApiException e1) {
            throw new RuntimeException(e1);
        }

    }
}
