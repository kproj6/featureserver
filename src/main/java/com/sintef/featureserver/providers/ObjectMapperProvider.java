package com.sintef.featureserver.providers;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * The JSON object mapper for Jersey to automatically serialize and deserialize JSON input data to
 * and from java objects.
 */
@Provider
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {
    ObjectMapper mapper;

    public ObjectMapperProvider(){
        mapper = new ObjectMapper();
        // Ignores the wrapping {} around the incomming json object.
        // { "amountTransaction": {...}} is deserialized as "amountTransaction": {...}.
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        // If the java object is an arraylist of objects the deserializer accepts a single object
        // with this field name and puts it into this arraylist.
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        // Adds the wrapping {} around the java object when you serialize the data.
        // "amountTransaction": {...} is serialized as { "amountTransaction": {...}}.
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}
