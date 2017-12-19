package de.predic8.kubernetesclient.util;

import com.squareup.okhttp.Call;
import de.predic8.kubernetesclient.util.ApiExceptionParser;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.ApiextensionsV1beta1Api;
import io.kubernetes.client.models.V1beta1CustomResourceDefinition;
import io.kubernetes.client.models.V1beta1CustomResourceDefinitionCondition;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class KubeUtil {
    private static final Logger LOG = Logger.getLogger(KubeUtil.class);

    @Autowired
    ApiClient apiClient;

    @Autowired
    ApiextensionsV1beta1Api api;

    @Autowired
    ApiExceptionParser apiExceptionParser;

    public <T> T ifExists(Call c, Class<T> clazz) throws ApiException {
        try {
            return (T) apiClient.execute(c, clazz).getData();
        } catch (ApiException e) {
            if ("NotFound".equals(apiExceptionParser.getReason(e)))
                return null;
            throw e;
        }
    }

    public void createCRDAndWaitUntilEstablished(V1beta1CustomResourceDefinition crd) {
        try {
            api.createCustomResourceDefinition(crd, null);
        } catch (ApiException e) {
            handleExistsException(e);
        }

        // wait for 'Established' condition
        // .status.conditions[?(@.type=="Established")].status == 'True'
        OUTER:
        while (true) {
            V1beta1CustomResourceDefinition crd2 = null;
            try {
                crd2 = api.readCustomResourceDefinition(crd.getMetadata().getName(), null, null, null);
            } catch (ApiException e) {
                e.printStackTrace();
            }
            for (V1beta1CustomResourceDefinitionCondition condition : crd2.getStatus().getConditions()) {
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
