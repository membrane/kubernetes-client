/*
Copyright 2017 The Kubernetes Authors.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package de.predic8.kubernetesclient.patch;

import com.google.common.net.HttpHeaders;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Pair;
import okhttp3.*;
import okio.Buffer;
import okio.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class WebSockets {
    public static final String V4_STREAM_PROTOCOL = "v4.channel.k8s.io";
    public static final String V3_STREAM_PROTOCOL = "v3.channel.k8s.io";
    public static final String V2_STREAM_PROTOCOL = "v2.channel.k8s.io";
    public static final String V1_STREAM_PROTOCOL = "channel.k8s.io";
    public static final String STREAM_PROTOCOL_HEADER = "X-Stream-Protocol-Version";
    public static final String SPDY_3_1 = "SPDY/3.1";

    private static final Logger log = LoggerFactory.getLogger(WebSockets.class);

    /**
     * Create a new WebSocket stream
     * @param path The HTTP Path to request from the API
     * @param method The HTTP method to use for the call
     * @param client The ApiClient for communicating with the API
     * @param listener The socket listener to handle socket events
     */
    public static void stream(String path, String method, ApiClient client, io.kubernetes.client.util.WebSockets.SocketListener listener) throws ApiException, IOException {
        stream(path, method, new ArrayList<Pair>(), client, listener);
    }

    public static Listener stream(String path, String method, List<Pair> queryParams, ApiClient client, io.kubernetes.client.util.WebSockets.SocketListener listener) throws ApiException, IOException {
            
        HashMap<String, String> headers = new HashMap<String, String>();
        String allProtocols = String.format("%s,%s,%s,%s", V4_STREAM_PROTOCOL, V3_STREAM_PROTOCOL, V2_STREAM_PROTOCOL, V1_STREAM_PROTOCOL);
        headers.put(STREAM_PROTOCOL_HEADER, allProtocols);
        headers.put(HttpHeaders.CONNECTION, HttpHeaders.UPGRADE);
        headers.put(HttpHeaders.UPGRADE, SPDY_3_1);

        String[] localVarAuthNames = new String[] { "BearerToken" };
        Request request = client.buildRequest(path, method, queryParams, new ArrayList<Pair>(), null, headers, new HashMap<String, String>(), new HashMap<String, Object>(), localVarAuthNames, null);
        return streamRequest(request, client, listener);
    }

    /* 
    If we ever upgrade to okhttp 3...
    public static void stream(Call call, ApiClient client, SocketListener listener) {
        streamRequest(call.request(), client, listener);
    }
    */

    private static Listener streamRequest(Request request, ApiClient client, io.kubernetes.client.util.WebSockets.SocketListener listener) {
        Listener listener1 = new Listener(listener);
        WebSocket webSocket = client.getHttpClient().newWebSocket(request, listener1);
        listener1.setWebSocket(webSocket);
        return listener1;
    }

    public static class Listener extends WebSocketListener {
        private io.kubernetes.client.util.WebSockets.SocketListener listener;
        private volatile boolean isCancelled;
        private volatile WebSocket webSocket;

        public Listener(io.kubernetes.client.util.WebSockets.SocketListener listener) {
            this.listener = listener;
        }

        @Override
        public void onOpen(final WebSocket webSocket, Response response) {
            String protocol = response.header(STREAM_PROTOCOL_HEADER, "missing");
            listener.open(protocol, webSocket);
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            listener.textMessage(new StringReader(text));
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            listener.bytesMessage(new ByteArrayInputStream(bytes.toByteArray()));
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            try {
                // This waits until the client pump had a change to pump the data
                log.debug("received close(), waiting 2 seconds...");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            listener.close();
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable e, Response res) {
            if (!isCancelled)
                e.printStackTrace();
            listener.close();
        }

        public void cancel() {
            isCancelled = true;
            webSocket.cancel();
        }

        public void setWebSocket(WebSocket webSocket) {
            this.webSocket = webSocket;
        }
    }
}

