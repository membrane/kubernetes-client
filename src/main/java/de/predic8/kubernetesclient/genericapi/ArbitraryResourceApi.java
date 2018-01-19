package de.predic8.kubernetesclient.genericapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.Call;
import io.kubernetes.client.*;
import io.kubernetes.client.models.V1DeleteOptions;
import io.kubernetes.client.models.V1Status;
import io.kubernetes.client.util.Watch;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArbitraryResourceApi<T> {
    private final ApiClient apiClient;
    private final ApiClient slowApiClient;
    private final String apiGroup;
    private final String plural;
    private final String version;

    public ArbitraryResourceApi(ApiClient apiClient, ApiClient slowApiClient, String apiGroup, String version, String plural) {
        this.apiClient = apiClient;
        this.slowApiClient = slowApiClient;
        this.apiGroup = apiGroup;
        this.version = version;
        this.plural = plural;
    }

    public T create(String namespace, T body, String pretty) throws ApiException {
        ApiResponse<T> resp = createWithHttpInfo(namespace, body, pretty);
        return resp.getData();
    }
    public ApiResponse<T> createWithHttpInfo(String namespace, T body, String pretty) throws ApiException {
        com.squareup.okhttp.Call call = createValidateBeforeCall(namespace, body, pretty, null, null);
        Type localVarReturnType = new TypeToken<T>(){}.getType();
        return apiClient.execute(call, localVarReturnType);
    }
    private com.squareup.okhttp.Call createValidateBeforeCall(String namespace, T body, String pretty, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {

        // verify the required parameter 'body' is set
        if (body == null) {
            throw new ApiException("Missing the required parameter 'body' when calling create(Async)");
        }


        com.squareup.okhttp.Call call = createCall(namespace, body, pretty, progressListener, progressRequestListener);
        return call;

    }

    private String namespaced(String namespace) {
        if (namespace == null)
            return "";
        return "/namespaces/" + apiClient.escapeString(namespace.toString());
    }

    private String apiGroup() {
        if (apiGroup == null)
            return "/api";
        return "/apis/" + apiGroup;
    }

    public com.squareup.okhttp.Call createCall(String namespace, T body, String pretty, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        Object localVarPostBody = body;

        // create path and map variables
        String localVarPath = apiGroup() + "/" + version + namespaced(namespace) + "/" + plural;

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        if (pretty != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("pretty", pretty));

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
                "application/json", "application/yaml", "application/vnd.kubernetes.protobuf"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {
                "*/*"
        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if(progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(new com.squareup.okhttp.Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(com.squareup.okhttp.Interceptor.Chain chain) throws IOException {
                    com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                            .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                            .build();
                }
            });
        }

        String[] localVarAuthNames = new String[] { "BearerToken" };
        return apiClient.buildCall(localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAuthNames, progressRequestListener);
    }

    /**
     * Build call for deleteCollectionCustomResourceDefinition
     * @param pretty If &#39;true&#39;, then the output is pretty printed. (optional)
     * @param _continue The continue option should be set when retrieving more results from the server. Since this value is server defined, clients may only use the continue value from a previous query result with identical query parameters (except for the value of continue) and the server may reject a continue value it does not recognize. If the specified continue value is no longer valid whether due to expiration (generally five to fifteen minutes) or a configuration change on the server the server will respond with a 410 ResourceExpired error indicating the client must restart their list without the continue field. This field is not supported when watch is true. Clients may start a watch from the last resourceVersion value returned by the server and not miss any modifications. (optional)
     * @param fieldSelector A selector to restrict the list of returned objects by their fields. Defaults to everything. (optional)
     * @param includeUninitialized If true, partially initialized resources are included in the response. (optional)
     * @param labelSelector A selector to restrict the list of returned objects by their labels. Defaults to everything. (optional)
     * @param limit limit is a maximum number of responses to return for a list call. If more items exist, the server will set the &#x60;continue&#x60; field on the list metadata to a value that can be used with the same initial query to retrieve the next set of results. Setting a limit may return fewer than the requested amount of items (up to zero items) in the event all requested objects are filtered out and clients should only use the presence of the continue field to determine whether more results are available. Servers may choose not to support the limit argument and will return all of the available results. If limit is specified and the continue field is empty, clients may assume that no more results are available. This field is not supported if watch is true.  The server guarantees that the objects returned when using continue will be identical to issuing a single list call without a limit - that is, no objects created, modified, or deleted after the first request is issued will be included in any subsequent continued requests. This is sometimes referred to as a consistent snapshot, and ensures that a client that is using limit to receive smaller chunks of a very large result can ensure they see all possible objects. If objects are updated during a chunked list the version of the object that was present at the time the first list result was calculated is returned. (optional)
     * @param resourceVersion When specified with a watch call, shows changes that occur after that particular version of a resource. Defaults to changes from the beginning of history. When specified for list: - if unset, then the result is returned from remote storage based on quorum-read flag; - if it&#39;s 0, then we simply return what we currently have in cache, no guarantee; - if set to non zero, then the result is at least as fresh as given rv. (optional)
     * @param timeoutSeconds Timeout for the list/watch call. (optional)
     * @param watch Watch for changes to the described resources and return them as a stream of add, update, and remove notifications. Specify resourceVersion. (optional)
     * @param progressListener Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public com.squareup.okhttp.Call deleteCollectionCall(String namespace, String pretty, String _continue, String fieldSelector, Boolean includeUninitialized, String labelSelector, Integer limit, String resourceVersion, Integer timeoutSeconds, Boolean watch, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = apiGroup() + "/" + version + namespaced(namespace) + "/" + plural;

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        if (pretty != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("pretty", pretty));
        if (_continue != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("continue", _continue));
        if (fieldSelector != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("fieldSelector", fieldSelector));
        if (includeUninitialized != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("includeUninitialized", includeUninitialized));
        if (labelSelector != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("labelSelector", labelSelector));
        if (limit != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("limit", limit));
        if (resourceVersion != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("resourceVersion", resourceVersion));
        if (timeoutSeconds != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("timeoutSeconds", timeoutSeconds));
        if (watch != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("watch", watch));

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
                "application/json", "application/yaml", "application/vnd.kubernetes.protobuf"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {
                "*/*"
        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if(progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(new com.squareup.okhttp.Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(com.squareup.okhttp.Interceptor.Chain chain) throws IOException {
                    com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                            .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                            .build();
                }
            });
        }

        String[] localVarAuthNames = new String[] { "BearerToken" };
        return apiClient.buildCall(localVarPath, "DELETE", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAuthNames, progressRequestListener);
    }

    @SuppressWarnings("rawtypes")
    private com.squareup.okhttp.Call deleteCollectionValidateBeforeCall(String namespace, String pretty, String _continue, String fieldSelector, Boolean includeUninitialized, String labelSelector, Integer limit, String resourceVersion, Integer timeoutSeconds, Boolean watch, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {


        com.squareup.okhttp.Call call = deleteCollectionCall(namespace, pretty, _continue, fieldSelector, includeUninitialized, labelSelector, limit, resourceVersion, timeoutSeconds, watch, progressListener, progressRequestListener);
        return call;





    }

    /**
     *
     * delete collection of CustomResourceDefinition
     * @param pretty If &#39;true&#39;, then the output is pretty printed. (optional)
     * @param _continue The continue option should be set when retrieving more results from the server. Since this value is server defined, clients may only use the continue value from a previous query result with identical query parameters (except for the value of continue) and the server may reject a continue value it does not recognize. If the specified continue value is no longer valid whether due to expiration (generally five to fifteen minutes) or a configuration change on the server the server will respond with a 410 ResourceExpired error indicating the client must restart their list without the continue field. This field is not supported when watch is true. Clients may start a watch from the last resourceVersion value returned by the server and not miss any modifications. (optional)
     * @param fieldSelector A selector to restrict the list of returned objects by their fields. Defaults to everything. (optional)
     * @param includeUninitialized If true, partially initialized resources are included in the response. (optional)
     * @param labelSelector A selector to restrict the list of returned objects by their labels. Defaults to everything. (optional)
     * @param limit limit is a maximum number of responses to return for a list call. If more items exist, the server will set the &#x60;continue&#x60; field on the list metadata to a value that can be used with the same initial query to retrieve the next set of results. Setting a limit may return fewer than the requested amount of items (up to zero items) in the event all requested objects are filtered out and clients should only use the presence of the continue field to determine whether more results are available. Servers may choose not to support the limit argument and will return all of the available results. If limit is specified and the continue field is empty, clients may assume that no more results are available. This field is not supported if watch is true.  The server guarantees that the objects returned when using continue will be identical to issuing a single list call without a limit - that is, no objects created, modified, or deleted after the first request is issued will be included in any subsequent continued requests. This is sometimes referred to as a consistent snapshot, and ensures that a client that is using limit to receive smaller chunks of a very large result can ensure they see all possible objects. If objects are updated during a chunked list the version of the object that was present at the time the first list result was calculated is returned. (optional)
     * @param resourceVersion When specified with a watch call, shows changes that occur after that particular version of a resource. Defaults to changes from the beginning of history. When specified for list: - if unset, then the result is returned from remote storage based on quorum-read flag; - if it&#39;s 0, then we simply return what we currently have in cache, no guarantee; - if set to non zero, then the result is at least as fresh as given rv. (optional)
     * @param timeoutSeconds Timeout for the list/watch call. (optional)
     * @param watch Watch for changes to the described resources and return them as a stream of add, update, and remove notifications. Specify resourceVersion. (optional)
     * @return V1Status
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public V1Status deleteCollection(String namespace, String pretty, String _continue, String fieldSelector, Boolean includeUninitialized, String labelSelector, Integer limit, String resourceVersion, Integer timeoutSeconds, Boolean watch) throws ApiException {
        ApiResponse<V1Status> resp = deleteCollectionWithHttpInfo(namespace, pretty, _continue, fieldSelector, includeUninitialized, labelSelector, limit, resourceVersion, timeoutSeconds, watch);
        return resp.getData();
    }

    /**
     *
     * delete collection of CustomResourceDefinition
     * @param pretty If &#39;true&#39;, then the output is pretty printed. (optional)
     * @param _continue The continue option should be set when retrieving more results from the server. Since this value is server defined, clients may only use the continue value from a previous query result with identical query parameters (except for the value of continue) and the server may reject a continue value it does not recognize. If the specified continue value is no longer valid whether due to expiration (generally five to fifteen minutes) or a configuration change on the server the server will respond with a 410 ResourceExpired error indicating the client must restart their list without the continue field. This field is not supported when watch is true. Clients may start a watch from the last resourceVersion value returned by the server and not miss any modifications. (optional)
     * @param fieldSelector A selector to restrict the list of returned objects by their fields. Defaults to everything. (optional)
     * @param includeUninitialized If true, partially initialized resources are included in the response. (optional)
     * @param labelSelector A selector to restrict the list of returned objects by their labels. Defaults to everything. (optional)
     * @param limit limit is a maximum number of responses to return for a list call. If more items exist, the server will set the &#x60;continue&#x60; field on the list metadata to a value that can be used with the same initial query to retrieve the next set of results. Setting a limit may return fewer than the requested amount of items (up to zero items) in the event all requested objects are filtered out and clients should only use the presence of the continue field to determine whether more results are available. Servers may choose not to support the limit argument and will return all of the available results. If limit is specified and the continue field is empty, clients may assume that no more results are available. This field is not supported if watch is true.  The server guarantees that the objects returned when using continue will be identical to issuing a single list call without a limit - that is, no objects created, modified, or deleted after the first request is issued will be included in any subsequent continued requests. This is sometimes referred to as a consistent snapshot, and ensures that a client that is using limit to receive smaller chunks of a very large result can ensure they see all possible objects. If objects are updated during a chunked list the version of the object that was present at the time the first list result was calculated is returned. (optional)
     * @param resourceVersion When specified with a watch call, shows changes that occur after that particular version of a resource. Defaults to changes from the beginning of history. When specified for list: - if unset, then the result is returned from remote storage based on quorum-read flag; - if it&#39;s 0, then we simply return what we currently have in cache, no guarantee; - if set to non zero, then the result is at least as fresh as given rv. (optional)
     * @param timeoutSeconds Timeout for the list/watch call. (optional)
     * @param watch Watch for changes to the described resources and return them as a stream of add, update, and remove notifications. Specify resourceVersion. (optional)
     * @return ApiResponse&lt;V1Status&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ApiResponse<V1Status> deleteCollectionWithHttpInfo(String namespace, String pretty, String _continue, String fieldSelector, Boolean includeUninitialized, String labelSelector, Integer limit, String resourceVersion, Integer timeoutSeconds, Boolean watch) throws ApiException {
        com.squareup.okhttp.Call call = deleteCollectionValidateBeforeCall(namespace, pretty, _continue, fieldSelector, includeUninitialized, labelSelector, limit, resourceVersion, timeoutSeconds, watch, null, null);
        Type localVarReturnType = new TypeToken<V1Status>(){}.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    /**
     *  (asynchronously)
     * delete collection of CustomResourceDefinition
     * @param pretty If &#39;true&#39;, then the output is pretty printed. (optional)
     * @param _continue The continue option should be set when retrieving more results from the server. Since this value is server defined, clients may only use the continue value from a previous query result with identical query parameters (except for the value of continue) and the server may reject a continue value it does not recognize. If the specified continue value is no longer valid whether due to expiration (generally five to fifteen minutes) or a configuration change on the server the server will respond with a 410 ResourceExpired error indicating the client must restart their list without the continue field. This field is not supported when watch is true. Clients may start a watch from the last resourceVersion value returned by the server and not miss any modifications. (optional)
     * @param fieldSelector A selector to restrict the list of returned objects by their fields. Defaults to everything. (optional)
     * @param includeUninitialized If true, partially initialized resources are included in the response. (optional)
     * @param labelSelector A selector to restrict the list of returned objects by their labels. Defaults to everything. (optional)
     * @param limit limit is a maximum number of responses to return for a list call. If more items exist, the server will set the &#x60;continue&#x60; field on the list metadata to a value that can be used with the same initial query to retrieve the next set of results. Setting a limit may return fewer than the requested amount of items (up to zero items) in the event all requested objects are filtered out and clients should only use the presence of the continue field to determine whether more results are available. Servers may choose not to support the limit argument and will return all of the available results. If limit is specified and the continue field is empty, clients may assume that no more results are available. This field is not supported if watch is true.  The server guarantees that the objects returned when using continue will be identical to issuing a single list call without a limit - that is, no objects created, modified, or deleted after the first request is issued will be included in any subsequent continued requests. This is sometimes referred to as a consistent snapshot, and ensures that a client that is using limit to receive smaller chunks of a very large result can ensure they see all possible objects. If objects are updated during a chunked list the version of the object that was present at the time the first list result was calculated is returned. (optional)
     * @param resourceVersion When specified with a watch call, shows changes that occur after that particular version of a resource. Defaults to changes from the beginning of history. When specified for list: - if unset, then the result is returned from remote storage based on quorum-read flag; - if it&#39;s 0, then we simply return what we currently have in cache, no guarantee; - if set to non zero, then the result is at least as fresh as given rv. (optional)
     * @param timeoutSeconds Timeout for the list/watch call. (optional)
     * @param watch Watch for changes to the described resources and return them as a stream of add, update, and remove notifications. Specify resourceVersion. (optional)
     * @param callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     */
    public com.squareup.okhttp.Call deleteCollectionAsync(String namespace, String pretty, String _continue, String fieldSelector, Boolean includeUninitialized, String labelSelector, Integer limit, String resourceVersion, Integer timeoutSeconds, Boolean watch, final ApiCallback<V1Status> callback) throws ApiException {

        ProgressResponseBody.ProgressListener progressListener = null;
        ProgressRequestBody.ProgressRequestListener progressRequestListener = null;

        if (callback != null) {
            progressListener = new ProgressResponseBody.ProgressListener() {
                @Override
                public void update(long bytesRead, long contentLength, boolean done) {
                    callback.onDownloadProgress(bytesRead, contentLength, done);
                }
            };

            progressRequestListener = new ProgressRequestBody.ProgressRequestListener() {
                @Override
                public void onRequestProgress(long bytesWritten, long contentLength, boolean done) {
                    callback.onUploadProgress(bytesWritten, contentLength, done);
                }
            };
        }

        com.squareup.okhttp.Call call = deleteCollectionValidateBeforeCall(namespace, pretty, _continue, fieldSelector, includeUninitialized, labelSelector, limit, resourceVersion, timeoutSeconds, watch, progressListener, progressRequestListener);
        Type localVarReturnType = new TypeToken<V1Status>(){}.getType();
        apiClient.executeAsync(call, localVarReturnType, callback);
        return call;
    }
    /**
     * Build call for deleteCustomResourceDefinition
     * @param name name of the CustomResourceDefinition (required)
     * @param body  (required)
     * @param pretty If &#39;true&#39;, then the output is pretty printed. (optional)
     * @param gracePeriodSeconds The duration in seconds before the object should be deleted. Value must be non-negative integer. The value zero indicates delete immediately. If this value is nil, the default grace period for the specified type will be used. Defaults to a per object value if not specified. zero means delete immediately. (optional)
     * @param orphanDependents Deprecated: please use the PropagationPolicy, this field will be deprecated in 1.7. Should the dependent objects be orphaned. If true/false, the \&quot;orphan\&quot; finalizer will be added to/removed from the object&#39;s finalizers list. Either this field or PropagationPolicy may be set, but not both. (optional)
     * @param propagationPolicy Whether and how garbage collection will be performed. Either this field or OrphanDependents may be set, but not both. The default policy is decided by the existing finalizer set in the metadata.finalizers and the resource-specific default policy. (optional)
     * @param progressListener Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public com.squareup.okhttp.Call deleteCall(String namespace, String name, V1DeleteOptions body, String pretty, Integer gracePeriodSeconds, Boolean orphanDependents, String propagationPolicy, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        Object localVarPostBody = body;

        // create path and map variables
        String localVarPath = apiGroup() + "/" + version + namespaced(namespace) + "/" + plural + "/{name}"
                .replaceAll("\\{" + "name" + "\\}", apiClient.escapeString(name.toString()));

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        if (pretty != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("pretty", pretty));
        if (gracePeriodSeconds != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("gracePeriodSeconds", gracePeriodSeconds));
        if (orphanDependents != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("orphanDependents", orphanDependents));
        if (propagationPolicy != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("propagationPolicy", propagationPolicy));

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
                "application/json", "application/yaml", "application/vnd.kubernetes.protobuf"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {
                "*/*"
        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if(progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(new com.squareup.okhttp.Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(com.squareup.okhttp.Interceptor.Chain chain) throws IOException {
                    com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                            .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                            .build();
                }
            });
        }

        String[] localVarAuthNames = new String[] { "BearerToken" };
        return apiClient.buildCall(localVarPath, "DELETE", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAuthNames, progressRequestListener);
    }

    @SuppressWarnings("rawtypes")
    private com.squareup.okhttp.Call deleteValidateBeforeCall(String namespace, String name, V1DeleteOptions body, String pretty, Integer gracePeriodSeconds, Boolean orphanDependents, String propagationPolicy, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {

        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException("Missing the required parameter 'name' when calling deleteCustomResourceDefinition(Async)");
        }

        // verify the required parameter 'body' is set
        if (body == null) {
            throw new ApiException("Missing the required parameter 'body' when calling deleteCustomResourceDefinition(Async)");
        }


        com.squareup.okhttp.Call call = deleteCall(namespace, name, body, pretty, gracePeriodSeconds, orphanDependents, propagationPolicy, progressListener, progressRequestListener);
        return call;





    }

    /**
     *
     * delete a CustomResourceDefinition
     * @param name name of the CustomResourceDefinition (required)
     * @param body  (required)
     * @param pretty If &#39;true&#39;, then the output is pretty printed. (optional)
     * @param gracePeriodSeconds The duration in seconds before the object should be deleted. Value must be non-negative integer. The value zero indicates delete immediately. If this value is nil, the default grace period for the specified type will be used. Defaults to a per object value if not specified. zero means delete immediately. (optional)
     * @param orphanDependents Deprecated: please use the PropagationPolicy, this field will be deprecated in 1.7. Should the dependent objects be orphaned. If true/false, the \&quot;orphan\&quot; finalizer will be added to/removed from the object&#39;s finalizers list. Either this field or PropagationPolicy may be set, but not both. (optional)
     * @param propagationPolicy Whether and how garbage collection will be performed. Either this field or OrphanDependents may be set, but not both. The default policy is decided by the existing finalizer set in the metadata.finalizers and the resource-specific default policy. (optional)
     * @return V1Status
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public V1Status delete(String namespace, String name, V1DeleteOptions body, String pretty, Integer gracePeriodSeconds, Boolean orphanDependents, String propagationPolicy) throws ApiException {
        ApiResponse<V1Status> resp = deleteWithHttpInfo(namespace, name, body, pretty, gracePeriodSeconds, orphanDependents, propagationPolicy);
        return resp.getData();
    }

    /**
     *
     * delete a CustomResourceDefinition
     * @param name name of the CustomResourceDefinition (required)
     * @param body  (required)
     * @param pretty If &#39;true&#39;, then the output is pretty printed. (optional)
     * @param gracePeriodSeconds The duration in seconds before the object should be deleted. Value must be non-negative integer. The value zero indicates delete immediately. If this value is nil, the default grace period for the specified type will be used. Defaults to a per object value if not specified. zero means delete immediately. (optional)
     * @param orphanDependents Deprecated: please use the PropagationPolicy, this field will be deprecated in 1.7. Should the dependent objects be orphaned. If true/false, the \&quot;orphan\&quot; finalizer will be added to/removed from the object&#39;s finalizers list. Either this field or PropagationPolicy may be set, but not both. (optional)
     * @param propagationPolicy Whether and how garbage collection will be performed. Either this field or OrphanDependents may be set, but not both. The default policy is decided by the existing finalizer set in the metadata.finalizers and the resource-specific default policy. (optional)
     * @return ApiResponse&lt;V1Status&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ApiResponse<V1Status> deleteWithHttpInfo(String namespace, String name, V1DeleteOptions body, String pretty, Integer gracePeriodSeconds, Boolean orphanDependents, String propagationPolicy) throws ApiException {
        com.squareup.okhttp.Call call = deleteValidateBeforeCall(namespace, name, body, pretty, gracePeriodSeconds, orphanDependents, propagationPolicy, null, null);
        Type localVarReturnType = new TypeToken<V1Status>(){}.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    /**
     *  (asynchronously)
     * delete a CustomResourceDefinition
     * @param name name of the CustomResourceDefinition (required)
     * @param body  (required)
     * @param pretty If &#39;true&#39;, then the output is pretty printed. (optional)
     * @param gracePeriodSeconds The duration in seconds before the object should be deleted. Value must be non-negative integer. The value zero indicates delete immediately. If this value is nil, the default grace period for the specified type will be used. Defaults to a per object value if not specified. zero means delete immediately. (optional)
     * @param orphanDependents Deprecated: please use the PropagationPolicy, this field will be deprecated in 1.7. Should the dependent objects be orphaned. If true/false, the \&quot;orphan\&quot; finalizer will be added to/removed from the object&#39;s finalizers list. Either this field or PropagationPolicy may be set, but not both. (optional)
     * @param propagationPolicy Whether and how garbage collection will be performed. Either this field or OrphanDependents may be set, but not both. The default policy is decided by the existing finalizer set in the metadata.finalizers and the resource-specific default policy. (optional)
     * @param callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     */
    public com.squareup.okhttp.Call deleteAsync(String namespace, String name, V1DeleteOptions body, String pretty, Integer gracePeriodSeconds, Boolean orphanDependents, String propagationPolicy, final ApiCallback<V1Status> callback) throws ApiException {

        ProgressResponseBody.ProgressListener progressListener = null;
        ProgressRequestBody.ProgressRequestListener progressRequestListener = null;

        if (callback != null) {
            progressListener = new ProgressResponseBody.ProgressListener() {
                @Override
                public void update(long bytesRead, long contentLength, boolean done) {
                    callback.onDownloadProgress(bytesRead, contentLength, done);
                }
            };

            progressRequestListener = new ProgressRequestBody.ProgressRequestListener() {
                @Override
                public void onRequestProgress(long bytesWritten, long contentLength, boolean done) {
                    callback.onUploadProgress(bytesWritten, contentLength, done);
                }
            };
        }

        com.squareup.okhttp.Call call = deleteValidateBeforeCall(namespace, name, body, pretty, gracePeriodSeconds, orphanDependents, propagationPolicy, progressListener, progressRequestListener);
        Type localVarReturnType = new TypeToken<V1Status>(){}.getType();
        apiClient.executeAsync(call, localVarReturnType, callback);
        return call;
    }

    /**
     * Build call for listCustomResourceDefinition
     * @param pretty If &#39;true&#39;, then the output is pretty printed. (optional)
     * @param _continue The continue option should be set when retrieving more results from the server. Since this value is server defined, clients may only use the continue value from a previous query result with identical query parameters (except for the value of continue) and the server may reject a continue value it does not recognize. If the specified continue value is no longer valid whether due to expiration (generally five to fifteen minutes) or a configuration change on the server the server will respond with a 410 ResourceExpired error indicating the client must restart their list without the continue field. This field is not supported when watch is true. Clients may start a watch from the last resourceVersion value returned by the server and not miss any modifications. (optional)
     * @param fieldSelector A selector to restrict the list of returned objects by their fields. Defaults to everything. (optional)
     * @param includeUninitialized If true, partially initialized resources are included in the response. (optional)
     * @param labelSelector A selector to restrict the list of returned objects by their labels. Defaults to everything. (optional)
     * @param limit limit is a maximum number of responses to return for a list call. If more items exist, the server will set the &#x60;continue&#x60; field on the list metadata to a value that can be used with the same initial query to retrieve the next set of results. Setting a limit may return fewer than the requested amount of items (up to zero items) in the event all requested objects are filtered out and clients should only use the presence of the continue field to determine whether more results are available. Servers may choose not to support the limit argument and will return all of the available results. If limit is specified and the continue field is empty, clients may assume that no more results are available. This field is not supported if watch is true.  The server guarantees that the objects returned when using continue will be identical to issuing a single list call without a limit - that is, no objects created, modified, or deleted after the first request is issued will be included in any subsequent continued requests. This is sometimes referred to as a consistent snapshot, and ensures that a client that is using limit to receive smaller chunks of a very large result can ensure they see all possible objects. If objects are updated during a chunked list the version of the object that was present at the time the first list result was calculated is returned. (optional)
     * @param resourceVersion When specified with a watch call, shows changes that occur after that particular version of a resource. Defaults to changes from the beginning of history. When specified for list: - if unset, then the result is returned from remote storage based on quorum-read flag; - if it&#39;s 0, then we simply return what we currently have in cache, no guarantee; - if set to non zero, then the result is at least as fresh as given rv. (optional)
     * @param timeoutSeconds Timeout for the list/watch call. (optional)
     * @param watch Watch for changes to the described resources and return them as a stream of add, update, and remove notifications. Specify resourceVersion. (optional)
     * @param progressListener Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public com.squareup.okhttp.Call listCall(String namespace, String pretty, String _continue, String fieldSelector, Boolean includeUninitialized, String labelSelector, Integer limit, String resourceVersion, Integer timeoutSeconds, Boolean watch, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = apiGroup() + "/" + version + namespaced(namespace) + "/" + plural;

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        if (pretty != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("pretty", pretty));
        if (_continue != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("continue", _continue));
        if (fieldSelector != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("fieldSelector", fieldSelector));
        if (includeUninitialized != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("includeUninitialized", includeUninitialized));
        if (labelSelector != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("labelSelector", labelSelector));
        if (limit != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("limit", limit));
        if (resourceVersion != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("resourceVersion", resourceVersion));
        if (timeoutSeconds != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("timeoutSeconds", timeoutSeconds));
        if (watch != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("watch", watch));

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
                "application/json", "application/yaml", "application/vnd.kubernetes.protobuf", "application/json;stream=watch", "application/vnd.kubernetes.protobuf;stream=watch"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {
                "*/*"
        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if(progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(new com.squareup.okhttp.Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(com.squareup.okhttp.Interceptor.Chain chain) throws IOException {
                    com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                            .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                            .build();
                }
            });
        }

        String[] localVarAuthNames = new String[] { "BearerToken" };
        return apiClient.buildCall(localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAuthNames, progressRequestListener);
    }

    @SuppressWarnings("rawtypes")
    private com.squareup.okhttp.Call listValidateBeforeCall(String namespace, String pretty, String _continue, String fieldSelector, Boolean includeUninitialized, String labelSelector, Integer limit, String resourceVersion, Integer timeoutSeconds, Boolean watch, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {


        com.squareup.okhttp.Call call = listCall(namespace, pretty, _continue, fieldSelector, includeUninitialized, labelSelector, limit, resourceVersion, timeoutSeconds, watch, progressListener, progressRequestListener);
        return call;





    }

    /**
     *
     * list or watch objects of kind CustomResourceDefinition
     * @param pretty If &#39;true&#39;, then the output is pretty printed. (optional)
     * @param _continue The continue option should be set when retrieving more results from the server. Since this value is server defined, clients may only use the continue value from a previous query result with identical query parameters (except for the value of continue) and the server may reject a continue value it does not recognize. If the specified continue value is no longer valid whether due to expiration (generally five to fifteen minutes) or a configuration change on the server the server will respond with a 410 ResourceExpired error indicating the client must restart their list without the continue field. This field is not supported when watch is true. Clients may start a watch from the last resourceVersion value returned by the server and not miss any modifications. (optional)
     * @param fieldSelector A selector to restrict the list of returned objects by their fields. Defaults to everything. (optional)
     * @param includeUninitialized If true, partially initialized resources are included in the response. (optional)
     * @param labelSelector A selector to restrict the list of returned objects by their labels. Defaults to everything. (optional)
     * @param limit limit is a maximum number of responses to return for a list call. If more items exist, the server will set the &#x60;continue&#x60; field on the list metadata to a value that can be used with the same initial query to retrieve the next set of results. Setting a limit may return fewer than the requested amount of items (up to zero items) in the event all requested objects are filtered out and clients should only use the presence of the continue field to determine whether more results are available. Servers may choose not to support the limit argument and will return all of the available results. If limit is specified and the continue field is empty, clients may assume that no more results are available. This field is not supported if watch is true.  The server guarantees that the objects returned when using continue will be identical to issuing a single list call without a limit - that is, no objects created, modified, or deleted after the first request is issued will be included in any subsequent continued requests. This is sometimes referred to as a consistent snapshot, and ensures that a client that is using limit to receive smaller chunks of a very large result can ensure they see all possible objects. If objects are updated during a chunked list the version of the object that was present at the time the first list result was calculated is returned. (optional)
     * @param resourceVersion When specified with a watch call, shows changes that occur after that particular version of a resource. Defaults to changes from the beginning of history. When specified for list: - if unset, then the result is returned from remote storage based on quorum-read flag; - if it&#39;s 0, then we simply return what we currently have in cache, no guarantee; - if set to non zero, then the result is at least as fresh as given rv. (optional)
     * @param timeoutSeconds Timeout for the list/watch call. (optional)
     * @param watch Watch for changes to the described resources and return them as a stream of add, update, and remove notifications. Specify resourceVersion. (optional)
     * @return V1beta1CustomResourceDefinitionList
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ARList<T> list(String namespace, String pretty, String _continue, String fieldSelector, Boolean includeUninitialized, String labelSelector, Integer limit, String resourceVersion, Integer timeoutSeconds, Boolean watch) throws ApiException {
        ApiResponse<ARList<T>> resp = listWithHttpInfo(namespace, pretty, _continue, fieldSelector, includeUninitialized, labelSelector, limit, resourceVersion, timeoutSeconds, watch);
        return resp.getData();
    }

    /**
     *
     * list or watch objects of kind CustomResourceDefinition
     * @param pretty If &#39;true&#39;, then the output is pretty printed. (optional)
     * @param _continue The continue option should be set when retrieving more results from the server. Since this value is server defined, clients may only use the continue value from a previous query result with identical query parameters (except for the value of continue) and the server may reject a continue value it does not recognize. If the specified continue value is no longer valid whether due to expiration (generally five to fifteen minutes) or a configuration change on the server the server will respond with a 410 ResourceExpired error indicating the client must restart their list without the continue field. This field is not supported when watch is true. Clients may start a watch from the last resourceVersion value returned by the server and not miss any modifications. (optional)
     * @param fieldSelector A selector to restrict the list of returned objects by their fields. Defaults to everything. (optional)
     * @param includeUninitialized If true, partially initialized resources are included in the response. (optional)
     * @param labelSelector A selector to restrict the list of returned objects by their labels. Defaults to everything. (optional)
     * @param limit limit is a maximum number of responses to return for a list call. If more items exist, the server will set the &#x60;continue&#x60; field on the list metadata to a value that can be used with the same initial query to retrieve the next set of results. Setting a limit may return fewer than the requested amount of items (up to zero items) in the event all requested objects are filtered out and clients should only use the presence of the continue field to determine whether more results are available. Servers may choose not to support the limit argument and will return all of the available results. If limit is specified and the continue field is empty, clients may assume that no more results are available. This field is not supported if watch is true.  The server guarantees that the objects returned when using continue will be identical to issuing a single list call without a limit - that is, no objects created, modified, or deleted after the first request is issued will be included in any subsequent continued requests. This is sometimes referred to as a consistent snapshot, and ensures that a client that is using limit to receive smaller chunks of a very large result can ensure they see all possible objects. If objects are updated during a chunked list the version of the object that was present at the time the first list result was calculated is returned. (optional)
     * @param resourceVersion When specified with a watch call, shows changes that occur after that particular version of a resource. Defaults to changes from the beginning of history. When specified for list: - if unset, then the result is returned from remote storage based on quorum-read flag; - if it&#39;s 0, then we simply return what we currently have in cache, no guarantee; - if set to non zero, then the result is at least as fresh as given rv. (optional)
     * @param timeoutSeconds Timeout for the list/watch call. (optional)
     * @param watch Watch for changes to the described resources and return them as a stream of add, update, and remove notifications. Specify resourceVersion. (optional)
     * @return ApiResponse&lt;V1beta1CustomResourceDefinitionList&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ApiResponse<ARList<T>> listWithHttpInfo(String namespace, String pretty, String _continue, String fieldSelector, Boolean includeUninitialized, String labelSelector, Integer limit, String resourceVersion, Integer timeoutSeconds, Boolean watch) throws ApiException {
        com.squareup.okhttp.Call call = listValidateBeforeCall(namespace, pretty, _continue, fieldSelector, includeUninitialized, labelSelector, limit, resourceVersion, timeoutSeconds, watch, null, null);
        Type localVarReturnType = new TypeToken<ARList<T>>(){}.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    public com.squareup.okhttp.Call listAsync(String namespace, String pretty, String _continue, String fieldSelector, Boolean includeUninitialized, String labelSelector, Integer limit, String resourceVersion, Integer timeoutSeconds, Boolean watch, final ApiCallback<ARList<T>> callback) throws ApiException {

        ProgressResponseBody.ProgressListener progressListener = null;
        ProgressRequestBody.ProgressRequestListener progressRequestListener = null;

        if (callback != null) {
            progressListener = new ProgressResponseBody.ProgressListener() {
                @Override
                public void update(long bytesRead, long contentLength, boolean done) {
                    callback.onDownloadProgress(bytesRead, contentLength, done);
                }
            };

            progressRequestListener = new ProgressRequestBody.ProgressRequestListener() {
                @Override
                public void onRequestProgress(long bytesWritten, long contentLength, boolean done) {
                    callback.onUploadProgress(bytesWritten, contentLength, done);
                }
            };
        }

        com.squareup.okhttp.Call call = listValidateBeforeCall(namespace, pretty, _continue, fieldSelector, includeUninitialized, labelSelector, limit, resourceVersion, timeoutSeconds, watch, progressListener, progressRequestListener);
        Type localVarReturnType = new TypeToken<ARList<T>>(){}.getType();
        apiClient.executeAsync(call, localVarReturnType, callback);
        return call;
    }
    /**
     * Build call for patchCustomResourceDefinition
     * @param name name of the CustomResourceDefinition (required)
     * @param body  (required)
     * @param pretty If &#39;true&#39;, then the output is pretty printed. (optional)
     * @param progressListener Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public com.squareup.okhttp.Call patchCall(String namespace, String name, Object body, String pretty, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        Object localVarPostBody = body;

        // create path and map variables
        String localVarPath = apiGroup() + "/" + version + namespaced(namespace) + "/" + plural + "/{name}"
                .replaceAll("\\{" + "name" + "\\}", apiClient.escapeString(name.toString()));

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        if (pretty != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("pretty", pretty));

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
                "application/json", "application/yaml", "application/vnd.kubernetes.protobuf"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {
                "application/json-patch+json", "application/merge-patch+json", "application/strategic-merge-patch+json"
        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if(progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(new com.squareup.okhttp.Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(com.squareup.okhttp.Interceptor.Chain chain) throws IOException {
                    com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                            .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                            .build();
                }
            });
        }

        String[] localVarAuthNames = new String[] { "BearerToken" };
        return apiClient.buildCall(localVarPath, "PATCH", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAuthNames, progressRequestListener);
    }

    @SuppressWarnings("rawtypes")
    private com.squareup.okhttp.Call patchValidateBeforeCall(String namespace, String name, Object body, String pretty, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {

        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException("Missing the required parameter 'name' when calling patchCustomResourceDefinition(Async)");
        }

        // verify the required parameter 'body' is set
        if (body == null) {
            throw new ApiException("Missing the required parameter 'body' when calling patchCustomResourceDefinition(Async)");
        }


        com.squareup.okhttp.Call call = patchCall(namespace, name, body, pretty, progressListener, progressRequestListener);
        return call;





    }

    /**
     *
     * partially update the specified CustomResourceDefinition
     * @param name name of the CustomResourceDefinition (required)
     * @param body  (required)
     * @param pretty If &#39;true&#39;, then the output is pretty printed. (optional)
     * @return V1beta1CustomResourceDefinition
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public T patch(String namespace, String name, Object body, String pretty) throws ApiException {
        ApiResponse<T> resp = patchWithHttpInfo(namespace, name, body, pretty);
        return resp.getData();
    }

    /**
     *
     * partially update the specified CustomResourceDefinition
     * @param name name of the CustomResourceDefinition (required)
     * @param body  (required)
     * @param pretty If &#39;true&#39;, then the output is pretty printed. (optional)
     * @return ApiResponse&lt;V1beta1CustomResourceDefinition&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ApiResponse<T> patchWithHttpInfo(String namespace, String name, Object body, String pretty) throws ApiException {
        com.squareup.okhttp.Call call = patchValidateBeforeCall(namespace, name, body, pretty, null, null);
        Type localVarReturnType = new TypeToken<T>(){}.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    /**
     *  (asynchronously)
     * partially update the specified CustomResourceDefinition
     * @param name name of the CustomResourceDefinition (required)
     * @param body  (required)
     * @param pretty If &#39;true&#39;, then the output is pretty printed. (optional)
     * @param callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     */
    public com.squareup.okhttp.Call patchAsync(String namespace, String name, Object body, String pretty, final ApiCallback<T> callback) throws ApiException {

        ProgressResponseBody.ProgressListener progressListener = null;
        ProgressRequestBody.ProgressRequestListener progressRequestListener = null;

        if (callback != null) {
            progressListener = new ProgressResponseBody.ProgressListener() {
                @Override
                public void update(long bytesRead, long contentLength, boolean done) {
                    callback.onDownloadProgress(bytesRead, contentLength, done);
                }
            };

            progressRequestListener = new ProgressRequestBody.ProgressRequestListener() {
                @Override
                public void onRequestProgress(long bytesWritten, long contentLength, boolean done) {
                    callback.onUploadProgress(bytesWritten, contentLength, done);
                }
            };
        }

        com.squareup.okhttp.Call call = patchValidateBeforeCall(namespace, name, body, pretty, progressListener, progressRequestListener);
        Type localVarReturnType = new TypeToken<T>(){}.getType();
        apiClient.executeAsync(call, localVarReturnType, callback);
        return call;
    }
    /**
     * Build call for readCustomResourceDefinition
     * @param name name of the CustomResourceDefinition (required)
     * @param pretty If &#39;true&#39;, then the output is pretty printed. (optional)
     * @param exact Should the export be exact.  Exact export maintains cluster-specific fields like &#39;Namespace&#39;. (optional)
     * @param export Should this value be exported.  Export strips fields that a user can not specify. (optional)
     * @param progressListener Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public com.squareup.okhttp.Call readCall(String namespace, String name, String pretty, Boolean exact, Boolean export, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = apiGroup() + "/" + version + namespaced(namespace) + "/" + plural + "/{name}"
                .replaceAll("\\{" + "name" + "\\}", apiClient.escapeString(name.toString()));

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        if (pretty != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("pretty", pretty));
        if (exact != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("exact", exact));
        if (export != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("export", export));

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
                "application/json", "application/yaml", "application/vnd.kubernetes.protobuf"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {
                "*/*"
        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if(progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(new com.squareup.okhttp.Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(com.squareup.okhttp.Interceptor.Chain chain) throws IOException {
                    com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                            .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                            .build();
                }
            });
        }

        String[] localVarAuthNames = new String[] { "BearerToken" };
        return apiClient.buildCall(localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAuthNames, progressRequestListener);
    }

    @SuppressWarnings("rawtypes")
    private com.squareup.okhttp.Call readValidateBeforeCall(String namespace, String name, String pretty, Boolean exact, Boolean export, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {

        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException("Missing the required parameter 'name' when calling readCustomResourceDefinition(Async)");
        }


        com.squareup.okhttp.Call call = readCall(namespace, name, pretty, exact, export, progressListener, progressRequestListener);
        return call;





    }

    /**
     *
     * read the specified CustomResourceDefinition
     * @param name name of the CustomResourceDefinition (required)
     * @param pretty If &#39;true&#39;, then the output is pretty printed. (optional)
     * @param exact Should the export be exact.  Exact export maintains cluster-specific fields like &#39;Namespace&#39;. (optional)
     * @param export Should this value be exported.  Export strips fields that a user can not specify. (optional)
     * @return V1beta1CustomResourceDefinition
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public T read(String namespace, String name, String pretty, Boolean exact, Boolean export) throws ApiException {
        ApiResponse<T> resp = readWithHttpInfo(namespace, name, pretty, exact, export);
        return resp.getData();
    }

    /**
     *
     * read the specified CustomResourceDefinition
     * @param name name of the CustomResourceDefinition (required)
     * @param pretty If &#39;true&#39;, then the output is pretty printed. (optional)
     * @param exact Should the export be exact.  Exact export maintains cluster-specific fields like &#39;Namespace&#39;. (optional)
     * @param export Should this value be exported.  Export strips fields that a user can not specify. (optional)
     * @return ApiResponse&lt;V1beta1CustomResourceDefinition&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ApiResponse<T> readWithHttpInfo(String namespace, String name, String pretty, Boolean exact, Boolean export) throws ApiException {
        com.squareup.okhttp.Call call = readValidateBeforeCall(namespace, name, pretty, exact, export, null, null);
        Type localVarReturnType = new TypeToken<T>(){}.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    /**
     *  (asynchronously)
     * read the specified CustomResourceDefinition
     * @param name name of the CustomResourceDefinition (required)
     * @param pretty If &#39;true&#39;, then the output is pretty printed. (optional)
     * @param exact Should the export be exact.  Exact export maintains cluster-specific fields like &#39;Namespace&#39;. (optional)
     * @param export Should this value be exported.  Export strips fields that a user can not specify. (optional)
     * @param callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     */
    public com.squareup.okhttp.Call readAsync(String namespace, String name, String pretty, Boolean exact, Boolean export, final ApiCallback<T> callback) throws ApiException {

        ProgressResponseBody.ProgressListener progressListener = null;
        ProgressRequestBody.ProgressRequestListener progressRequestListener = null;

        if (callback != null) {
            progressListener = new ProgressResponseBody.ProgressListener() {
                @Override
                public void update(long bytesRead, long contentLength, boolean done) {
                    callback.onDownloadProgress(bytesRead, contentLength, done);
                }
            };

            progressRequestListener = new ProgressRequestBody.ProgressRequestListener() {
                @Override
                public void onRequestProgress(long bytesWritten, long contentLength, boolean done) {
                    callback.onUploadProgress(bytesWritten, contentLength, done);
                }
            };
        }

        com.squareup.okhttp.Call call = readValidateBeforeCall(namespace, name, pretty, exact, export, progressListener, progressRequestListener);
        Type localVarReturnType = new TypeToken<T>(){}.getType();
        apiClient.executeAsync(call, localVarReturnType, callback);
        return call;
    }
    /**
     * Build call for replaceCustomResourceDefinition
     * @param name name of the CustomResourceDefinition (required)
     * @param body  (required)
     * @param pretty If &#39;true&#39;, then the output is pretty printed. (optional)
     * @param progressListener Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public com.squareup.okhttp.Call replaceCall(String namespace, String name, T body, String pretty, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        Object localVarPostBody = body;

        // create path and map variables
        String localVarPath = apiGroup() + "/" + version + namespaced(namespace) + "/" + plural + "/{name}"
                .replaceAll("\\{" + "name" + "\\}", apiClient.escapeString(name.toString()));

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        if (pretty != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("pretty", pretty));

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
                "application/json", "application/yaml", "application/vnd.kubernetes.protobuf"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {
                "*/*"
        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if(progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(new com.squareup.okhttp.Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(com.squareup.okhttp.Interceptor.Chain chain) throws IOException {
                    com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                            .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                            .build();
                }
            });
        }

        String[] localVarAuthNames = new String[] { "BearerToken" };
        return apiClient.buildCall(localVarPath, "PUT", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAuthNames, progressRequestListener);
    }

    @SuppressWarnings("rawtypes")
    private com.squareup.okhttp.Call replaceValidateBeforeCall(String namespace, String name, T body, String pretty, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {

        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException("Missing the required parameter 'name' when calling replaceCustomResourceDefinition(Async)");
        }

        // verify the required parameter 'body' is set
        if (body == null) {
            throw new ApiException("Missing the required parameter 'body' when calling replaceCustomResourceDefinition(Async)");
        }


        com.squareup.okhttp.Call call = replaceCall(namespace, name, body, pretty, progressListener, progressRequestListener);
        return call;





    }

    /**
     *
     * replace the specified CustomResourceDefinition
     * @param name name of the CustomResourceDefinition (required)
     * @param body  (required)
     * @param pretty If &#39;true&#39;, then the output is pretty printed. (optional)
     * @return V1beta1CustomResourceDefinition
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public T replace(String namespace, String name, T body, String pretty) throws ApiException {
        ApiResponse<T> resp = replaceWithHttpInfo(namespace, name, body, pretty);
        return resp.getData();
    }

    /**
     *
     * replace the specified CustomResourceDefinition
     * @param name name of the CustomResourceDefinition (required)
     * @param body  (required)
     * @param pretty If &#39;true&#39;, then the output is pretty printed. (optional)
     * @return ApiResponse&lt;V1beta1CustomResourceDefinition&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ApiResponse<T> replaceWithHttpInfo(String namespace, String name, T body, String pretty) throws ApiException {
        com.squareup.okhttp.Call call = replaceValidateBeforeCall(namespace, name, body, pretty, null, null);
        Type localVarReturnType = new TypeToken<T>(){}.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    /**
     *  (asynchronously)
     * replace the specified CustomResourceDefinition
     * @param name name of the CustomResourceDefinition (required)
     * @param body  (required)
     * @param pretty If &#39;true&#39;, then the output is pretty printed. (optional)
     * @param callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     */
    public com.squareup.okhttp.Call replaceAsync(String namespace, String name, T body, String pretty, final ApiCallback<T> callback) throws ApiException {

        ProgressResponseBody.ProgressListener progressListener = null;
        ProgressRequestBody.ProgressRequestListener progressRequestListener = null;

        if (callback != null) {
            progressListener = new ProgressResponseBody.ProgressListener() {
                @Override
                public void update(long bytesRead, long contentLength, boolean done) {
                    callback.onDownloadProgress(bytesRead, contentLength, done);
                }
            };

            progressRequestListener = new ProgressRequestBody.ProgressRequestListener() {
                @Override
                public void onRequestProgress(long bytesWritten, long contentLength, boolean done) {
                    callback.onUploadProgress(bytesWritten, contentLength, done);
                }
            };
        }

        com.squareup.okhttp.Call call = replaceValidateBeforeCall(namespace, name, body, pretty, progressListener, progressRequestListener);
        Type localVarReturnType = new TypeToken<T>(){}.getType();
        apiClient.executeAsync(call, localVarReturnType, callback);
        return call;
    }
    /**
     * Build call for replaceStatus
     * @param name name of the CustomResourceDefinition (required)
     * @param body  (required)
     * @param pretty If &#39;true&#39;, then the output is pretty printed. (optional)
     * @param progressListener Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public com.squareup.okhttp.Call replaceStatusCall(String namespace, String name, T body, String pretty, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        Object localVarPostBody = body;

        // create path and map variables
        String localVarPath = apiGroup() + "/" + version + namespaced(namespace) + "/" + plural + "/{name}/status"
                .replaceAll("\\{" + "name" + "\\}", apiClient.escapeString(name.toString()));

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        if (pretty != null)
            localVarQueryParams.addAll(apiClient.parameterToPair("pretty", pretty));

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
                "application/json", "application/yaml", "application/vnd.kubernetes.protobuf"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {
                "*/*"
        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if(progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(new com.squareup.okhttp.Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(com.squareup.okhttp.Interceptor.Chain chain) throws IOException {
                    com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                            .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                            .build();
                }
            });
        }

        String[] localVarAuthNames = new String[] { "BearerToken" };
        return apiClient.buildCall(localVarPath, "PUT", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAuthNames, progressRequestListener);
    }

    @SuppressWarnings("rawtypes")
    private com.squareup.okhttp.Call replaceStatusValidateBeforeCall(String namespace, String name, T body, String pretty, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {

        // verify the required parameter 'name' is set
        if (name == null) {
            throw new ApiException("Missing the required parameter 'name' when calling replaceStatus(Async)");
        }

        // verify the required parameter 'body' is set
        if (body == null) {
            throw new ApiException("Missing the required parameter 'body' when calling replaceStatus(Async)");
        }


        com.squareup.okhttp.Call call = replaceStatusCall(namespace, name, body, pretty, progressListener, progressRequestListener);
        return call;





    }

    /**
     *
     * replace status of the specified CustomResourceDefinition
     * @param name name of the CustomResourceDefinition (required)
     * @param body  (required)
     * @param pretty If &#39;true&#39;, then the output is pretty printed. (optional)
     * @return V1beta1CustomResourceDefinition
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public T replaceStatus(String namespace, String name, T body, String pretty) throws ApiException {
        ApiResponse<T> resp = replaceStatusWithHttpInfo(namespace, name, body, pretty);
        return resp.getData();
    }

    /**
     *
     * replace status of the specified CustomResourceDefinition
     * @param name name of the CustomResourceDefinition (required)
     * @param body  (required)
     * @param pretty If &#39;true&#39;, then the output is pretty printed. (optional)
     * @return ApiResponse&lt;V1beta1CustomResourceDefinition&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ApiResponse<T> replaceStatusWithHttpInfo(String namespace, String name, T body, String pretty) throws ApiException {
        com.squareup.okhttp.Call call = replaceStatusValidateBeforeCall(namespace, name, body, pretty, null, null);
        Type localVarReturnType = new TypeToken<T>(){}.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    /**
     *  (asynchronously)
     * replace status of the specified CustomResourceDefinition
     * @param name name of the CustomResourceDefinition (required)
     * @param body  (required)
     * @param pretty If &#39;true&#39;, then the output is pretty printed. (optional)
     * @param callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     */
    public com.squareup.okhttp.Call replaceStatusAsync(String namespace, String name, T body, String pretty, final ApiCallback<T> callback) throws ApiException {

        ProgressResponseBody.ProgressListener progressListener = null;
        ProgressRequestBody.ProgressRequestListener progressRequestListener = null;

        if (callback != null) {
            progressListener = new ProgressResponseBody.ProgressListener() {
                @Override
                public void update(long bytesRead, long contentLength, boolean done) {
                    callback.onDownloadProgress(bytesRead, contentLength, done);
                }
            };

            progressRequestListener = new ProgressRequestBody.ProgressRequestListener() {
                @Override
                public void onRequestProgress(long bytesWritten, long contentLength, boolean done) {
                    callback.onUploadProgress(bytesWritten, contentLength, done);
                }
            };
        }

        com.squareup.okhttp.Call call = replaceStatusValidateBeforeCall(namespace, name, body, pretty, progressListener, progressRequestListener);
        Type localVarReturnType = new TypeToken<T>(){}.getType();
        apiClient.executeAsync(call, localVarReturnType, callback);
        return call;
    }



    public T replaceIfNotModified(String namespace, String name, T item) throws ApiException {
        // TODO: check this
        return replace(namespace, name, item, null);
    }

    public AsyncWatcher watchAsync(String namespace, String resourceVersion, Class<T> clazz, Watcher<T> watcher) throws ApiException {
        Call call = listCall(namespace, null, null, null, true, null, null, resourceVersion, 0, true, null, null);
        apiClient.setConnectTimeout(0);

        Thread t = new Thread() {
            @Override
            public void run() {


                try {
                    Watch<T> watch = Watch.createWatch(slowApiClient,
                            call, Watch.Response.class);

                    ObjectMapper om = new ObjectMapper();

                    for (Watch.Response<T> pod : watch) {
                        T t = om.readValue(om.writeValueAsString(pod.object), clazz);
                        watcher.eventReceived(Watcher.Action.valueOf(pod.type), t);
                    }
                    watcher.onClose(null);
                } catch (ApiException e) {
                    watcher.onClose(e);
                } catch (Exception e) {
                    watcher.onClose(new ApiException(e));
                } finally {
                    call.cancel();
                }
            }
        };

        t.start();

        return new AsyncWatcher(t);
    }


}