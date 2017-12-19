package de.predic8.kubernetesclient.client;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class InClusterApiClient extends LoggingApiClient {
    public static File getTokenFile() {
        return new File("/var/run/secrets/kubernetes.io/serviceaccount/token");
    }

    @PostConstruct
    public void init() {
        setBasePath("https://kubernetes.default.svc.cluster.local");

        try {
            String ca = Files.asCharSource(new File("/var/run/secrets/kubernetes.io/serviceaccount/ca.crt"), Charsets.UTF_8).read();

            setupTLS(ca, null, null);

            setApiKeyPrefix("Bearer");
            String token = Files.asCharSource(new File("/var/run/secrets/kubernetes.io/serviceaccount/token"), Charsets.UTF_8).read();
            setApiKey(token);

        } catch (IOException | KeyStoreException | KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException | CertificateException e) {
            throw new RuntimeException(e);
        }

        super.init();
    }

    public String getMyNamespace() {
        try {
            return Files.asCharSource(new File("/var/run/secrets/kubernetes.io/serviceaccount/namespace"), Charsets.UTF_8).read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
