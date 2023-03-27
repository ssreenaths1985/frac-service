package com.tarento.frac.kafka.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

	public static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducer.class);

	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;

	public void sendMessage(String topicName, Object message) {
		kafkaTemplate.send(topicName, "FRAC", message);
		LOGGER.info(String.format("Pushed to Kafka Topic: %s, value = %s", topicName, message));
	}
}
