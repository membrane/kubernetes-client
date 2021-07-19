package de.predic8.kubernetesclient.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import de.predic8.kubernetesclient.Kubeconfig;
import de.predic8.kubernetesclient.PEMSupport;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LocalKubeconfigApiClient extends LoggingApiClient {

    Logger LOG = LoggerFactory.getLogger(LocalKubeconfigApiClient.class);

    @Value("${kubernetes.client.namespace:}")
    public String namespace;

    public static File getConfigFile() {
        return new File(new File(new File(System.getProperty("user.home")), ".kube"), "config");
    }

    @PostConstruct
    public void init() {
        try {
            File baseDir = new File(new File(System.getProperty("user.home")), ".kube");

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            Kubeconfig config = mapper.readValue(new File(baseDir, "config"), Kubeconfig.class);

            String bp = config.getCluster().getServer();
            LOG.info("Using Kubernetes URL " + bp);
            setBasePath(bp);

            String ca = config.getCluster().certificateAuthority;
            if (ca != null)
                ca = Files.asCharSource(new File(baseDir, ca), Charsets.UTF_8).read();

            String cert = null, key = null;
            Kubeconfig.User user = config.getUser();
            if (user != null) {
                if (user.token != null) {
                    setHttpClient(getHttpClient().newBuilder().addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request request = chain.request().newBuilder().header("Authorization", "Bearer " + user.token).build();
                            return chain.proceed(request);
                        }
                    }).build());
                }
                if (user.authProvider != null && user.authProvider.config != null && user.authProvider.config.idToken != null) {
                    setHttpClient(getHttpClient().newBuilder().addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request request = chain.request().newBuilder().header("Authorization", "Bearer " + user.authProvider.config.idToken).build();
                            return chain.proceed(request);
                        }
                    }).build());
                }


                if (user.clientCertificate != null) {
                    cert = user.clientCertificate;
                    if (cert == null)
                        throw new NotImplementedException();
                    File file;
                    if (cert.contains(":\\"))
                        file = new File(cert);
                    else
                        file = new File(baseDir, cert);
                    cert = Files.asCharSource(file, Charsets.UTF_8).read();
                }

                if (user.clientKey != null) {
                    key = user.clientKey;
                    if (key == null)
                        throw new NotImplementedException();
                    File file;
                    if (key.contains(":\\"))
                        file = new File(key);
                    else
                        file = new File(baseDir, key);
                    key = Files.asCharSource(file, Charsets.UTF_8).read();
                }
            }

            try {
                setupTLS(ca, cert, key);
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        super.init();
    }

    public String getMyNamespace() {
        if (namespace == null || namespace.equals(""))
            return "default";
        return namespace;
    }

}
