package de.predic8.kubernetesclient;

import com.google.common.io.ByteStreams;
import de.predic8.kubernetesclient.patch.PortForward;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class PortForwarder {

    private static Logger LOG = LoggerFactory.getLogger(PortForwarder.class);

    private final int sourcePort;
    private final int targetPort;
    private final String namespace;
    protected final String pod;
    private volatile ServerSocket ss;

    public PortForwarder(int sourcePort, int targetPort, String namespace, String pod) {

        this.sourcePort = sourcePort;
        this.targetPort = targetPort;
        this.namespace = namespace;
        this.pod = pod;
    }

    @Autowired
    ApiClient apiClient;

    volatile Thread connectionHandler;

    @PostConstruct
    public void start() throws IOException, ApiException {

        ss = new ServerSocket(sourcePort);

        connectionHandler = new Thread(() -> {
            try {
                while (true) {
                    ServerSocket ss2 = ss;
                    if (ss2 == null)
                        break;
                    final Socket s = ss2.accept();
                    LOG.info("Forwarding request from port " + sourcePort + " to " + pod + ":" + targetPort);

                    PortForward forward = new PortForward(apiClient);
                    try {
                        PortForward.PortForwardResult result = forward.forward(namespace, pod, Arrays.asList(targetPort));

                        new Thread(() -> {
                            try {
                                ByteStreams.copy(result.getInputStream(targetPort), s.getOutputStream());
                                try {
                                    s.getOutputStream().close();
                                } catch (IOException e) {
                                    LOG.info("ignoring " + e.getMessage());
                                }
                                LOG.info("server->client stream closed.");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }).start();

                        new Thread(() -> {
                            try {
                                try {
                                    ByteStreams.copy(s.getInputStream(), result.getOutboundStream(targetPort));
                                    s.getInputStream().close();
                                } finally {
                                    LOG.info("client->server stream closed: cancelling request");
                                    result.getListener().cancel();
                                }
                            } catch (Exception ex) {
                                if (ex instanceof IOException && ex.getMessage().contains("Socket closed"))
                                    LOG.info("client->server: socket closed.");
                                else
                                    ex.printStackTrace();
                            }
                        }).start();

                    } catch (IOException | ApiException e) {
                        e.printStackTrace();
                        try {
                            s.close();
                        } catch (IOException f) {
                            f.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                if (ss != null)
                    e.printStackTrace();
                connectionHandler = null;
            } finally {
                try {
                    ServerSocket ss2 = ss;
                    if (ss2 != null)
                        ss2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        connectionHandler.start();
    }

    @PreDestroy
    public void done() {
        Thread t = connectionHandler;
        if (t != null)
            t.interrupt();

        try {
            ServerSocket ss2 = ss;
            ss = null;
            ss2.close();
        } catch (IOException e) {
            LOG.error("could not close socket", e);
        }
    }
}
