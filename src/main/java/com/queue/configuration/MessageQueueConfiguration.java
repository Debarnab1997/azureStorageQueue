package com.queue.configuration;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;


@Configuration
public class MessageQueueConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(MessageQueueConfiguration.class);

    @Value ("${azure.storage.connectionString}")
    private String connectionString;

    @Value ("${azure.storage.queueName}")
    private String queueName;

    @Bean
    public QueueClient getQueueClient() {
        LOG.info("Creating Azure Storage Queue Client for queue: {}", "balsampoc1");

        QueueClient queueClient =
                new QueueClientBuilder().connectionString(connectionString).queueName(queueName).buildClient();
        queueClient.createIfNotExists();

        return queueClient;
    }
}
