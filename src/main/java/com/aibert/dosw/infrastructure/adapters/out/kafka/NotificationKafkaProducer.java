package com.aibert.dosw.infrastructure.adapters.out.kafka;

import com.aibert.dosw.domain.model.NotificationEvent;
import com.aibert.dosw.domain.port.out.NotificationEventPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationKafkaProducer implements NotificationEventPort {

    private static final Logger log = LoggerFactory.getLogger(NotificationKafkaProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.notifications:student-notifications}")
    private String notificationsTopic;

    public NotificationKafkaProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void sendNotification(NotificationEvent event) {
        if (event == null) {
            log.warn("Notification event is null. Skipping Kafka send.");
            return;
        }

        try {
            String payload = objectMapper.writeValueAsString(event);
            String key = event.getUserId() != null ? String.valueOf(event.getUserId()) : "unknown";

            kafkaTemplate.send(notificationsTopic, key, payload).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Kafka send failed. topic=[{}], userId=[{}], type=[{}], reason=[{}]",
                            notificationsTopic, event.getUserId(), event.getType(), ex.getMessage(), ex);
                    return;
                }

                log.info("Kafka send ok. topic=[{}], partition=[{}], offset=[{}], userId=[{}], type=[{}]",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        event.getUserId(),
                        event.getType());
            });
        } catch (JsonProcessingException e) {
            log.error("Kafka payload serialization failed for userId=[{}]: {}", event.getUserId(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected Kafka send setup error for userId=[{}]: {}", event.getUserId(), e.getMessage(), e);
        }
    }
}
