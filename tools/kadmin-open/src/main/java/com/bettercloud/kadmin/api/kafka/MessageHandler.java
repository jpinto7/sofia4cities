package com.bettercloud.kadmin.api.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * Created by davidesposito on 7/19/16.
 */
public interface MessageHandler {

    void handle(ConsumerRecord<String, Object> record);
}
