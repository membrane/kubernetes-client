package de.predic8.kubernetesclient.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.ResponseBody;
import de.predic8.kubernetesclient.CustomCoreV1Api;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

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
        Map<String, String> headers = new HashMap<>();
        String[] localVarAuthNames = new String[] { "BearerToken" };
        List<Pair> queryParams = new ArrayList<>();
        kc.updateParamsForAuth(localVarAuthNames, queryParams, headers);

        Request.Builder builder = new Request.Builder().url(kc.getBasePath() + "/version");
        for (Map.Entry<String, String> header : headers.entrySet())
            builder.addHeader(header.getKey(), header.getValue());
        Call call = kc.getHttpClient().newCall(builder.get().build());

        ResponseBody res = call.execute().body();
        Map version = om.readValue(res.byteStream(), Map.class);
        String status = (String)version.get("status");
        if ("Failure".equals(status))
            throw new RuntimeException("/version returned " + status);

        major = Integer.parseInt((String) version.get("major"));
        minor = Integer.parseInt((String) version.get("minor"));
        res.close();
    }

    public boolean supportsCRD() {
        return major == 1 && minor >= 7 || major >= 2;
    }
}
