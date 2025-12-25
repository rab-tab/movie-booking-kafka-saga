package com.javatechie.config;

import com.javatechie.common.NonRecoverableBusinessException;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

@Configuration
public class KafkaErrorHandlerConfig {

    @Bean
    public DefaultErrorHandler errorHandler(
            KafkaTemplate<Object, Object> kafkaTemplate
    ) {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, ex) ->
                                new TopicPartition(record.topic() + "-dlt",
                                        record.partition())
                );



        FixedBackOff backOff = new FixedBackOff(2000L, 3);
        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(recoverer, new FixedBackOff(2000L, 3));
        // ✅ Retryable (TRANSIENT – infra issues)
        errorHandler.addRetryableExceptions(
                SQLException.class,
                DataAccessException.class,
                TimeoutException.class
        );

        // ❌ Non-retryable (BUSINESS)
        errorHandler.addNotRetryableExceptions(
                IllegalArgumentException.class,
                NonRecoverableBusinessException.class
        );

        return new DefaultErrorHandler(recoverer, backOff);
    }
}