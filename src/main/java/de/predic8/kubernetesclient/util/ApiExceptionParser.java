package de.predic8.kubernetesclient.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.ApiException;

import java.io.IOException;
import java.util.Map;

public class ApiExceptionParser {
    public String getReason(ApiException e) throws ApiException {
        String body = e.getResponseBody();
        if (!body.startsWith("{"))
            throw e;
        try {
            return (String) new ObjectMapper().readValue(body, Map.class).get("reason");
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
    }
}
