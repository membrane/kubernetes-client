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
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import com.squareup.okhttp.ws.WebSocket;
import com.squareup.okhttp.ws.WebSocketCall;
import com.squareup.okhttp.ws.WebSocketListener;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Pair;
import okio.Buffer;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import static com.squareup.okhttp.ws.WebSocket.BINARY;
import static com.squareup.okhttp.ws.WebSocket.TEXT;

public class WebSockets {
    public static final String V4_STREAM_PROTOCOL = "v4.channel.k8s.io";
    public static final String V3_STREAM_PROTOCOL = "v3.channel.k8s.io";
    public static final String V2_STREAM_PROTOCOL = "v2.channel.k8s.io";
    public static final String V1_STREAM_PROTOCOL = "channel.k8s.io";
    public static final String STREAM_PROTOCOL_HEADER = "X-Stream-Protocol-Version";
    public static final String SPDY_3_1 = "SPDY/3.1";

    private static final Logger log = Logger.getLogger(WebSockets.class);

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
        Request request = client.buildRequest(path, method, queryParams, new ArrayList<Pair>(), null, headers, new HashMap<String, Object>(), localVarAuthNames, null);
        return streamRequest(request, client, listener);
    }

    /* 
    If we ever upgrade to okhttp 3...
    public static void stream(Call call, ApiClient client, SocketListener listener) {
        streamRequest(call.request(), client, listener);
    }
    */

    private static Listener streamRequest(Request request, ApiClient client, io.kubernetes.client.util.WebSockets.SocketListener listener) {
        WebSocketCall webSocketCall = WebSocketCall.create(client.getHttpClient(), request);
        Listener listener1 = new Listener(listener, webSocketCall);
        webSocketCall.enqueue(listener1);
        return listener1;
    }

    public static class Listener implements WebSocketListener {
        private io.kubernetes.client.util.WebSockets.SocketListener listener;
        private WebSocketCall webSocketCall;
        private volatile boolean isCancelled;

        public Listener(io.kubernetes.client.util.WebSockets.SocketListener listener, WebSocketCall webSocketCall) {
            this.listener = listener;
            this.webSocketCall = webSocketCall;
        }

        @Override
        public void onOpen(final WebSocket webSocket, Response response) {
            System.err.println("status: " + response.code());
            String protocol = response.header(STREAM_PROTOCOL_HEADER, "missing");
            listener.open(protocol, webSocket);
        }

        @Override
        public void onMessage(ResponseBody body) throws IOException {
            if (body.contentType() == TEXT) {
                listener.textMessage(body.charStream());
            } else if (body.contentType() == BINARY) {
                listener.bytesMessage(body.byteStream());
            }
            body.close();
        }

        @Override
        public void onPong(Buffer payload) {
        }

        @Override
        public void onClose(int code, String reason) {
            try {
                // This waits until the client pump had a change to pump the data
                log.debug("received close(), waiting 2 seconds...");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            listener.close();
        }

        @Override
        public void onFailure(IOException e, Response res) {
            if (!isCancelled)
                e.printStackTrace();
            listener.close();
        }

        public void cancel() {
            isCancelled = true;
            webSocketCall.cancel();
        }
    }
}

