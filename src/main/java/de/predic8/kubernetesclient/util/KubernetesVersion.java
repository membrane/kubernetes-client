package de.predic8.kubernetesclient.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.predic8.kubernetesclient.CustomCoreV1Api;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Pair;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniel Bonnauer(bonnauer@predic8.de), Predic8 GmbH on 04.07.17
 */

public class KubernetesVersion {

    @Autowired
    ApiClient kc;

    @Autowired
    CustomCoreV1Api core;

    @Autowired
    ObjectMapper om;

    private int major, minor;

    @PostConstruct
    public void init() throws IOException {
        HashMap<String, String> headers = new HashMap<>();
        String[] localVarAuthNames = new String[] { "BearerToken" };
        List<Pair> queryParams = new ArrayList<>();
        kc.updateParamsForAuth(localVarAuthNames, queryParams, headers, new HashMap<>());

        Request.Builder builder = new Request.Builder().url(kc.getBasePath() + "/version");
        headers.forEach(builder::addHeader);
        Call call = kc.getHttpClient().newCall(builder.get().build());

        ResponseBody res = call.execute().body();
        assert res != null;
        Map<String, Object> version = om.readValue(res.byteStream(), new TypeReference<Map<String, Object>>(){});
        if ("Failure".equals(version.get("status")))
            throw new RuntimeException("/version returned Failure. Message: " + version.getOrDefault("message", "") + ", Reason: " + version.getOrDefault("reason", ""));

        major = Integer.parseInt((String) version.get("major"));
        minor = Integer.parseInt((String) version.get("minor"));
        res.close();
    }

    public boolean supportsCRD() {
        return major == 1 && minor >= 7 || major >= 2;
    }
}
