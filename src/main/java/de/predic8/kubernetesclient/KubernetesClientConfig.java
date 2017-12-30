package de.predic8.kubernetesclient;

import de.predic8.kubernetesclient.client.InClusterApiClient;
import de.predic8.kubernetesclient.client.LocalKubeconfigApiClient;
import de.predic8.kubernetesclient.util.ApiExceptionParser;
import de.predic8.kubernetesclient.util.KubeUtil;
import de.predic8.kubernetesclient.util.KubernetesVersion;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.apis.ApiextensionsV1beta1Api;
import io.kubernetes.client.apis.PolicyV1beta1Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

@Configuration
public class KubernetesClientConfig {
    @Primary
    @Bean
    public ApiClient kubernetesClient() {

        if (LocalKubeconfigApiClient.getConfigFile().exists())
            return new LocalKubeconfigApiClient();

        if (InClusterApiClient.getTokenFile().exists())
            return new InClusterApiClient();

        throw new RuntimeException("Could not auto-detect the Kubernetes client configuration, as neither " +
                LocalKubeconfigApiClient.getConfigFile().getAbsolutePath() + " nor " +
                InClusterApiClient.getTokenFile().getAbsolutePath() + " exist.");
    }

    @Bean
    public ApiClient slowApiClient() {
        ApiClient ac = kubernetesClient();
        ac.getHttpClient().setReadTimeout(0, TimeUnit.MILLISECONDS);
        return ac;
    }

    @Bean
    public CustomCoreV1Api customCoreV1Api(@Autowired ApiClient apiClient) {
        return new CustomCoreV1Api(apiClient);
    }

    @Bean
    public ApiextensionsV1beta1Api apiextensionsV1beta1Api(@Autowired ApiClient apiClient) {
        return new ApiextensionsV1beta1Api(apiClient);
    }

    @Bean
    public PolicyV1beta1Api policyV1beta1Api(@Autowired ApiClient apiClient) {
        return new PolicyV1beta1Api(apiClient);
    }

    @Bean
    public ApiExceptionParser apiExceptionParser() {
        return new ApiExceptionParser();
    }

    @Bean
    public KubeUtil kubeUtil() {
        return new KubeUtil();
    }

    @Bean
    public KubernetesVersion kv() { return new KubernetesVersion(); }
}
