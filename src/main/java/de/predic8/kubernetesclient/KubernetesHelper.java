package de.predic8.kubernetesclient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;

public class KubernetesHelper {
    public static ObjectMapper createYamlObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        return objectMapper;
    }

    public static <T> T loadYaml(byte[] data, Class<T> clazz) throws IOException {
        ObjectMapper mapper = createYamlObjectMapper();
        return mapper.readValue(data, clazz);
    }

    /*
    public static <T> T loadYaml(InputStream in, Class<T> clazz) throws IOException {
        byte[] data = Files.readBytes(in);
        return loadYaml(data, clazz);
    }
    */

    public static <T> T loadYaml(String text, Class<T> clazz) throws IOException {
        byte[] data = text.getBytes();
        return loadYaml(data, clazz);
    }

}
