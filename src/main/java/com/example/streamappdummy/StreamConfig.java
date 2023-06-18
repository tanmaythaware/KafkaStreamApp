package com.example.streamappdummy;



import com.example.streamappdummy.util.Constants;
import com.example.streamappdummy.util.KafkaStreamProperties;
import com.example.streamappdummy.config.KafkaStreamsConfig;
import lombok.Getter;


import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.processor.AbstractProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;

@Service
public class StreamConfig {

    @Getter
    private KafkaStreams kafkaStreams;

    @Autowired
    private ApplicationContext context;

    @Value("${kafka.application.id}")
    private String applicationId;

    @Value("${kafka.brokers}")
    private String brokers;

    @Value("${kafka.offset.reset.policy}")
    private String offsetPolicy;

    @Value("${kafka.event.source.topic}")
    private String kafkaEventSourceTopic;

    @Value("${spring.kafka.security.enabled}")
    private boolean kafkaSecurityEnabled;

    @Autowired
    private KafkaStreamsConfig kafkaStreamsConfig;

    @Autowired
    private Environment environment;

    public static Map<String, AbstractProcessor> map;

   
    @PostConstruct
    public void init() {
        try {
            //Get all the Beans that extends AbstractProcessor
            map = context.getBeansOfType(AbstractProcessor.class);

            KafkaStreamProperties streamProperties = setStreamProperties();
            kafkaStreams = kafkaStreamsConfig.initializeStream(streamProperties, map, new EnrichmentProcessor(), EnrichmentProcessor::new);

            kafkaStreams.start();
        } catch (Exception e) {

            // Try Reconnecting the Stream
            if (!kafkaStreams.state().isRunning()) {
                init();
            }
        }
    }

    @PreDestroy
    public void cleanUp() {
        kafkaStreamsConfig.destroyStream(applicationId);
    }

    private KafkaStreamProperties setStreamProperties() {

        //set topology and properties
        KafkaStreamProperties streamProperties = KafkaStreamProperties.builder()
                .streamId(applicationId)
                .bootstrapServerConfig(brokers)
                .autoOffsetResetConfig(offsetPolicy)
                .sourceTopic(kafkaEventSourceTopic)
                .kafkaSecurityEnabled(kafkaSecurityEnabled)
                .build();

        if (kafkaSecurityEnabled) {
            streamProperties.setKafkaSASLJaas(environment.getProperty(Constants.PROP_KAFKA_SASL_JAAS_CONFIG));
            streamProperties.setKafkaSASLMechanism(environment.getProperty(Constants.PROP_KAFKA_SASL_MECHANISM));
            streamProperties.setKafkaSecurityProtocol(environment.getProperty(Constants.PROP_KAFKA_SECURITY_PROTOCOL));
            streamProperties.setKafkaSSLKeyStore(environment.getProperty(Constants.PROP_KAFKA_SSL_KEYSTORE_LOCATION));
            streamProperties.setKafkaSSLPassword(environment.getProperty(Constants.PROP_KAFKA_SSL_KEY_PASSWORD));
            streamProperties.setKafkaSSLTrustStore(environment.getProperty(Constants.PROP_KAFKA_SSL_TRUSTSTORE_LOCATION));
        }
        return streamProperties;
    }

}
