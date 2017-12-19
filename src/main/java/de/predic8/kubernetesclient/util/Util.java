package de.predic8.kubernetesclient.util;

import io.kubernetes.client.models.V1Pod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;
import java.util.jar.Manifest;

/**
 * Created by Daniel Bonnauer(bonnauer@predic8.de), Predic8 GmbH on 27.06.17
 */
public class Util {

    private static final Logger logger = LoggerFactory.getLogger(Util.class);

    private static String version;

    public static synchronized String getVersion() {
        if (version != null && !version.isEmpty())
            return version;
        try {
            version = new Manifest(Util.class.getResourceAsStream("/META-INF/MANIFEST.MF")).getMainAttributes().getValue("Implementation-Version");
        } catch (IOException e) {
            // do nothing
        }
        if (version == null)
            version = "" + System.currentTimeMillis();
        return version;
    }

    public static boolean podIsDifferent(V1Pod pod, V1Pod pod1) {
        return Comparator
                .comparing((V1Pod p) -> {
                    if (p.getMetadata().getAnnotations() == null)
                        return "";
                    String version = p.getMetadata().getAnnotations().get("version");
                    return version == null ? "" : version;
                })
                .thenComparing(p -> {
                    if (p.getMetadata().getAnnotations() == null)
                        return "";
                    String s = p.getMetadata().getAnnotations().get("kubernetes.io/created-by");
                    return s == null ? "" : s;
                })
                .compare(pod, pod1) != 0;
    }

    public static String getLocalIpAddress() {
        try {
            String localIpAddress = Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                    .flatMap(networkInterface -> Collections.list(networkInterface.getInetAddresses()).stream())
                    .filter(inetAddress -> inetAddress instanceof Inet4Address && inetAddress.getAddress()[0] != 127)
                    .map(InetAddress::getHostAddress)
                    .findFirst()
                    .orElseThrow(AssertionError::new);

            logger.debug("localIpAddress: {}", localIpAddress); // TODO not even in debug.

            return localIpAddress;
        } catch (SocketException e) {
            // should never happen
            throw new AssertionError();
        }
    }

    public static String loadYamlResource(Class clazz, String resourceBaseName) {
        String s = loadResource(clazz, getYAMLName(resourceBaseName));
        logger.debug("Loaded resource " + resourceBaseName  + ", for Class: " + clazz.getName(), s);
        return s;
    }

    public static String getYAMLName(String yamlName) {
        return "/" + yamlName + ".yaml";
    }

    public static String loadResource(Class clazz, String resourceName) {

        StringBuilder result = new StringBuilder("");

        InputStream is = clazz.getResourceAsStream(resourceName);

        try (Scanner scanner = new Scanner(is)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }
        }

        return result.toString();
    }

}
