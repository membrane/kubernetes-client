package de.predic8.kubernetesclient;

import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.Call;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1Endpoints;
import org.springframework.beans.factory.annotation.Autowired;

public class KubeUtil {

    @Autowired
    ApiClient apiClient;

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
}
