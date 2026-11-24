package com.faster360.notificationticketservice.consumer.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/** Audita en logs los mensajes que llegaron al Dead Letter Topic, con la causa original del fallo. */
@Component
public class DeadLetterKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterKafkaListener.class);

    @KafkaListener(
            topics = "${faster360.kafka.topics.dlt}",
            groupId = "${faster360.kafka.groups.dlt}",
            containerFactory = "deadLetterListenerFactory")
    public void onDeadLetter(ConsumerRecord<String, byte[]> record) {
        String payload = record.value() == null ? "<null>" : new String(record.value(), StandardCharsets.UTF_8);
        log.error("[KAFKA DLT] Mensaje no procesable. Origen={}-{}@{} clave={} causa=\"{}\" payload={}",
                header(record, KafkaHeaders.DLT_ORIGINAL_TOPIC),
                header(record, KafkaHeaders.DLT_ORIGINAL_PARTITION),
                header(record, KafkaHeaders.DLT_ORIGINAL_OFFSET),
                record.key(),
                header(record, KafkaHeaders.DLT_EXCEPTION_MESSAGE),
                payload);
    }

    private String header(ConsumerRecord<String, byte[]> record, String name) {
        Header h = record.headers().lastHeader(name);
        if (h == null || h.value() == null) {
            return "n/d";
        }
        byte[] v = h.value();
        // partición y offset viajan como enteros big-endian; el resto como texto UTF-8
        if (name.equals(KafkaHeaders.DLT_ORIGINAL_PARTITION) && v.length == 4) {
            return String.valueOf(java.nio.ByteBuffer.wrap(v).getInt());
        }
        if (name.equals(KafkaHeaders.DLT_ORIGINAL_OFFSET) && v.length == 8) {
            return String.valueOf(java.nio.ByteBuffer.wrap(v).getLong());
        }
        return new String(v, StandardCharsets.UTF_8);
    }
}
