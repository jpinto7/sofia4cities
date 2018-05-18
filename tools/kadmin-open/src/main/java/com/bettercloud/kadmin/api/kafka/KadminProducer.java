package com.bettercloud.kadmin.api.kafka;

/**
 * Created by davidesposito on 7/19/16.
 */
public interface KadminProducer<KeyT, ValueT> {

    long getSentCount();

    long getErrorCount();

    long getLastUsedTime();

    KadminProducerConfig getConfig();

    String getId();

    void send(KeyT key, ValueT val);

    void shutdown();
}
