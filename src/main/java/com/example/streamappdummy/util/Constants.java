package com.example.streamappdummy.util;

public class Constants {

    public static final String PROP_KAFKA_OFFSET_RESET_POLICY = "kafka.offset.reset.policy";
    public static final String PROP_KAFKA_BROKERS = "kafka.brokers";
    public static final String PROP_KAFKA_RETRY_SINK_TOPIC = "kafka.retry.sink.topic";
    public static final String PROP_FAILED_SINK_TOPIC = "kafka.failed.sink.topic";
    public static final String PROP_RETRY_APPLICATION_ID = "kafka.retry.application.id";
    public static final String PROP_GROUP_APPLICATION_ID = "kafka.group.application.id";
    public static final String PROP_KAFKA_EVENT_SOURCE_TOPIC = "kafka.event.source.topic";
    public static final String PROP_KAFKA_SSL_KEYSTORE_LOCATION = "spring.kafka.ssl.keystore-location";
    public static final String PROP_KAFKA_SSL_TRUSTSTORE_LOCATION = "spring.kafka.ssl.truststore-location";
    public static final String PROP_KAFKA_SSL_KEY_PASSWORD = "spring.kafka.ssl.key-password";
    public static final String PROP_KAFKA_SECURITY_PROTOCOL = "spring.kafka.properties.security.protocol";
    public static final String PROP_KAFKA_SASL_MECHANISM = "spring.kafka.properties.sasl.mechanism";
    public static final String PROP_KAFKA_SASL_JAAS_CONFIG = "spring.kafka.properties.sasl.jaas.config";
    public static final String PROP_KAFKA_SECURITY_ENABLED = "spring.kafka.security.enabled";

    public enum EventType {
        EVENT_TYPE("event.type");
        private final String typeCode;

        EventType(String typeCode) { this.typeCode = typeCode; }

        public String getType() { return typeCode; }
    }

    public enum CACHE_OPERATION {
        SET("SET"),
        DELETE("DELETE"),
        LIST_APPEND("LIST_APPEND"),
        LIST_DELETE("LIST_DELETE");
        private final String operation;

        CACHE_OPERATION(String operation) {
            this.operation = operation;
        }

        public String getOperation() { return operation; }
    }

    public enum KAFKA_SINK {
        PROCESSED_SINK("processed_events"),
        RETRY_SINK("retry_events"),
        FAILED_SINK("failed_events");
        private final String sink;

        KAFKA_SINK(String sink) {
            this.sink = sink;
        }

        public String getSink() { return sink; }
    }

    public static final String GET_SINKS_METHOD_NAME = "getSinks";

}