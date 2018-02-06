package de.predic8.kubernetesclient.client;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.JSON;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.lang.reflect.Type;
import java.util.Base64;
import java.util.Date;

public abstract class NamespacedApiClient extends ApiClient {


    public NamespacedApiClient() {
        setJSON(new ByteArrayHandlingJSON());
    }

    /**
     * @return "default" (overrideable using the "kubernetes.client.namespace" Spring property) if running outside of the cluster,
     * the pod's own namespace, if running on the inside.
     */
    public abstract String getMyNamespace();

    private static class ByteArrayHandlingJSON extends JSON {
        public ByteArrayHandlingJSON() {
            setGson(new GsonBuilder()
                .registerTypeAdapter(Date.class, new DateTypeAdapter())
                .registerTypeAdapter(java.sql.Date.class, new SqlDateTypeAdapter())
                .registerTypeAdapter(DateTime.class, new DateTimeTypeAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
                .registerTypeAdapter(byte[].class, new ByteArrayBase64StringTypeAdapter())
                .create());

        }
    }

    public static class ByteArrayBase64StringTypeAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {

        public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Base64.getDecoder().decode(json.getAsString());
        }
        public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(Base64.getEncoder().encodeToString(src));
        }
    }
}
