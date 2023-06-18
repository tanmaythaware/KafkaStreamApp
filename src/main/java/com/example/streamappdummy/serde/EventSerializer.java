package com.example.streamappdummy.serde;


import com.example.streamappdummy.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;

@Slf4j
public class EventSerializer implements Serializer<Object> {

    @Override
    public byte[] serialize(String s, Object event) {
        byte[] retVal = null;
        try {
            retVal = Utils
                    .getObjectMapper()
                    .writeValueAsString(event)
                    .getBytes();
        } catch (Exception e) {
            log.error("Exception in Serializer:", e);
        }
        return retVal;
    }
}
