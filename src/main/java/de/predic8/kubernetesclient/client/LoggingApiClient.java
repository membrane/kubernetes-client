package de.predic8.kubernetesclient.client;

import com.squareup.okhttp.*;
import com.squareup.okhttp.internal.http.HttpEngine;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;
import de.predic8.kubernetesclient.PEMSupport;
import io.kubernetes.client.ApiClient;
import okio.Buffer;
import okio.BufferedSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LoggingApiClient extends ApiClient {

    Logger LOG = LoggerFactory.getLogger(LocalKubeconfigApiClient.class);

    @Value("${kubernetes.client.logHttp:false}")
    boolean logHttp;


    @PostConstruct
    public void init() {
        getHttpClient().setReadTimeout(300, TimeUnit.SECONDS);

        if (logHttp) {
            Interceptor hli = new Interceptor() {
                /*
                 Source from com.squareup.okhttp:logging-interceptor:2.7.5 , Apache 2 License.

                 Modified to ignore "Upgrade: Websocket" responses.
                 */

                private final Charset UTF8 = Charset.forName("UTF-8");

                private volatile HttpLoggingInterceptor.Level level = HttpLoggingInterceptor.Level.BODY;


                @Override public Response intercept(Chain chain) throws IOException {
                    HttpLoggingInterceptor.Level level = this.level;

                    Request request = chain.request();
                    if (level == HttpLoggingInterceptor.Level.NONE) {
                        return chain.proceed(request);
                    }

                    boolean logBody = level == HttpLoggingInterceptor.Level.BODY;
                    boolean logHeaders = logBody || level == HttpLoggingInterceptor.Level.HEADERS;

                    RequestBody requestBody = request.body();
                    boolean hasRequestBody = requestBody != null;

                    Connection connection = chain.connection();
                    Protocol protocol = connection != null ? connection.getProtocol() : Protocol.HTTP_1_1;
                    String requestStartMessage =
                            "--> " + request.method() + ' ' + request.httpUrl() + ' ' + protocol(protocol);
                    if (!logHeaders && hasRequestBody) {
                        requestStartMessage += " (" + requestBody.contentLength() + "-byte body)";
                    }
                    LOG.info(requestStartMessage);

                    if (logHeaders) {
                        if (hasRequestBody) {
                            // Request body headers are only present when installed as a network interceptor. Force
                            // them to be included (when available) so there values are known.
                            if (requestBody.contentType() != null) {
                                LOG.info("Content-Type: " + requestBody.contentType());
                            }
                            if (requestBody.contentLength() != -1) {
                                LOG.info("Content-Length: " + requestBody.contentLength());
                            }
                        }

                        Headers headers = request.headers();
                        for (int i = 0, count = headers.size(); i < count; i++) {
                            String name = headers.name(i);
                            // Skip headers from the request body as they are explicitly logged above.
                            if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
                                LOG.info(name + ": " + headers.value(i));
                            }
                        }

                        if (!logBody || !hasRequestBody) {
                            LOG.info("--> END " + request.method());
                        } else if (bodyEncoded(request.headers())) {
                            LOG.info("--> END " + request.method() + " (encoded body omitted)");
                        } else {
                            Buffer buffer = new Buffer();
                            requestBody.writeTo(buffer);

                            Charset charset = UTF8;
                            MediaType contentType = requestBody.contentType();
                            if (contentType != null) {
                                contentType.charset(UTF8);
                            }

                            LOG.info("");
                            LOG.info(buffer.readString(charset));

                            LOG.info("--> END " + request.method()
                                    + " (" + requestBody.contentLength() + "-byte body)");
                        }
                    }

                    long startNs = System.nanoTime();
                    Response response = chain.proceed(request);
                    long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

                    ResponseBody responseBody = response.body();
                    LOG.info("<-- " + protocol(response.protocol()) + ' ' + response.code() + ' '
                            + response.message() + " (" + tookMs + "ms"
                            + (!logHeaders ? ", " + responseBody.contentLength() + "-byte body" : "") + ')');

                    if (logHeaders) {
                        Headers headers = response.headers();
                        for (int i = 0, count = headers.size(); i < count; i++) {
                            LOG.info(headers.name(i) + ": " + headers.value(i));
                        }

                        if (!logBody || !HttpEngine.hasBody(response) || "websocket".equals(headers.get("Upgrade"))) {
                            LOG.info("<-- END HTTP");
                        } else if (bodyEncoded(response.headers())) {
                            LOG.info("<-- END HTTP (encoded body omitted)");
                        } else {
                            BufferedSource source = responseBody.source();
                            source.request(Long.MAX_VALUE); // Buffer the entire body.
                            Buffer buffer = source.buffer();

                            Charset charset = UTF8;
                            MediaType contentType = responseBody.contentType();
                            if (contentType != null) {
                                charset = contentType.charset(UTF8);
                            }

                            if (responseBody.contentLength() != 0) {
                                LOG.info("");
                                LOG.info(buffer.clone().readString(charset));
                            }

                            LOG.info("<-- END HTTP (" + buffer.size() + "-byte body)");
                        }
                    }

                    return response;
                }

                private boolean bodyEncoded(Headers headers) {
                    String contentEncoding = headers.get("Content-Encoding");
                    return contentEncoding != null && !contentEncoding.equalsIgnoreCase("identity");
                }

                private String protocol(Protocol protocol) {
                    return protocol == Protocol.HTTP_1_0 ? "HTTP/1.0" : "HTTP/1.1";
                }


            };
            getHttpClient().interceptors().add(hli);
        }
    }


    protected void setupTLS(String ca, String cert, String key) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, KeyManagementException {
        if (ca == null && cert == null && key == null)
            return;
        String keyStoreType = "JKS";

        KeyStore ks = KeyStore.getInstance(keyStoreType);
        ks.load(null, "".toCharArray());

        ks.setCertificateEntry("inlinePemCertificate", PEMSupport.getInstance().parseCertificate(ca));

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(gkm(cert, key), tmf.getTrustManagers(), new SecureRandom());

        getHttpClient().setSslSocketFactory(sslContext.getSocketFactory());

    }

    protected KeyManager[] gkm(String cert, String k2) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException {
        if (cert == null && k2 == null)
            return new KeyManager[0];
        String keyStoreType = "JKS";

        KeyStore ks = KeyStore.getInstance(keyStoreType);
        ks.load(null, "".toCharArray());

        List<java.security.cert.Certificate> certs = new ArrayList<java.security.cert.Certificate>();

        certs.add(PEMSupport.getInstance().parseCertificate(cert));

        Object key = PEMSupport.getInstance().parseKey(k2);
        Key k = key instanceof Key ? (Key) key : ((KeyPair)key).getPrivate();
        if (k instanceof RSAPrivateCrtKey && certs.get(0).getPublicKey() instanceof RSAPublicKey) {
            RSAPrivateCrtKey privkey = (RSAPrivateCrtKey)k;
            RSAPublicKey pubkey = (RSAPublicKey) certs.get(0).getPublicKey();
            if (!(privkey.getModulus().equals(pubkey.getModulus()) && privkey.getPublicExponent().equals(pubkey.getPublicExponent())))
                LOG.warn("Certificate does not fit to key.");
        }

        ks.setKeyEntry("inlinePemKeyAndCertificate", k, "".toCharArray(),  certs.toArray(new Certificate[certs.size()]));

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        String keyPassword = "";
        kmf.init(ks, keyPassword.toCharArray());
        return kmf.getKeyManagers();
    }

}
