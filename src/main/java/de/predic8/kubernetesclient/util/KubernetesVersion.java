package de.predic8.kubernetesclient.util;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.ResponseBody;
import de.predic8.kubernetesclient.CustomCoreV1Api;
import io.kubernetes.client.ApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by Daniel Bonnauer(bonnauer@predic8.de), Predic8 GmbH on 04.07.17
 */

@Component
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
        ResponseBody res = kc.getHttpClient().newCall(new Request.Builder().url(kc.getBasePath() + "/version").get().build()).execute().body();
        Map version = om.readValue(res.byteStream(), Map.class);

        major = Integer.parseInt((String) version.get("major"));
        minor = Integer.parseInt((String) version.get("minor"));
    }

    public boolean supportsCRD() {
        return major == 1 && minor >= 7 || major >= 2;
    }
}
