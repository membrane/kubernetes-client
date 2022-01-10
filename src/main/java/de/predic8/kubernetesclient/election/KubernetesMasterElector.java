package de.predic8.kubernetesclient.election;

import de.predic8.kubernetesclient.util.KubeUtil;
import de.predic8.kubernetesclient.util.KubernetesVersion;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoordinationV1Api;
import io.kubernetes.client.openapi.models.V1Lease;
import io.kubernetes.client.openapi.models.V1LeaseSpec;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class KubernetesMasterElector {

    Logger LOG = LoggerFactory.getLogger(KubernetesMasterElector.class);

    public static final String API_VERSION = "coordination.k8s.io/v1";
    public static final String KIND = "Lease";

    private CoordinationV1Api ops;

    @Value("${spring.application.name}")
    String leaseName;

    @Value("${kubernetes.client.namespace}")
    private String namespace;

    @Value("${p8.master-election.log-only:false}")
    private boolean logOnly;

    @Value("${p8.master-election.lease.duration-seconds:400}")
    private int leaseDurationSeconds;

    @Value("${p8.master-election.lease.renew-timeout:350}")
    private int leaseRenewTimeout;

    private String holderName;

    @Autowired
    KubernetesVersion kubernetesVersion;

    @Autowired
    KubeUtil kubeUtil;

    @Autowired
    ApiClient apiClient;

    @Autowired
    @Qualifier("slowApiClient")
    ApiClient slowApiClient;

    @PostConstruct
    public void init() throws UnknownHostException {
        if (!kubernetesVersion.supportsCRD()) {
            throw new RuntimeException("Not implemented.");
        }
        ops = new CoordinationV1Api(apiClient);
        //ops = new ArbitraryResourceApi<>(apiClient, slowApiClient, "predic8.de", API_VERSION, "leases");
        holderName = InetAddress.getLocalHost().getHostName();
        this.leaseName = this.leaseName.toLowerCase().replaceAll(" ", "-");

        elect();
    }

    private Runnable runnable;

    public KubernetesMasterElector(Runnable runnable) {
        this.runnable = runnable;
    }

    public V1Lease getByName() throws ApiException {
        try {
            return ops.readNamespacedLease(leaseName, namespace, null);
        }catch (ApiException e) {
            if(e.getResponseBody() != null && e.getResponseBody().contains("\"reason\":\"NotFound\"")) // todo improve
                return null;
            throw e;
        }
    }

    public void elect() {
        LOG.info("Starting election..." + System.lineSeparator()
        + " holder name: " + holderName + System.lineSeparator()
        + " lease duration/renew timeout: " + leaseDurationSeconds + "/" + leaseRenewTimeout + System.lineSeparator()
        + (logOnly ? "only logging if election won" : ""));
        while (true) {
            V1Lease oldLease = null;
            OffsetDateTime now = OffsetDateTime.now();
            try {
                oldLease = getByName();
                if (oldLease != null) {
                    V1LeaseSpec spec = oldLease.getSpec();

                    if (!spec.getHolderIdentity().equals(holderName) &&
                            !(now.isAfter(spec.getAcquireTime().plus(Duration.of(spec.getLeaseDurationSeconds(), ChronoUnit.SECONDS))))) {
                        // wait for expiration of lease
                        long timeout = oldLease.getSpec().getAcquireTime().toInstant().toEpochMilli() + Duration.of(oldLease.getSpec().getLeaseDurationSeconds(), ChronoUnit.SECONDS).toMillis() - OffsetDateTime.now().toInstant().toEpochMilli();
                        if(timeout > 0)
                            Thread.sleep(timeout);
                        continue;
                    }
                }
            } catch (ApiException e) {
                // ignore ApiExceptions
                //e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            boolean currentOwner = false;
            try {
                while (true) { // if able to claim lease, loop until error
                    V1Lease lease = leaseFromNow(namespace, holderName);
                    if (oldLease != null) {
                        lease.setMetadata(oldLease.getMetadata()); // keep resource version etc.
                        lease = ops.replaceNamespacedLease(lease.getMetadata().getName(), namespace, lease, null, null, null);
                    } else {
                        ops.createNamespacedLease(namespace, lease, null, null, null);
                    }
                    oldLease = getByName();
                    currentOwner = true;
                    if (logOnly)
                        LOG.info("election won / renewed");
                    else
                        runnable.run();

                    long timeout = lease.getSpec().getRenewTime().toInstant().toEpochMilli() - OffsetDateTime.now().toInstant().toEpochMilli();
                    if(timeout > 0)
                        Thread.sleep(timeout);
                }
            } catch (ApiException e) {
                if (currentOwner) // restart to avoid any side effects
                    System.exit(0);
                // ignore ApiExceptions
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0); // restart to avoid any side effects
            }
        }
    }

    public V1Lease leaseFromNow(String namespace, String holderName) {
        OffsetDateTime now = OffsetDateTime.now();
        return new V1Lease()
                .apiVersion(API_VERSION)
                .kind(KIND)
                .metadata(new V1ObjectMeta()
                        .name(leaseName)
                        .namespace(namespace))
                .spec(new V1LeaseSpec()
                        .holderIdentity(holderName)
                        .acquireTime(now)
                        .leaseDurationSeconds(leaseDurationSeconds)
                        .renewTime(now.plusSeconds(leaseRenewTimeout)));
    }

}
