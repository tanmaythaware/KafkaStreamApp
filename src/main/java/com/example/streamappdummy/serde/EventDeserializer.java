package com.example.streamappdummy.serde;

import com.example.streamappdummy.dto.Event;
import com.example.streamappdummy.util.Utils;


import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.charset.StandardCharsets;

@Slf4j
public class EventDeserializer implements Deserializer<Object> {

    @Override
    public Event deserialize(String arg0, byte[] arg1) {
        Event event = null;
        try {
            String eventStr = new String(arg1, StandardCharsets.UTF_8);
            event = Utils
                    .getObjectMapper()
                    .readValue(eventStr, Event.class);
        } catch (Exception e) {
            log.error("Exception in Deserializer:", e);
        }
        return event;
    }
}
