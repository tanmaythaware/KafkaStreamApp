package com.example.streamappdummy;

import org.apache.kafka.streams.processor.AbstractProcessor;

import java.util.ArrayList;
import java.util.List;


public class EnrichmentProcessor extends AbstractProcessor<String, Object> {


    @Override
    public void process(String key, Object event) {
        try {
            System.out.println(event);
        } catch (Exception e) {
        }
    }

    public List<String> getSinks() {
        List<String> sinks = new ArrayList<>();
        sinks.add("processed_events");
        sinks.add("failed_events");
        return sinks;
    }
}