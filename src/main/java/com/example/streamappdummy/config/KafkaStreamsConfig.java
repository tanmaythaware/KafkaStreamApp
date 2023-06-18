package com.example.streamappdummy.config;

import com.example.streamappdummy.serde.EventSerde;
import com.example.streamappdummy.util.Constants;
import com.example.streamappdummy.util.KafkaStreamProperties;
import com.example.streamappdummy.util.StreamListener;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.ProcessorSupplier;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;

@Slf4j
@Configuration
public class KafkaStreamsConfig {

    private static final long STREAM_KILL_TIMEOUT = 3000;

    private final CountDownLatch latch = new CountDownLatch(1);

    private Map<String, KafkaStreams> streams = new HashMap<>();

    private Map<String, StreamListener> streamExceptionListeners;

    /**
     * This bean provides KafkaStream which can be used to start the stream.
     *
     * @param appStreamProperties : Stream Properties with Transaction Cache Details
     * @param processorMap        : List of Processors
     * @param rootProcessor       : AbstractProcessor instance of Root Processor to fetch Sinks
     * @return : Kafka Stream
     */
    public KafkaStreams initializeStream(@NonNull KafkaStreamProperties appStreamProperties,
                                         Map<String, AbstractProcessor> processorMap,
                                         @NonNull AbstractProcessor<String, Object> rootProcessor,
                                         @NonNull ProcessorSupplier<String, Object> processorSupplier) throws InvalidPropertiesFormatException {

        final Properties props = new Properties();
        final Topology topology = new Topology();

        if (appStreamProperties.getStreamId() == null || appStreamProperties.getStreamId().isEmpty())
            throw new InvalidPropertiesFormatException("Stream Id should not be null or empty.");


        // Check whether the stream already exists and in the running state or else remove it from the map
        if (streams.containsKey(appStreamProperties.getStreamId())) {
            KafkaStreams kafkaStream = streams.get(appStreamProperties.getStreamId());

            if (kafkaStream.state().isRunning())
                return kafkaStream;

            kafkaStream.close(Duration.ofMillis(STREAM_KILL_TIMEOUT));
            latch.countDown();
            streams.remove(appStreamProperties.getStreamId());
        }

        setStreamProperties(props, appStreamProperties);
        setStreamTopology(topology, processorMap, appStreamProperties.getSourceTopic(), rootProcessor, processorSupplier);

        KafkaStreams stream = new KafkaStreams(topology, props);
        setUncaughtExceptionHandler(stream, appStreamProperties.getStreamId());
        streams.put(appStreamProperties.getStreamId(), stream);
        return stream;
    }

    private void setUncaughtExceptionHandler(KafkaStreams stream, String streamId) {
        stream.setUncaughtExceptionHandler((t, e) -> {
            log.error("Uncaught exception in stream with streamId : {} due to {}", streamId, e.getMessage(), e);
            try {
                // Properly CLose the Stream
                if (!stream.state().isRunning()) {
                    stream.close(Duration.ofMillis(STREAM_KILL_TIMEOUT));
                    latch.countDown();
                }
            } catch (Exception ex) {
                log.error("Exception in closing the Stream with app id {} due to {}", streamId,
                        ex.getMessage());
            } finally {
                if (streamExceptionListeners != null && !streamExceptionListeners.isEmpty() &&
                        streamExceptionListeners.containsKey(streamId) &&
                        streamExceptionListeners.get(streamId) != null) {
                    streamExceptionListeners.get(streamId).trigger();
                }
            }
        });
    }



    /**
     * Configure the Application Specific Properties in Stream Properties
     *
     * @param properties          Properties object
     * @param appStreamProperties : Application Specific Properties
     */
    private void setStreamProperties(Properties properties, KafkaStreamProperties appStreamProperties) {
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, appStreamProperties.getStreamId());
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, appStreamProperties.getBootstrapServerConfig());
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        if (appStreamProperties.getMessageSerde() != null && !appStreamProperties.getMessageSerde().isEmpty())
            properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, appStreamProperties.getMessageSerde());
        else
            properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, EventSerde.class.getName());

        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, appStreamProperties.getAutoOffsetResetConfig());
        properties.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.AT_LEAST_ONCE);
        properties.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class);
        if (appStreamProperties.getMaxPollRecordsConfig() != 0)
            properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, appStreamProperties.getMaxPollRecordsConfig());
        else
            properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);

        // Add Kafka SSL Configurations
        if (appStreamProperties.isKafkaSecurityEnabled()) {
            properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, appStreamProperties.getKafkaSecurityProtocol());
            properties.put(SaslConfigs.SASL_MECHANISM, appStreamProperties.getKafkaSASLMechanism());
            properties.put(SaslConfigs.SASL_JAAS_CONFIG, appStreamProperties.getKafkaSASLJaas());
            properties.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, appStreamProperties.getKafkaSSLPassword());
            properties.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, appStreamProperties.getKafkaSSLKeyStore());
            properties.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, appStreamProperties.getKafkaSSLPassword());
            properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, appStreamProperties.getKafkaSSLTrustStore());
            properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, appStreamProperties.getKafkaSSLPassword());
        }
    }

    /**
     * Add the Source, processors in the Stream Topology
     *
     * @param topology      : topology instance
     * @param processorMap  : processors Map
     * @param sourceTopic   : name of the source topic
     * @param rootProcessor : AbstractProcessor instance of rootProcessor to fetch sinks
     */
    private void setStreamTopology(Topology topology,
                                   Map<String, AbstractProcessor> processorMap,
                                   String sourceTopic,
                                   AbstractProcessor<String, Object> rootProcessor,
                                   ProcessorSupplier<String, Object> processorSupplier) {
        Map<String, List<String>> sinkMap = new HashMap<>();

        //Add Source topic to topology
        topology.addSource(sourceTopic + "_source", sourceTopic);
        //Add ProcessRouter Processor to topology
        topology.addProcessor(rootProcessor.getClass().getSimpleName(), processorSupplier, sourceTopic + "_source");
        verifyAndAddSinkListToMap(sinkMap, rootProcessor);

        if (processorMap != null && !processorMap.isEmpty()) {
            processorMap.forEach((k, v) -> {
                //Add Processor to topology
                topology.addProcessor(v.getClass().getSimpleName(), () -> {
                    try {
                        return v.getClass().getDeclaredConstructor().newInstance();
                    } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                        log.error("Exception while initializing topology : {}", e.getMessage(), e);
                    }
                    return null;
                }, rootProcessor.getClass().getSimpleName());
                verifyAndAddSinkListToMap(sinkMap, v);
            });
        }

        //Add sink to topology
        sinkMap.forEach((k, v) -> topology.addSink(k, k, v.toArray(new String[0])));
    }

    /**
     * Verify whether the provided processor contains method for sink topics, if so collect it into the map
     *
     * @param sinkMap   : Map to collect sink topic & it's processor list
     * @param processor : processor instance
     */
    private void verifyAndAddSinkListToMap(Map<String, List<String>> sinkMap, AbstractProcessor processor) {
        Method getSinks;
        try {
            getSinks = processor.getClass().getMethod(Constants.GET_SINKS_METHOD_NAME);
            //Get all the sinks for the processor
            List<String> sinks = (List<String>) getSinks.invoke(processor);

            for (String sink : sinks) {
                if (sinkMap.containsKey(sink)) {
                    //Add parentName to existing sink
                    sinkMap.get(sink).add(processor.getClass().getSimpleName());
                } else {
                    List<String> processors = new ArrayList<>();
                    processors.add(processor.getClass().getSimpleName());
                    //Add parentName to new sink
                    sinkMap.put(sink, processors);
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.warn("Exception while calling getSink : {}", e.getMessage());
        }
    }

    @PreDestroy
    private void destroyStreams() {
        if (!streams.isEmpty()) {
            streams.forEach((streamId, stream) -> {
                stream.close(Duration.ofMillis(STREAM_KILL_TIMEOUT));
                latch.countDown();
            });
        }
    }

    /**
     * Close and Remove the specific Stream explicitly
     *
     * @param streamId : unique stream id
     */
    public void destroyStream(@NonNull String streamId) {
        if (streams.containsKey(streamId)) {
            KafkaStreams kafkaStream = streams.get(streamId);

            kafkaStream.close(Duration.ofMillis(STREAM_KILL_TIMEOUT));
            latch.countDown();
            streams.remove(streamId);
        }
    }

    /**
     * Add Stream Listener for handling exception in a stream
     *
     * @param streamId : unique stream id
     */
    public void addStreamListener(@NonNull String streamId, StreamListener streamListener) {
        if (streamExceptionListeners == null)
            streamExceptionListeners = new HashMap<>();

        streamExceptionListeners.put(streamId, streamListener);
    }
}
