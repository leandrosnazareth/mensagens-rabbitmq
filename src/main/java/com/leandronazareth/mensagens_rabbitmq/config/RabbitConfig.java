package com.leandronazareth.mensagens_rabbitmq.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    /**
     * Name of the queue used to publish SMS requests.
     * Keep it consistent between producer and consumer.
     */
    public static final String SMS_QUEUE = "sms.rpc.queue";

    /**
     * Reply timeout used by RabbitTemplate when doing RPC-style send/receive.
     * The application also uses this value to wait for callbacks.
     */
    @Value("${timeout.delay.ms:60000}")
    private long timeoutMs;

    /**
     * Declare a durable queue for SMS requests. Durable makes the queue survive broker restarts.
     */
    @Bean
    public Queue smsQueue() {
        return new Queue(SMS_QUEUE, true);
    }

    /**
     * Use Jackson to (de)serialize message payloads as JSON.
     */
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configure a RabbitTemplate with JSON converter and a reply timeout.
     * The template is used by the SmsService to publish messages.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setReplyTimeout(timeoutMs);
        template.setMessageConverter(jackson2JsonMessageConverter());
        return template;
    }

    /**
     * Listener container factory with JSON message conversion. Consumers in the
     * same application (if any) should use this factory to deserialize messages.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter());
        return factory;
    }

}
