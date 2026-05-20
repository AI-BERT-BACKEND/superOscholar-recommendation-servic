package com.aibert.dosw.infrastructure.adapters.out.kafka;

import com.aibert.dosw.domain.model.NotificationEvent;
import com.aibert.dosw.domain.model.NotificationSeverity;
import com.aibert.dosw.domain.model.NotificationType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationKafkaProducerTest {

    @Test
    void sendNotification_withNullEvent_doesNothing() {
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        NotificationKafkaProducer producer = new NotificationKafkaProducer(kafkaTemplate, objectMapper);

        producer.sendNotification(null);

        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void sendNotification_withValidEventAndUser_sendsToKafka() throws Exception {
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        NotificationKafkaProducer producer = new NotificationKafkaProducer(kafkaTemplate, objectMapper);
        ReflectionTestUtils.setField(producer, "notificationsTopic", "student-notifications");

        NotificationEvent event = NotificationEvent.builder()
                .userId(10L)
                .type(NotificationType.STUDY_SUGGESTION)
                .title("title")
                .message("message")
                .severity(NotificationSeverity.MEDIUM)
                .relatedEntityId(99L)
                .build();

        when(objectMapper.writeValueAsString(event)).thenReturn("{\"ok\":true}");
        when(kafkaTemplate.send(eq("student-notifications"), eq("10"), eq("{\"ok\":true}")))
                .thenReturn(CompletableFuture.completedFuture(successResult()));

        producer.sendNotification(event);

        verify(kafkaTemplate).send("student-notifications", "10", "{\"ok\":true}");
    }

    @Test
    void sendNotification_withNullUser_usesUnknownKey() throws Exception {
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        NotificationKafkaProducer producer = new NotificationKafkaProducer(kafkaTemplate, objectMapper);
        ReflectionTestUtils.setField(producer, "notificationsTopic", "student-notifications");

        NotificationEvent event = NotificationEvent.builder()
                .userId(null)
                .type(NotificationType.OVERLOAD_ALERT)
                .title("title")
                .message("message")
                .severity(NotificationSeverity.HIGH)
                .build();

        when(objectMapper.writeValueAsString(event)).thenReturn("{\"ok\":true}");
        when(kafkaTemplate.send(eq("student-notifications"), eq("unknown"), eq("{\"ok\":true}")))
                .thenReturn(CompletableFuture.completedFuture(successResult()));

        producer.sendNotification(event);

        verify(kafkaTemplate).send("student-notifications", "unknown", "{\"ok\":true}");
    }

    @Test
    void sendNotification_withJsonProcessingException_doesNotSend() throws Exception {
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        NotificationKafkaProducer producer = new NotificationKafkaProducer(kafkaTemplate, objectMapper);
        ReflectionTestUtils.setField(producer, "notificationsTopic", "student-notifications");

        NotificationEvent event = NotificationEvent.builder().userId(10L).build();
        when(objectMapper.writeValueAsString(event)).thenThrow(new JsonProcessingException("bad json") {
        });

        producer.sendNotification(event);

        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void sendNotification_withKafkaSetupException_isHandled() throws Exception {
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        NotificationKafkaProducer producer = new NotificationKafkaProducer(kafkaTemplate, objectMapper);
        ReflectionTestUtils.setField(producer, "notificationsTopic", "student-notifications");

        NotificationEvent event = NotificationEvent.builder().userId(10L).type(NotificationType.STUDY_SUGGESTION).build();
        when(objectMapper.writeValueAsString(event)).thenReturn("{\"ok\":true}");
        when(kafkaTemplate.send(any(), any(), any())).thenThrow(new RuntimeException("send setup error"));

        producer.sendNotification(event);

        verify(kafkaTemplate).send("student-notifications", "10", "{\"ok\":true}");
    }

    @Test
    void sendNotification_withAsyncFailure_isHandledInCallback() throws Exception {
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        NotificationKafkaProducer producer = new NotificationKafkaProducer(kafkaTemplate, objectMapper);
        ReflectionTestUtils.setField(producer, "notificationsTopic", "student-notifications");

        NotificationEvent event = NotificationEvent.builder().userId(10L).type(NotificationType.STUDY_SUGGESTION).build();
        when(objectMapper.writeValueAsString(event)).thenReturn("{\"ok\":true}");
        when(kafkaTemplate.send(eq("student-notifications"), eq("10"), eq("{\"ok\":true}")))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("async send failed")));

        producer.sendNotification(event);

        verify(kafkaTemplate).send("student-notifications", "10", "{\"ok\":true}");
    }

    private SendResult<String, String> successResult() {
        ProducerRecord<String, String> record = new ProducerRecord<>("student-notifications", "10", "{\"ok\":true}");
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("student-notifications", 0),
                0,
                42,
                System.currentTimeMillis(),
                Long.valueOf(0L),
                10,
                20);
        return new SendResult<>(record, metadata);
    }
}
