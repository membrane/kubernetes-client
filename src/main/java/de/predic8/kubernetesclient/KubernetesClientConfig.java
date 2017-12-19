package de.predic8.kubernetesclient;

import de.predic8.kubernetesclient.client.InClusterApiClient;
import de.predic8.kubernetesclient.client.LocalKubeconfigApiClient;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.apis.ApiextensionsV1beta1Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class KubernetesClientConfig {
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
    public CustomCoreV1Api customCoreV1Api(@Autowired ApiClient apiClient) {
        return new CustomCoreV1Api(apiClient);
    }

    @Bean
    public ApiextensionsV1beta1Api apiextensionsV1beta1Api(@Autowired ApiClient apiClient) {
        return new ApiextensionsV1beta1Api(apiClient);
    }

    @Bean
    public ApiExceptionParser apiExceptionParser() {
        return new ApiExceptionParser();
    }

    @Bean
    public KubeUtil kubeUtil() {
        return new KubeUtil();
    }
}
