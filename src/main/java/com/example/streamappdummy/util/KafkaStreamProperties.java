package com.example.streamappdummy.util;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class KafkaStreamProperties {

    private String bootstrapServerConfig;

    private String autoOffsetResetConfig;

    private String appId;

    private int keyExpiryTimeoutInSec;

    private String redisHost;

    private int redisPort;

    private String redisPassword;

    private boolean redisSSlEnabled;

    private int redisMinIdleConnection;

    private int redisMaxIdleConnection;

    private String streamId;

    private String sourceTopic;

    private int redisDBIndex;

    private boolean kafkaSecurityEnabled;

    private String kafkaSecurityProtocol;

    private String kafkaSASLMechanism;

    private String kafkaSASLJaas;

    private String kafkaSSLPassword;

    private String kafkaSSLKeyStore;

    private String kafkaSSLTrustStore;

    private String messageSerde;

    private int maxPollRecordsConfig;

}
