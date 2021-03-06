package de.predic8.kubernetesclient;

import com.google.gson.reflect.TypeToken;
import io.kubernetes.client.*;
import io.kubernetes.client.openapi.*;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.openapi.models.V1Pod;
import okhttp3.Call;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CustomCoreV1Api extends CoreV1Api {

    private ApiClient apiClient;

    public CustomCoreV1Api(ApiClient apiClient) {
        super(apiClient);
        this.apiClient = apiClient;
    }

    public V1Pod deleteNamespacedPod2(String name, String namespace, V1DeleteOptions body, String pretty, Integer gracePeriodSeconds, Boolean orphanDependents, String propagationPolicy) throws ApiException {
        ApiResponse<V1Pod> resp = deleteNamespacedPodWithHttpInfo2(name, namespace, body, pretty, gracePeriodSeconds, orphanDependents, propagationPolicy);
        return resp.getData();
    }

    public ApiResponse<V1Pod> deleteNamespacedPodWithHttpInfo2(String name, String namespace, V1DeleteOptions body, String pretty, Integer gracePeriodSeconds, Boolean orphanDependents, String propagationPolicy) throws ApiException {
        Call call = deleteNamespacedPodValidateBeforeCall2(name, namespace, body, pretty, gracePeriodSeconds, orphanDependents, propagationPolicy, null);
        Type localVarReturnType = new TypeToken<V1Pod>(){}.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    private Call deleteNamespacedPodValidateBeforeCall2(String name, String namespace, V1DeleteOptions body, String pretty, Integer gracePeriodSeconds, Boolean orphanDependents, String propagationPolicy, final ApiCallback apiCallback) throws ApiException {

        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException("Missing the required parameter 'name' when calling deleteNamespacedPod(Async)");
        }

        // verify the required parameter 'namespace' is set
        if (namespace == null) {
            throw new ApiException("Missing the required parameter 'namespace' when calling deleteNamespacedPod(Async)");
        }

        // verify the required parameter 'body' is set
        if (body == null) {
            throw new ApiException("Missing the required parameter 'body' when calling deleteNamespacedPod(Async)");
        }


        Call call = deleteNamespacedPodCall(name, namespace, pretty, null, gracePeriodSeconds, orphanDependents, propagationPolicy, body, apiCallback);
        return call;
    }


}
