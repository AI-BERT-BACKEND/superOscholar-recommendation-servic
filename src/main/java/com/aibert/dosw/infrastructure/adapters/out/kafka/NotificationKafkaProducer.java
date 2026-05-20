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
        try {
            String payload = objectMapper.writeValueAsString(event);
            String key = event.getUserId() != null ? String.valueOf(event.getUserId()) : "unknown";
            kafkaTemplate.send(notificationsTopic, key, payload);
            log.info("Notificación enviada al tópico [{}] para usuario [{}] tipo [{}]",
                    notificationsTopic, event.getUserId(), event.getType());
        } catch (JsonProcessingException e) {
            log.error("Error serializando notificación para usuario [{}]: {}", event.getUserId(), e.getMessage());
        } catch (Exception e) {
            log.error("Error enviando notificación Kafka para usuario [{}]: {}", event.getUserId(), e.getMessage());
        }
    }
}
