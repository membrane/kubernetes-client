package de.predic8.kubernetesclient.util;

import com.squareup.okhttp.Call;
import de.predic8.kubernetesclient.util.ApiExceptionParser;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
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
