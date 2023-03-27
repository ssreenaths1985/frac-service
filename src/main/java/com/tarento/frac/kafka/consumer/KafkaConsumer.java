package com.tarento.frac.kafka.consumer;

import java.util.Map;

import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

public interface KafkaConsumer {
	public void processMessage(final Map incomingData, @Header(KafkaHeaders.RECEIVED_TOPIC) final String topic);
}
