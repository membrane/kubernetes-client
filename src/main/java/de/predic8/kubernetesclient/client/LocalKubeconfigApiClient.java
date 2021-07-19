package de.predic8.kubernetesclient.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import de.predic8.kubernetesclient.Kubeconfig;
import okhttp3.Request;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Optional;

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

            String ca = getReferencedFile(baseDir, config.getCluster().certificateAuthority);

            String cert = null, key = null;
            Kubeconfig.User user = config.getUser();
            if (user != null) {
                findToken(user).ifPresent(token -> setHttpClient(getHttpClient().newBuilder().addInterceptor(chain -> {
                    Request request = chain.request().newBuilder().header("Authorization", "Bearer " + token).build();
                    return chain.proceed(request);
                }).build()));

                cert = getReferencedFile(baseDir, user.clientCertificate);
                key = getReferencedFile(baseDir, user.clientKey);
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

    private Optional<String> findToken(Kubeconfig.User user) {
        if (user.token != null) {
            return Optional.of(user.token);
        }
        if (user.authProvider != null && user.authProvider.config != null && user.authProvider.config.idToken != null) {
            return Optional.of(user.authProvider.config.idToken);
        }
        return Optional.empty();
    }

    private String getReferencedFile(File baseDir, String filePath) throws IOException {
        if (filePath == null)
            return null;
        File file = new File(filePath);
        File absolutePath = file.isAbsolute() ? file : new File(baseDir, filePath);
        return Files.asCharSource(absolutePath, Charsets.UTF_8).read();
    }

    public String getMyNamespace() {
        if (namespace == null || namespace.equals(""))
            return "default";
        return namespace;
    }

}
