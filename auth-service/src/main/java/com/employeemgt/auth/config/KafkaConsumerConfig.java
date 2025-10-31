package com.employeemgt.auth.config;

import com.employeemgt.auth.model.EmployeeEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Consumer Configuration for Auth Service
 * Following GeeksforGeeks Spring Boot Kafka integration guide
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:auth-service-group}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, EmployeeEvent> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        
        // Basic Kafka consumer configuration
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        // Key deserializer
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        
        // Value deserializer with error handling
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, EmployeeEvent.class.getName());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false); // Ignore type headers from producer
        
        // Additional consumer settings
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "10000");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "10");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Disable auto commit for manual ack
        
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EmployeeEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, EmployeeEvent> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        
        // Set concurrency level
        factory.setConcurrency(1);
        
        // Enable auto startup
        factory.setAutoStartup(true);
        
        // Configure manual acknowledgment mode
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        return factory;
    }

    /**
     * Create Kafka topic for employee events
     */
    @Bean
    public NewTopic employeeEventsTopic() {
        return new NewTopic("employee-events", 3, (short) 1);
    }
}